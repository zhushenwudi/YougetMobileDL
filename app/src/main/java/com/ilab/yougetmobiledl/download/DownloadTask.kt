package com.ilab.yougetmobiledl.download

/**
 * 下载任务控制类
 */

interface DownloadTask {
    fun download(
        manager: DownloadManagerImpl,
        progressResponse: (progress: Int) -> Unit
    )

    fun convert()
}