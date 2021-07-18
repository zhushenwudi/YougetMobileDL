package com.ilab.yougetmobiledl.service

import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.chaquo.python.Python
import com.ilab.yougetmobiledl.base.App
import com.ilab.yougetmobiledl.base.eventVM
import com.ilab.yougetmobiledl.model.DownloadInfo
import com.ilab.yougetmobiledl.model.DownloadInfo_
import com.ilab.yougetmobiledl.model.VideoInfo
import com.ilab.yougetmobiledl.utils.AppUtil
import dev.utils.app.ShellUtils
import dev.utils.app.image.BitmapUtils
import dev.utils.app.image.ImageUtils
import dev.utils.common.FileUtils
import kotlinx.coroutines.*

class DownloadTask(
    private val downloadInfo: DownloadInfo
) {
    private val downloadBox = App.boxStore.boxFor(DownloadInfo::class.java)
    private val downloadedBox = App.boxStore.boxFor(VideoInfo::class.java)
    private var downloadStatus = Status.IDLE

    fun getInfo(): DownloadInfo {
        return downloadInfo
    }

    suspend fun onDownload(url: String) {
        // 抓 Log，获取 percent 和 totalSize
        downloadStatus = Status.DOWNLOAD
        AppUtil.getSDCardPath()?.let {
            supervisorScope {
                launch(Dispatchers.IO) {
                    while (downloadStatus == Status.DOWNLOAD) {
                        try {
                            ShellUtils.execCmd(CLEAR_LOG, false)
                            delay(500)
                            val result = ShellUtils.execCmd(PRINT_LOG, false)
                            if (result.isSuccess) {
                                val res = result.successMsg
                                    .substringAfterLast("\n")
                                    .substringAfterLast(TAG)
                                    .trim()
                                if (!res.contains(SPLIT_STR1) && !res.contains(SPLIT_STR2)) {
                                    val mPercent = res.substringBefore(' ').trim()
                                    val mSize = res.substringAfter('/').substringBefore(')').trim()
                                    val mSpeed = res.substringAfterLast("]").trim()
                                    if (mSize.isNotEmpty() && mPercent.isNotEmpty() && mSpeed.isNotEmpty()) {
                                        val task = eventVM.currentDownloadTask.value
                                        task?.downloadInfo?.run {
                                            totalSize = mSize
                                            percent = mPercent
                                            speed = mSpeed
                                        }
                                        eventVM.currentDownloadTask.postValue(task)
                                    }
                                }
                            } else {
                                Log.e("aaa", "get log fail")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                launch(Dispatchers.IO) {
                    val py = Python.getInstance()
                    val result = py.getModule("download").callAttr("download", url, it).toString()
                    if (result == "finish") {
                        downloadStatus = Status.SUCCESS
                        onDownloadSuccess()
                        return@launch
                    }
                    downloadStatus = Status.URL_ERROR
                    onDownloadFailed()
                }
            }
        } ?: (false.also { downloadStatus = Status.SDCARD_NOT_FOUND })
    }

    fun onDelete() {
        // 从数据库删除
        removeFromDB()
        // 清理缓存文件
        AppUtil.getSDCardPath()?.let {
            val files = FileUtils.listFilesInDir(it)
            files.forEach { f ->
                if (f.absolutePath.contains(downloadInfo.name)) {
                    FileUtils.deleteFile(f)
                }
            }
        }
    }

    private fun onDownloadSuccess() {
        var task = eventVM.currentDownloadTask.value
        task?.downloadInfo?.run {
            percent = "100%"
            speed = "0 kB/s"
            status = 4
        }
        eventVM.currentDownloadTask.postValue(task)
        AppUtil.getSDCardPath()?.let {
            // flv -> mp4
            val src = "$it/${downloadInfo.name}.flv"
            val dest = "$it/${downloadInfo.name}.mp4"
            val cmd = "-i $src -vcodec copy -acodec copy $dest"
            FFmpegKit.executeAsync(cmd,
                { session ->
                    val returnCode: ReturnCode = session.returnCode
                    if (returnCode.value == ReturnCode.SUCCESS) {
                        Log.e("aaa", "转换成功")
                        // 删除 flv 文件
                        FileUtils.deleteFile(src)
                        // 从数据库删除
                        removeFromDB()
                        // 下载中 跳到 已下载
                        val path = "$it/${downloadInfo.name}.mp4"
                        val info = VideoInfo(
                            name = downloadInfo.name,
                            path = path,
                            totalSize = downloadInfo.totalSize
                        )
                        BitmapUtils.getVideoThumbnail(path)?.let { bitmap ->
                            val photo =
                                AppUtil.getSDCardPath() + "/temp/" + downloadInfo.name.substringBeforeLast(
                                    '.'
                                ) + ".png"
                            if (ImageUtils.saveBitmapToSDCardPNG(bitmap, photo)) {
                                info.photo = photo
                            }
                        }
                        downloadedBox.put(info)
                        task = eventVM.currentDownloadTask.value
                        task?.downloadInfo?.run {
                            percent = "100%"
                            speed = "0 kB/s"
                            status = 5
                        }
                        eventVM.currentDownloadTask.postValue(task)
                    }
                }, {}) {}
        }
    }

    private fun onDownloadFailed() {
        // 下载失败 写入数据库
        val video =
            downloadBox.query().equal(DownloadInfo_.name, downloadInfo.name).build().findFirst()
        video?.run {
            status = 3
            downloadBox.put(this)
        }

        val task = eventVM.currentDownloadTask.value
        task?.downloadInfo?.run {
            percent = ""
            speed = ""
            status = 3
        }
        eventVM.currentDownloadTask.postValue(task)
    }

    private fun removeFromDB() {
        val video =
            downloadBox.query().equal(DownloadInfo_.name, downloadInfo.name).build().findFirst()
        video?.run {
            downloadBox.remove(this)
        }
    }

    enum class Status {
        DOWNLOAD,
        IDLE,
        SUCCESS,
        SDCARD_NOT_FOUND,
        URL_ERROR
    }

    companion object {
        const val CLEAR_LOG = "logcat -c"
        const val PRINT_LOG = "logcat -d -s python.stdout"
        const val TAG = "python.stdout:"
        const val SPLIT_STR1 = "main"
        const val SPLIT_STR2 = "begin"
    }

}