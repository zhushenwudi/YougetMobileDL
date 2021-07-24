package com.ilab.yougetmobiledl.db

import com.ilab.yougetmobiledl.base.App
import com.ilab.yougetmobiledl.model.DownloadInfo
import com.ilab.yougetmobiledl.model.DownloadInfo_
import com.ilab.yougetmobiledl.model.VideoInfo
import com.ilab.yougetmobiledl.model.VideoInfo_

class DBController : DownloadDBController {
    private val downloadBox = App.boxStore.boxFor(DownloadInfo::class.java)
    private val downloadedBox = App.boxStore.boxFor(VideoInfo::class.java)

    override fun findAllDownloading(): List<DownloadInfo> {
        return downloadBox.all
    }

    override fun findAllDownloaded(): List<VideoInfo> {
        return downloadedBox.all
    }

    override fun findDownloadedInfoById(id: Long): VideoInfo? {
        return downloadedBox.query().equal(VideoInfo_.id, id).build().findFirst()
    }

    override fun findDownloadInfoById(id: Long): DownloadInfo? {
        return downloadBox.query().equal(DownloadInfo_.id, id).build().findFirst()
    }

    override fun createOrUpdate(downloadInfo: DownloadInfo): Long {
        return downloadBox.put(downloadInfo)
    }

    override fun createOrUpdate(videoInfo: VideoInfo): Long {
        return downloadedBox.put(videoInfo)
    }

    override fun delete(downloadInfo: DownloadInfo) {
        downloadBox.remove(downloadInfo.id)
    }

    override fun delete(videoInfo: VideoInfo) {
        downloadedBox.remove(videoInfo.id)
    }
}