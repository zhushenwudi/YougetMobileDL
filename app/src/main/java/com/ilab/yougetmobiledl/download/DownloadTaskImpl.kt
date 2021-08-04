package com.ilab.yougetmobiledl.download

import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.chaquo.python.Python
import com.ilab.yougetmobiledl.base.eventVM
import com.ilab.yougetmobiledl.db.DBController
import com.ilab.yougetmobiledl.model.DownloadInfo
import com.ilab.yougetmobiledl.model.DownloadInfo.Companion.STATUS_CONVERT_FAIL
import com.ilab.yougetmobiledl.model.DownloadedInfo
import com.ilab.yougetmobiledl.utils.AppUtil
import dev.utils.app.ShellUtils
import dev.utils.common.FileUtils
import kotlinx.coroutines.*
import kotlin.math.round

/**
 * 下载任务实现类
 */

class DownloadTaskImpl(
    private val downloadInfo: DownloadInfo,
    private val resultResult: (res: Pair<Boolean, String?>) -> Unit
) : DownloadTask {

    enum class Status {
        DOWNLOAD,
        IDLE,
    }

    private var downloadStatus = Status.IDLE

    override fun download(
        manager: DownloadManagerImpl,
        progressResponse: (progress: Int) -> Unit
    ) {
        downloadStatus = Status.DOWNLOAD
        progressResponse(0)
        AppUtil.getSDCardPath()?.let {
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                supervisorScope {
                    launch(Dispatchers.IO) {
                        while (downloadStatus == Status.DOWNLOAD) {
                            catchLog(progressResponse)
                        }
                    }

                    launch(Dispatchers.IO) {
                        val py = Python.getInstance()
                        val res = py.getModule("download")
                            .callAttr("download", downloadInfo.url, it, downloadInfo.name)
                            .toString()
                        progressResponse(100)
                        if (res == "finish") {
                            manager.downloadSuccess(downloadInfo)
                            when (downloadInfo.type) {
                                0 -> {
                                    // 导入视频 获取封面
                                    val coverPath =
                                        AppUtil.createCover(downloadInfo.path, downloadInfo.name)
                                    generateDownloadedInfo("file://$coverPath")
                                }
                                1 -> {
                                    // B站视频 flv -> mp4
                                    convert()
                                }
                                2 -> {
                                    // B站音频 默认生成的mp4，直接生成已下载实例
                                    generateDownloadedInfo()
                                }
                                3 -> {
                                    // 优酷视频 获取封面
                                    val coverPath =
                                        AppUtil.createCover(downloadInfo.path, downloadInfo.name)
                                    generateDownloadedInfo("file://$coverPath")
                                }
                            }
                        } else {
                            manager.downloadFail(downloadInfo)
                            resultResult.invoke(Pair(false, "文件下载失败"))
                        }
                    }
                }
            }
        }
    }

    // 生成已下载实例
    private fun generateDownloadedInfo(photo: String = downloadInfo.pic) {
        AppUtil.getSDCardPath()?.let {
            // 生成已下载实例
            val path = "$it/${downloadInfo.name}.mp4"
            val pathLength = FileUtils.getFileLength(path).toDouble()
            val info = DownloadedInfo(
                name = downloadInfo.name,
                path = path,
                totalSize = FileUtils.formatByteMemorySize(2, pathLength),
                url = downloadInfo.url,
                photo = photo,
                type = downloadInfo.type
            )

            // 删除内存中的任务，从下载中数据库删除，写入已下载数据库
            val downloads = mutableListOf<DownloadInfo>()
            eventVM.mutableDownloadTasks.value?.let { it1 -> downloads.addAll(it1) }
            val iterator = downloads.iterator()
            while (iterator.hasNext()) {
                val current = iterator.next()
                if (current.url == downloadInfo.url) {
                    iterator.remove()
                    // 从 下载中 数据库删除
                    DBController.delete(current)
                    // 写入 已下载 数据库
                    DBController.createOrUpdate(info)
                    val downloadedList = eventVM.mutableDownloadedTasks.value
                    downloadedList?.add(info)
                    eventVM.mutableDownloadedTasks.postValue(downloadedList)
                }
            }
            eventVM.mutableDownloadTasks.postValue(downloads)
            resultResult.invoke(Pair(true, null))
        }
    }

    private suspend fun catchLog(progressResponse: (progress: Int) -> Unit) {
        try {
            ShellUtils.execCmd(CLEAR_LOG, false)
            delay(500)
            val result = ShellUtils.execCmd(PRINT_LOG, false)
            if (result.isSuccess) {
                val res = result.successMsg
                    .substringAfterLast("\n")
                    .substringAfterLast(TAG)
                    .trim()
                if (!res.contains(SPLIT_STR1) && !res.contains(
                        SPLIT_STR2
                    )
                ) {
                    val mPercent = res.substringBefore(' ').trim().replace("%", "")
                    val mSize = res.substringAfter('/').substringBefore(')').trim()
                    val mSpeed = res.substringAfterLast("]").trim()
                    if (mSize.isNotEmpty() && mPercent.isNotEmpty() && mSpeed.isNotEmpty()) {
                        val downloadList = eventVM.mutableDownloadTasks.value
                        progressResponse(round(mPercent.toFloat()).toInt())
                        downloadList?.forEach {
                            if (it.url == downloadInfo.url) {
                                // 下载中只需要修改内存中的数值
                                it.totalSize = mSize
                                it.percent = mPercent.split(".")[0].toInt()
                                it.speed = mSpeed
                            }
                        }
                        // 数据驱动界面
                        eventVM.mutableDownloadTasks.postValue(downloadList)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun convert() {
        AppUtil.getSDCardPath()?.let {
            // flv -> mp4
            val src = "$it/${downloadInfo.name}.flv"
            val dest = "$it/${downloadInfo.name}.mp4"
            val cmd = "-i $src -vcodec copy -acodec copy $dest"
            FileUtils.deleteFile(dest)
            FFmpegKit.executeAsync(cmd,
                { session ->
                    if (session.returnCode.value == ReturnCode.SUCCESS) {
                        // 删除 flv 文件
                        FileUtils.deleteFile(src)
                        // 生成已下载实例
                        generateDownloadedInfo()
                    } else {
                        val downloadList = eventVM.mutableDownloadTasks.value
                        downloadList?.forEach { info ->
                            if (info.id == downloadInfo.id) {
                                info.percent = 0
                                info.speed = "0 kB/s"
                                info.status = STATUS_CONVERT_FAIL
                                eventVM.mutableDownloadTasks.postValue(downloadList)
                                DBController.createOrUpdate(info)
                                return@forEach
                            }
                        }
                        resultResult.invoke(Pair(false, "格式转换失败"))
                    }
                }, {}) {}
        }
    }

    companion object {
        const val CLEAR_LOG = "logcat -c"
        const val PRINT_LOG = "logcat -d -s python.stdout"
        const val TAG = "python.stdout:"
        const val SPLIT_STR1 = "main"
        const val SPLIT_STR2 = "begin"
    }
}