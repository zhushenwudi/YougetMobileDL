package com.ilab.yougetmobiledl.download

/**
 * 下载器管理类
 */

import com.ilab.yougetmobiledl.model.DownloadInfo
import com.ilab.yougetmobiledl.model.DownloadedInfo

interface DownloadManager {
    /**
     * 新增
     */
    fun add(downloadInfo: DownloadInfo): DownloadInfo

    /**
     * 下载
     */
    suspend fun download(
        downloadInfo: DownloadInfo,
        progressResult: (progress: Int) -> Unit
    ): Pair<Boolean, String?>

    suspend fun convert(
        downloadInfo: DownloadInfo,
        progressResult: (progress: Int) -> Unit
    ): Pair<Boolean, String?>

    /**
     * 暂停
     */
    fun pause(downloadInfo: DownloadInfo): Boolean

    /**
     * 暂停所有
     */
    fun pauseAll()

    /**
     * 恢复
     */
    fun resume(downloadInfo: DownloadInfo): DownloadInfo

    /**
     * 恢复所有
     */
    fun resumeAll()

    /**
     * 删除正在下载的
     */
    fun remove(downloadInfo: DownloadInfo)

    /**
     * 删除已经下载的
     */
    fun remove(downloadedInfo: DownloadedInfo)

    /**
     * 下载完成
     */
    fun downloadSuccess(downloadInfo: DownloadInfo)

    /**
     * 下载失败
     */
    fun downloadFail(downloadInfo: DownloadInfo)

    /**
     * 查找所有正在下载中的列表
     */
    fun findAllDownloading(): List<DownloadInfo>

    /**
     * 查找所有已下载的列表
     */
    fun findAllDownloaded(): List<DownloadedInfo>

    /**
     * 获取上一次关闭程序前准备下载和正在下载的任务
     */
    fun getLatestDownloadTasks(): List<DownloadInfo>
}