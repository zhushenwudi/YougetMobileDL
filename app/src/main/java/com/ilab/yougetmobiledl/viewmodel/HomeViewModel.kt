package com.ilab.yougetmobiledl.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ilab.yougetmobiledl.base.BaseViewModel
import com.ilab.yougetmobiledl.base.eventVM
import com.ilab.yougetmobiledl.model.*
import com.ilab.yougetmobiledl.network.apiService
import com.ilab.yougetmobiledl.network.request
import com.ilab.yougetmobiledl.network.requestNoCheck
import com.ilab.yougetmobiledl.utils.AppUtil
import kotlinx.coroutines.*

class HomeViewModel : BaseViewModel() {
    enum class Status {
        FIND_VIDEO_INFO,
        FIND_VIDEO_ERROR,
        PARSE_VIDEO_ERROR,
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
                postEvent(Status.ALREADY_DOWNLOAD)
                return@launch
            }
            if (eventVM.mutableDownloadTasks.value?.any { it.url == url } == true) {
                postEvent(Status.ALREADY_DOWNLOAD)
                return@launch
            }

            // 没有继续走
            try {
                withTimeout(10000) {
                    downloadStatus.postValue(Status.FIND_VIDEO_INFO)
                    requestNoCheck({
                        apiService.getHasCurrentVideo(url)
                    }, {
                        val rawUrl = it.raw().request().url().toString()
                        checkPlatform(rawUrl)
                    }, {
                        postEvent(Status.FIND_VIDEO_ERROR)
                    })
                }
            } catch (e: TimeoutCancellationException) {
                postEvent(Status.TIMEOUT_ERROR)
            }
        }
    }

    private fun checkPlatform(rawUrl: String) {
        Log.e("aaa", "rawUrl: $rawUrl")
        var url = rawUrl.replace("m.bilibili.com", "www.bilibili.com")
        var hasPart = false
        if ('?' in rawUrl) {
            val paramsMap = AppUtil.getUrlParamsMap(rawUrl)
            hasPart = paramsMap.containsKey("p")
            url = rawUrl.substringBefore('?')
            if (hasPart) {
                paramsMap["p"]?.let { url += ("?p=$it") }
            }
        }
        Log.e("aaa", url)
        when {
            "bilibili.com" in url -> {
                // b站长链
                when {
                    "av" in url -> {
                        // av视频
                        getVideoInfo(
                            url = url,
                            aid = url.getVideoKey().toInt(),
                            bvid = null,
                            hasPart = hasPart
                        )
                    }
                    "BV" in url -> {
                        // bv视频
                        getVideoInfo(
                            url = url,
                            aid = null,
                            bvid = url.getVideoKey(),
                            hasPart = hasPart
                        )
                    }
                    else -> {
                        // 其他
                        val downloadInfo = DownloadInfo::class.java.newInstance()
                        downloadInfo.url = url
                    }
                }
            }
            else -> {
                // 其他平台视频
            }
        }
    }

    private fun getVideoInfo(url: String, aid: Int?, bvid: String?, hasPart: Boolean = false) {
        request({
            apiService.getVideoInfo(bvid = bvid, aid = aid)
        }, {
            val downloadInfo = DownloadInfo::class.java.newInstance()
            downloadInfo.name = replaceWindows(it.title)
            downloadInfo.bvid = it.bvid
            downloadInfo.cid = it.cid
            downloadInfo.videoPart = it.videos
            downloadInfo.hasPart = hasPart
            downloadInfo.pic = it.pic
            downloadInfo.url = url
            getHighDigitalStream(downloadInfo)
        }, {
            postEvent(Status.PARSE_VIDEO_ERROR)
        })
    }

    private fun getHighDigitalStream(info: DownloadInfo) {
        requestNoCheck({
            apiService.getHighDigitalVideoStream(cid = info.cid)
        }, {
            if (it.isSuccess()) {
                info.path = "${AppUtil.getSDCardPath()}/${info.name}.${it.format}"
                info.totalSize = parseSize(it.durl[0].size.toString())
                postEvent(Status.READY_FOR_DOWNLOAD)
                downloadInfo.postValue(info)
                Log.d("aaa", info.toString())
            } else {
                postEvent(Status.PARSE_VIDEO_ERROR)
            }
        })
    }

    private fun parseSize(value: String): String {
        val mb = value.toDouble() / (1024 * 1024)
        return "%.2f".format(mb) + " MB"
    }

    private fun postEvent(status: Status) {
        viewModelScope.launch(Dispatchers.Default) {
            downloadStatus.postValue(status)
            delay(2000)
            downloadStatus.postValue(Status.CLOSE_DIALOG)
        }
    }

    private fun replaceWindows(str: String): String {
        return str.replace("\\", "-").replace("/", "-").replace(":", "-").replace("*", "-")
            .replace("?", "-").replace("\"", "-").replace("<", "-").replace(">", "-")
            .replace("|", "-").replace(" ", "")
    }

    private fun String.getVideoKey(): String {
        return substringAfter("video/").substringBefore("?")
    }
}