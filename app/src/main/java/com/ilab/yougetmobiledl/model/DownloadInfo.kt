package com.ilab.yougetmobiledl.model

import android.os.Parcelable
import com.ilab.yougetmobiledl.utils.AppUtil
import com.ilab.yougetmobiledl.utils.NoArg
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import kotlinx.android.parcel.Parcelize

@NoArg
@Parcelize
@Entity
data class DownloadInfo(
    @Id
    var id: Long = 0,

    var name: String = "",
    var path: String = "${AppUtil.getSDCardPath()}/$name.flv",
    var totalSize: String = "",
    var percent: Int = 0,
    var speed: String = "",
    var status: Int = STATUS_NONE,
    var url: String = "",
    var format: String = "flv",
    var pic: String? = null
) : Parcelable, Comparable<DownloadInfo> {
    override fun compareTo(other: DownloadInfo): Int {
        return other.status - status
    }

    override fun toString(): String {
        return "DownloadInfo(\n" +
                "id=$id, \n" +
                "name='$name', \n" +
                "path='$path', \n" +
                "totalSize='$totalSize', \n" +
                "percent=$percent, \n" +
                "speed='$speed', \n" +
                "status=$status, \n" +
                "url='$url', \n" +
                "format='$format'\n" +
                ")"
    }

    companion object {
        // 未开始
        const val STATUS_NONE = 0

        // 等待中
        const val STATUS_PREPARE_DOWNLOAD = 1

        // 下载中
        const val STATUS_DOWNLOADING = 2

        // 格式转换中
        const val STATUS_CONVERT = 3

        // 下载失败
        const val STATUS_ERROR = 4
    }
}