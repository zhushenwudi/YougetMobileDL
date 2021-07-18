package com.ilab.yougetmobiledl.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaquo.python.Python
import com.ilab.yougetmobiledl.base.App
import com.ilab.yougetmobiledl.model.DownloadInfo
import com.ilab.yougetmobiledl.model.DownloadInfo_
import com.ilab.yougetmobiledl.utils.AppUtil
import dev.utils.app.ShellUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel : ViewModel() {
    private val downloadBox = App.boxStore.boxFor(DownloadInfo::class.java)

    enum class Status {
        DOWNLOAD,
        IDLE,
        SUCCESS,
        SDCARD_NOT_FOUND,
        URL_ERROR
    }

    val downloadStatus = MutableLiveData(Status.IDLE)
    val downloadInfo = MutableLiveData<DownloadInfo?>()

    fun getVideoList(url: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val py = Python.getInstance()
            ShellUtils.execCmd(CLEAR_LOG, false)
            val result = py.getModule("download").callAttr("getInfo", url).toString()
            if (result == "finish") {
                val log = ShellUtils.execCmd(PRINT_LOG, false)
                if (log.isSuccess) {
                    val res = log.successMsg
                    val title = res.substringAfter(TITLE).substringBefore("\n").trim()
                    var size = ""
                    var format = ""
                    res.substringAfter("[ DEFAULT ]")
                        .substringBefore("[URL]")
                        .split("\n")
                        .forEach {
                            if (!it.endsWith(TAG) && !it.contains(DEFAULT_LINE)) {
                                val line = it.substringAfter(TAG).trim()
                                if (line.contains(SIZE)) {
                                    size = line.substringAfter(SIZE).substringBefore(" (").trim()
                                }
                                if (line.contains(FORMAT)) {
                                    format = line.substringAfterLast(FORMAT).trim()
                                }
                            }
                        }
                    val info =
                        DownloadInfo(name = title, totalSize = size, format = format, url = url)
                    val videoList =
                        downloadBox.query().equal(DownloadInfo_.name, info.name).build().find()
                    if (videoList.isEmpty()) {
                        downloadBox.put(info)
                        Log.e("aaa", "$title - $size - $format")
                        downloadInfo.postValue(info)
                    } else {
                        downloadInfo.postValue(info)
                        Log.e("aaa", "已经在缓存列表了")
                    }
                }
            }
        }
    }

    fun download(url: String) {
//        downloadBox.put(DownloadInfo(url = url))

        downloadStatus.value = Status.DOWNLOAD
        AppUtil.getSDCardPath()?.let {
            viewModelScope.launch(Dispatchers.Default) {
                while (downloadStatus.value == Status.DOWNLOAD) {
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
                                val percent = res.substringBefore(' ').trim()
                                val totalSize = res.substringAfter('/').substringBefore(')').trim()
                                val speed = res.substringAfterLast("]").trim()
                                withContext(Dispatchers.Main) {
                                    if (totalSize.isNotEmpty() && percent.isNotEmpty() && speed.isNotEmpty()) {
                                        downloadInfo.value =
                                            DownloadInfo(
                                                url = url,
                                                totalSize = totalSize,
                                                percent = percent,
                                                speed = speed
                                            )
                                    }
                                }
                            }
                        } else {
                            Log.e("aaa", "failed")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                withContext(Dispatchers.Main) {
                    downloadInfo.value = null
                }
            }

            viewModelScope.launch(Dispatchers.Default) {
                val py = Python.getInstance()
                val result = py.getModule("download").callAttr("download", url, it).toString()
                val status = if (result == "finish") {
                    Status.SUCCESS
                } else {
                    Status.URL_ERROR
                }
                withContext(Dispatchers.Main) {
                    downloadStatus.value = status
                }
            }
        } ?: (false.also { downloadStatus.value = Status.SDCARD_NOT_FOUND })
    }

    companion object {
        const val CLEAR_LOG = "logcat -c"
        const val PRINT_LOG = "logcat -d -s python.stdout"
        const val TAG = "python.stdout:  "
        const val DEFAULT_LINE = "___________"
        const val TITLE = "title:"
        const val SIZE = "size:"
        const val FORMAT = "format:"
        const val SPLIT_STR1 = "main"
        const val SPLIT_STR2 = "begin"
    }
}