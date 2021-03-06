package com.ilab.yougetmobiledl.db

/**
 * 数据库控制类
 */

import com.ilab.yougetmobiledl.model.DownloadInfo
import com.ilab.yougetmobiledl.model.DownloadedInfo

interface DownloadDBController {
    /**
     * 查找 下载中 的全部信息
     */
    fun findAllDownloading(): List<DownloadInfo>

    /**
     * 查找 已下载 的全部信息
     */
    fun findAllDownloaded(): List<DownloadedInfo>

    /**
     * 根据 id 获取 已下载 信息
     */
    fun findDownloadedInfoById(id: Long): DownloadedInfo?

    /**
     * 根据 绝对路径 获取 已下载 信息
     */
    fun findDownloadedInfoByPath(path: String): DownloadedInfo?

    /**
     * 根据 id 获取 下载中 信息
     */
    fun findDownloadInfoById(id: Long): DownloadInfo?

    /**
     * 写入或更新 下载中 数据
     */
    fun createOrUpdate(downloadInfo: DownloadInfo): Long

    /**
     * 写入或更新 已下载 数据
     */
    fun createOrUpdate(downloadedInfo: DownloadedInfo): Long

    /**
     * 删除 下载中 数据
     */
    fun delete(downloadInfo: DownloadInfo): Boolean

    /**
     * 删除 已下载 数据
     */
    fun delete(downloadedInfo: DownloadedInfo): Boolean
}