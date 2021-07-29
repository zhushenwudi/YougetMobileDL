package com.ilab.yougetmobiledl.db

import com.ilab.yougetmobiledl.base.App
import com.ilab.yougetmobiledl.model.DownloadInfo
import com.ilab.yougetmobiledl.model.DownloadInfo_
import com.ilab.yougetmobiledl.model.DownloadedInfo
import com.ilab.yougetmobiledl.model.DownloadedInfo_

class DBController : DownloadDBController {
    private val downloadBox = App.boxStore.boxFor(DownloadInfo::class.java)
    private val downloadedBox = App.boxStore.boxFor(DownloadedInfo::class.java)

    override fun findAllDownloading(): List<DownloadInfo> {
        return downloadBox.all
    }

    override fun findAllDownloaded(): List<DownloadedInfo> {
        return downloadedBox.all
    }

    override fun findDownloadedInfoById(id: Long): DownloadedInfo? {
        return downloadedBox.query().equal(DownloadedInfo_.id, id).build().findFirst()
    }

    override fun findDownloadInfoById(id: Long): DownloadInfo? {
        return downloadBox.query().equal(DownloadInfo_.id, id).build().findFirst()
    }

    override fun createOrUpdate(downloadInfo: DownloadInfo): Long {
        return downloadBox.put(downloadInfo)
    }

    override fun createOrUpdate(downloadedInfo: DownloadedInfo): Long {
        return downloadedBox.put(downloadedInfo)
    }

    override fun delete(downloadInfo: DownloadInfo): Boolean {
        return downloadBox.remove(downloadInfo.id)
    }

    override fun delete(downloadedInfo: DownloadedInfo): Boolean {
        return downloadedBox.remove(downloadedInfo.id)
    }
}