package com.ilab.yougetmobiledl.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.chaquo.python.Python
import com.ilab.yougetmobiledl.base.App.Companion.volley
import com.ilab.yougetmobiledl.base.eventVM
import com.ilab.yougetmobiledl.model.*
import com.ilab.yougetmobiledl.utils.AppUtil
import kotlinx.coroutines.*


class HomeViewModel : ViewModel() {
    enum class Status {
        FIND_VIDEO_INFO,
        FIND_VIDEO_ERROR,
        PARSE_VIDEO_INFO,
        PARSE_VIDEO_ERROR,
        MATCH_ERROR,
        TIMEOUT_ERROR,
        READY_FOR_DOWNLOAD,
        ALREADY_DOWNLOAD,
        CLOSE_DIALOG
    }

    val downloadStatus = MutableLiveData(Status.CLOSE_DIALOG)
    val downloadInfo = MutableLiveData<DownloadInfo>()

    fun getVideoList(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // 判断内存中已下载和下载中是否包括该 url
            if (eventVM.mutableDownloadedTasks.value?.any { it.url == url } == true) {
                downloadStatus.postValue(Status.ALREADY_DOWNLOAD)
                closeDialog()
                return@launch
            }
            if (eventVM.mutableDownloadTasks.value?.any { it.url == url } == true) {
                downloadStatus.postValue(Status.ALREADY_DOWNLOAD)
                closeDialog()
                return@launch
            }
            // 没有继续走
            try {
                withTimeout(120000) {
                    downloadStatus.postValue(Status.FIND_VIDEO_INFO)
                    val py = Python.getInstance()
                    val defaultInfo =
                        py.getModule("default").callAttr("getDefaultDownloadInfo", url).toString()
                    if (defaultInfo == "error") {
                        downloadStatus.postValue(Status.FIND_VIDEO_ERROR)
                        closeDialog()
                        return@withTimeout
                    }
                    downloadStatus.postValue(Status.PARSE_VIDEO_INFO)
                    val matchFormat = Regex(REGEX_FORMAT).findAll(defaultInfo)
                    val matchSize = Regex(REGEX_SIZE).findAll(defaultInfo)
                    if (matchFormat.any() && matchSize.any()) {
                        delay(1000)
                        val jsonInfo = py.getModule("getJson").callAttr("getJson", url).toString()
                        if (jsonInfo == "error") {
                            downloadStatus.postValue(Status.PARSE_VIDEO_ERROR)
                            closeDialog()
                            return@withTimeout
                        }
                        downloadStatus.postValue(Status.READY_FOR_DOWNLOAD)
                        val json = AppUtil.fromJson<StreamInfo>(jsonInfo)
                        json?.let {
                            val info = DownloadInfo(
                                name = replaceWindows(json.title),
                                totalSize = parseSize(matchSize.first().value),
                                format = matchFormat.first().value,
                                url = json.url.substringBefore("?")
                            )
                            getCoverPic(info)
                        }
                        closeDialog()
                    } else {
                        downloadStatus.postValue(Status.MATCH_ERROR)
                        closeDialog()
                        return@withTimeout
                    }
                }
            } catch (e: TimeoutCancellationException) {
                downloadStatus.postValue(Status.TIMEOUT_ERROR)
                closeDialog()
            }
        }
    }

    private fun getCoverPic(info: DownloadInfo) {
        if ("BV" in info.url) {
            val url = COVER_URL + info.url.substringAfter("BV")
            val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
                { response ->
                    if (response.getInt("code") == 0) {
                        val pic = response.getJSONObject("data").getString("pic")
                        info.pic = pic
                        downloadInfo.postValue(info)
                    }
                }
            ) {
                downloadInfo.postValue(info)
            }
            volley.add(jsonObjectRequest)
        } else {
            downloadInfo.postValue(info)
        }
    }

    private fun parseSize(value: String): String {
        val mb = value.toDouble() / (1024 * 1024)
        return "%.2f".format(mb) + " MB"
    }

    private suspend fun closeDialog() {
        delay(2000)
        downloadStatus.postValue(Status.CLOSE_DIALOG)
    }

    private fun replaceWindows(str: String): String {
        return str.replace("\\", "-").replace("/", "-").replace(":", "-").replace("*", "-")
            .replace("?", "-").replace("\"", "-").replace("<", "-").replace(">", "-")
            .replace("|", "-")
    }

    companion object {
        const val REGEX_FORMAT = "(?<=--format=)(.*?)(?= \\[URL])"
        const val REGEX_SIZE = "(?<=MiB \\()(.*?)(?= bytes)"
        const val COVER_URL = "https://api.bilibili.com/x/web-interface/view?bvid="
    }
}