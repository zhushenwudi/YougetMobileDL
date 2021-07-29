package com.ilab.yougetmobiledl.download

/**
 * 下载管理器实现类
 */

import com.ilab.yougetmobiledl.base.eventVM
import com.ilab.yougetmobiledl.db.DBController
import com.ilab.yougetmobiledl.model.DownloadInfo
import com.ilab.yougetmobiledl.model.DownloadInfo.Companion.STATUS_CONVERT
import com.ilab.yougetmobiledl.model.DownloadInfo.Companion.STATUS_DOWNLOADING
import com.ilab.yougetmobiledl.model.DownloadInfo.Companion.STATUS_ERROR
import com.ilab.yougetmobiledl.model.DownloadInfo.Companion.STATUS_NONE
import com.ilab.yougetmobiledl.model.DownloadInfo.Companion.STATUS_PREPARE_DOWNLOAD
import com.ilab.yougetmobiledl.model.DownloadedInfo
import com.ilab.yougetmobiledl.utils.AppUtil
import dev.utils.common.FileUtils
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object DownloadManagerImpl : DownloadManager {

    private val dbController = DBController()
    private val latestDownloadTask = mutableListOf<DownloadInfo>()

    init {
        val downloadList = dbController.findAllDownloading().toMutableList()
        downloadList.forEach {
            if (it.status == STATUS_PREPARE_DOWNLOAD || it.status == STATUS_DOWNLOADING) {
                latestDownloadTask.add(it)
                it.status = STATUS_PREPARE_DOWNLOAD
            }
        }
        eventVM.mutableDownloadTasks.postValue(downloadList)

        val downloadedList = dbController.findAllDownloaded().toMutableList()
        eventVM.mutableDownloadedTasks.postValue(downloadedList)
    }

    override fun add(downloadInfo: DownloadInfo): DownloadInfo {
        downloadInfo.status = STATUS_PREPARE_DOWNLOAD
        // 写入数据库
        downloadInfo.id = dbController.createOrUpdate(downloadInfo)
        changeAddUI(downloadInfo)
        return downloadInfo
    }

    override suspend fun download(
        downloadInfo: DownloadInfo,
        progressResult: (progress: Int) -> Unit
    ): Pair<Boolean, String?> = suspendCancellableCoroutine { continuation ->
        downloadInfo.status = STATUS_DOWNLOADING
        // 写入数据库
        dbController.createOrUpdate(downloadInfo)
        // 创建下载任务
        val downloadTask = DownloadTaskImpl(downloadInfo, dbController) {
            continuation.resume(it)
        }
        // 更新视图
        changeStatusUI(downloadInfo.id, STATUS_DOWNLOADING)
        downloadTask.download(this, progressResult)
    }

    override suspend fun convert(
        downloadInfo: DownloadInfo,
        progressResult: (progress: Int) -> Unit
    ): Pair<Boolean, String?> = suspendCancellableCoroutine { continuation ->
        downloadInfo.status = STATUS_CONVERT
        // 更新数据库
        dbController.createOrUpdate(downloadInfo)
        // 创建下载任务
        val downloadTask = DownloadTaskImpl(downloadInfo, dbController) {
            continuation.resume(it)
        }
        // 更新视图
        changeStatusUI(downloadInfo.id, STATUS_CONVERT)
        downloadTask.convert()
    }

    override fun pause(downloadInfo: DownloadInfo): Boolean {
        // 正在下载的不允许暂停
        if (downloadInfo.status == STATUS_DOWNLOADING) {
            return false
        }
        downloadInfo.status = STATUS_NONE
        // 更新数据库
        dbController.createOrUpdate(downloadInfo)
        // 更新视图
        changeStatusUI(downloadInfo.id, STATUS_NONE)
        return true
    }

    override fun pauseAll() {
        val downloadList = eventVM.mutableDownloadTasks.value
        downloadList?.forEach {
            if (it.status == STATUS_DOWNLOADING || it.status == STATUS_ERROR) {
                return@forEach
            }
            it.status = STATUS_NONE
            // 更新数据库
            dbController.createOrUpdate(it)
            changeStatusUI(it.id, STATUS_NONE)
        }
    }

    override fun resume(downloadInfo: DownloadInfo): DownloadInfo {
        if (downloadInfo.status == STATUS_NONE) {
            // 未开始 -> 等待中
            downloadInfo.status = STATUS_PREPARE_DOWNLOAD
        }
        // 更新数据库
        downloadInfo.id = dbController.createOrUpdate(downloadInfo)
        downloadInfo.percent = 0
        // 更新视图
        changeStatusUI(downloadInfo.id, STATUS_PREPARE_DOWNLOAD)
        return downloadInfo
    }

    override fun resumeAll() {
        val downloadList = eventVM.mutableDownloadTasks.value
        downloadList?.forEach {
            it.status = STATUS_PREPARE_DOWNLOAD
            // 更新数据库
            dbController.createOrUpdate(it)
            // 更新视图
            changeStatusUI(it.id, STATUS_PREPARE_DOWNLOAD)
        }
    }

    override fun remove(downloadInfo: DownloadInfo) {
        // 删除数据库
        val isSuccess = dbController.delete(downloadInfo)
        if (isSuccess) {
            // 更新视图
            val tempList = eventVM.mutableDownloadTasks.value
            if (tempList?.isNotEmpty() == true) {
                val iterator = tempList.iterator()
                while (iterator.hasNext()) {
                    val info = iterator.next()
                    if (info.id == downloadInfo.id) {
                        iterator.remove()
                    }
                }
            }
            eventVM.mutableDownloadTasks.postValue(tempList)
        }
    }

    override fun remove(downloadedInfo: DownloadedInfo) {
        // 删除数据库
        val isSuccess = dbController.delete(downloadedInfo)
        if (isSuccess) {
            // 清理文件
            AppUtil.getSDCardPath()?.let {
                val files = FileUtils.listFilesInDir(it)
                files.forEach { f ->
                    if (f.absolutePath.contains(downloadedInfo.name)) {
                        FileUtils.deleteFile(f)
                    }
                }
            }
            // 更新视图
            val tempList = eventVM.mutableDownloadedTasks.value
            if (tempList?.isNotEmpty() == true) {
                val iterator = tempList.iterator()
                while (iterator.hasNext()) {
                    val info = iterator.next()
                    if (info.id == downloadedInfo.id) {
                        iterator.remove()
                    }
                }
            }
            eventVM.mutableDownloadedTasks.postValue(tempList)
        }
    }

    override fun downloadSuccess(downloadInfo: DownloadInfo) {
        val tempList = eventVM.mutableDownloadTasks.value
        tempList?.forEach {
            if (it.id == downloadInfo.id) {
                it.percent = 100
                it.speed = "0 kB/s"
                it.status = STATUS_CONVERT
                dbController.createOrUpdate(it)
                return@forEach
            }
        }
        eventVM.mutableDownloadTasks.postValue(tempList)
    }

    override fun downloadFail(downloadInfo: DownloadInfo) {
        val tempList = eventVM.mutableDownloadTasks.value
        tempList?.forEach {
            if (it.id == downloadInfo.id) {
                it.status = STATUS_ERROR
                dbController.createOrUpdate(it)
            }
        }
        eventVM.mutableDownloadTasks.postValue(tempList)
    }

    override fun findAllDownloading(): List<DownloadInfo> {
        return dbController.findAllDownloading()
    }

    override fun findAllDownloaded(): List<DownloadedInfo> {
        return dbController.findAllDownloaded()
    }

    override fun getDBController(): DBController {
        return dbController
    }

    override fun getLatestDownloadTasks(): MutableList<DownloadInfo> {
        return latestDownloadTask
    }

    private fun changeStatusUI(id: Long, status: Int) {
        val tempList = eventVM.mutableDownloadTasks.value
        tempList?.forEach {
            if (it.id == id) {
                it.status = status
            }
        }
        eventVM.mutableDownloadTasks.postValue(tempList)
    }

    private fun changeAddUI(downloadInfo: DownloadInfo) {
        val tempList = eventVM.mutableDownloadTasks.value
        tempList?.add(downloadInfo)
        eventVM.mutableDownloadTasks.postValue(tempList)
    }
}