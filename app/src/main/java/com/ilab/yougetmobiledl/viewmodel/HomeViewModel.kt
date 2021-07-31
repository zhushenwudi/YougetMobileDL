package com.ilab.yougetmobiledl.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ilab.yougetmobiledl.base.BaseViewModel
import com.ilab.yougetmobiledl.base.eventVM
import com.ilab.yougetmobiledl.model.Bangumi
import com.ilab.yougetmobiledl.model.DownloadInfo
import com.ilab.yougetmobiledl.model.Episode
import com.ilab.yougetmobiledl.network.apiService
import com.ilab.yougetmobiledl.network.request
import com.ilab.yougetmobiledl.network.requestNoCheck
import com.ilab.yougetmobiledl.utils.AppUtil
import dev.utils.LogPrintUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.await
import retrofit2.awaitResponse

class HomeViewModel : BaseViewModel() {
    enum class Status {
        FIND_VIDEO_INFO,
        FIND_VIDEO_ERROR,
        PARSE_VIDEO_ERROR,
        PARSE_AUDIO_ERROR,
        TIMEOUT_ERROR,
        READY_FOR_DOWNLOAD,
        ALREADY_DOWNLOAD,
        ONLY_VIP,
        CLOSE_DIALOG
    }

    val downloadStatus = MutableLiveData(Status.CLOSE_DIALOG)
    val downloadInfo = MutableLiveData<DownloadInfo>()
    val chooseEP = MutableLiveData<Bangumi>()

    fun handleURL(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            downloadStatus.postValue(Status.FIND_VIDEO_INFO)
            try {
                val html = apiService.getHasCurrentVideo(url).awaitResponse()
                checkPlatform(html.raw().request().url().toString())
            } catch (e: Exception) {
                e.printStackTrace()
                postEvent(Status.FIND_VIDEO_ERROR)
            }
        }
    }

    private suspend fun checkPlatform(rawUrl: String) {
        var url = rawUrl.replace("m.bilibili.com", "www.bilibili.com")
        var hasPart = false
        if ('?' in url) {
            val paramsMap = AppUtil.getUrlParamsMap(url)
            hasPart = paramsMap.containsKey("p")
            url = url.substringBefore('?')
            if (hasPart) {
                paramsMap["p"]?.let { url += ("?p=$it") }
            }
        }
        when {
            url.matches(Regex(MATCH_AV)) -> {
                // AV
                url = url.substringBefore('?')
                if (checkURL(url)) {
                    getVideoInfo(
                        url = url,
                        aid = url.getVideoKey("av").toInt(),
                        bvid = null,
                        hasPart = hasPart
                    )
                }
            }
            url.matches(Regex(MATCH_BV)) -> {
                // BV
                url = url.substringBefore('?')
                if (checkURL(url)) {
                    getVideoInfo(
                        url = url,
                        aid = null,
                        bvid = url.getVideoKey("BV"),
                        hasPart = hasPart
                    )
                }
            }
            url.matches(Regex(MATCH_AUDIO)) -> {
                // 音频
                url = url.substringBefore('?')
                if (checkURL(url)) {
                    getAudioInfo(url)
                }
            }
            url.matches(Regex(MATCH_BANGUMI_SS)) -> {
                // 番剧集，弹窗显示选择分P
                if (checkURL(url)) {
                    getBangumiInfo(
                        seasonId = url.getVideoKey("ss").toInt(),
                        epId = null
                    )
                }
            }
            url.matches(Regex(MATCH_BANGUMI_EP)) -> {
                // 单集番剧
                if (checkURL(url)) {
                    getBangumiInfo(
                        seasonId = null,
                        epId = url.getVideoKey("ep").toInt()
                    )
                }
            }
            else -> {

            }
        }
        LogPrintUtils.e(url)
    }

    private fun checkURL(url: String): Boolean {
        // 判断内存中已下载和下载中是否包括该 url
        if (eventVM.mutableDownloadedTasks.value?.any { it.url == url } == true) {
            postEvent(Status.ALREADY_DOWNLOAD)
            return false
        }
        if (eventVM.mutableDownloadTasks.value?.any { it.url == url } == true) {
            postEvent(Status.ALREADY_DOWNLOAD)
            return false
        }
        return true
    }

    // 获取音频信息和流
    private suspend fun getAudioInfo(url: String) {
        val sid = url.getVideoKey("au").toInt()
        try {
            val audio = apiService.getAudioInfo(sid).await()
            val audioStream = apiService.getAudioStream(sid).await()
            if (audio.code == 0 && audioStream.code == 0) {
                val info = DownloadInfo::class.java.newInstance()
                info.type = 2
                info.url = url
                info.path = "${AppUtil.getSDCardPath()}/${info.name}.mp4"
                audio.data.title?.let { info.name = it }
                audio.data.cover?.let { info.pic = it }
                audioStream.data.size?.let { info.totalSize = parseSize(it.toString()) }
                postEvent(Status.READY_FOR_DOWNLOAD)
                downloadInfo.postValue(info)
            } else {
                postEvent(Status.PARSE_AUDIO_ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            postEvent(Status.PARSE_AUDIO_ERROR)
        }
    }

    // 获取AV/BV视频信息
    private fun getVideoInfo(url: String, aid: Int?, bvid: String?, hasPart: Boolean = false) {
        request({
            apiService.getVideoInfo(bvid = bvid, aid = aid)
        }, {
            it.run {
                val info = DownloadInfo::class.java.newInstance()
                info.name = replaceWindows(title)
                info.bvid = bvid
                info.cid = cid
                info.videoPart = videos
                info.hasPart = hasPart
                info.pic = pic
                info.url = url
                getHighDigitalStream(info)
            }
        }, {
            postEvent(Status.PARSE_VIDEO_ERROR)
        })
    }

    // 获取AV/BV高清视频流
    private fun getHighDigitalStream(info: DownloadInfo) {
        requestNoCheck({
            apiService.getHighDigitalVideoStream(cid = info.cid)
        }, {
            if (it.isSuccess()) {
                info.path = "${AppUtil.getSDCardPath()}/${info.name}.${it.format}"
                info.totalSize = parseSize(it.durl[0].size.toString())
                postEvent(Status.READY_FOR_DOWNLOAD)
                downloadInfo.postValue(info)
            } else {
                postEvent(Status.PARSE_VIDEO_ERROR)
            }
        })
    }

    // 获取番剧视频信息
    private suspend fun getBangumiInfo(seasonId: Int?, epId: Int?) {
        try {
            val bangumi = apiService.getBangumiInfo(seasonId, epId).await()
            if (bangumi.code == 0) {
                if (seasonId != null) {
                    bangumi.result?.run { chooseEP.postValue(this) }
                    downloadStatus.postValue(Status.CLOSE_DIALOG)
                } else {
                    bangumi.result?.episodes
                        ?.filter { ep -> ep.id == epId }
                        ?.let { getBangumiHighDigitalStream(it) }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            postEvent(Status.PARSE_VIDEO_ERROR)
        }
    }

    // 获取番剧高清视频流
    fun getBangumiHighDigitalStream(episodes: List<Episode>) {
        viewModelScope.launch(Dispatchers.IO) {
            episodes.forEach { ep ->
                try {
                    val dualStream = apiService.getBangumiVideoStream(ep.id).await()
                    if (dualStream.isSuccess() && dualStream.result != null) {
                        val info = DownloadInfo::class.java.newInstance()
                        info.name = replaceWindows(ep.long_title)
                        info.epid = ep.id
                        info.videoPart = episodes.size
                        info.hasPart = true
                        info.pic = ep.cover
                        info.url = ep.share_url
                        info.path =
                            "${AppUtil.getSDCardPath()}/${info.name}.${dualStream.result.format}"
                        info.totalSize = parseSize(dualStream.result.durl[0].size.toString())
                        postEvent(Status.READY_FOR_DOWNLOAD)
                        downloadInfo.postValue(info)
                    } else {
                        if (dualStream.message == "大会员专享限制") {
                            postEvent(Status.ONLY_VIP)
                        } else {
                            postEvent(Status.PARSE_VIDEO_ERROR)
                        }
                    }
                } catch (e: Exception) {
                    postEvent(Status.PARSE_VIDEO_ERROR)
                }
            }
        }
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

    private fun String.getVideoKey(after: String): String {
        return substringBefore("?").substringAfterLast(after)
    }

    companion object {
        const val MATCH_AV = "https?://(www\\.)?bilibili\\.com/video/(av(\\d+))"
        const val MATCH_BV = "https?://(www\\.)?bilibili\\.com/video/(BV(\\S+))"
        const val MATCH_AUDIO = "https?://(www\\.)?bilibili\\.com/audio/au(\\d+)"
        const val MATCH_BANGUMI_SS = "https?://(www\\.)?bilibili\\.com/bangumi/play/ss(\\d+)"
        const val MATCH_BANGUMI_EP = "https?://(www\\.)?bilibili\\.com/bangumi/play/ep(\\d+)"
    }
}