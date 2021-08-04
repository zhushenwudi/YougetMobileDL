package com.ilab.yougetmobiledl.model

import android.os.Parcelable
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
    var bvid: String? = "",
    var cid: Int = 0,
    var epid: Int = 0,
    var path: String = "",
    var totalSize: String = "",
    var percent: Int = 0,
    var speed: String = "",
    var status: Int = STATUS_NONE,
    var url: String = "",
    var format: String = "flv",
    var pic: String = "",
    var videoPart: Int = 1,
    var hasPart: Boolean = false,
    var type: Int = 1, // 0: 导入视频 1: B站视频 2: B站音频 3：优酷视频
) : Parcelable, Comparable<DownloadInfo> {
    override fun compareTo(other: DownloadInfo): Int {
        return other.status - status
    }

    fun clone(): DownloadInfo {
        return this.copy()
    }

    override fun toString(): String {
        return "DownloadInfo(\n" +
                "id=$id, \n" +
                "name='$name', \n" +
                "bvid='$bvid', \n" +
                "cid='$cid', \n" +
                "path='$path', \n" +
                "totalSize='$totalSize', \n" +
                "percent=$percent, \n" +
                "speed='$speed', \n" +
                "status=$status, \n" +
                "url='$url', \n" +
                "format='$format'\n" +
                "pic='$pic'\n" +
                "videoPart='$videoPart'\n" +
                "hasPart='$hasPart'\n" +
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

        // 转换格式失败
        const val STATUS_CONVERT_FAIL = 5
    }
}