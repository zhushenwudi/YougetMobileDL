package com.ilab.yougetmobiledl.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaquo.python.Python
import com.ilab.yougetmobiledl.model.DownloadInfo
import com.ilab.yougetmobiledl.utils.AppUtil
import dev.utils.app.ShellUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel : ViewModel() {
    enum class Status {
        DOWNLOAD,
        IDLE,
        SUCCESS,
        SDCARD_NOT_FOUND,
        URL_ERROR
    }

    val downloadStatus = MutableLiveData(Status.IDLE)
    val downloadInfo = MutableLiveData<DownloadInfo>()

    fun download(url: String) {
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
                                            DownloadInfo(url, totalSize, percent, speed)
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
        const val TAG = "python.stdout:"
        const val SPLIT_STR1 = "main"
        const val SPLIT_STR2 = "begin"
    }
}