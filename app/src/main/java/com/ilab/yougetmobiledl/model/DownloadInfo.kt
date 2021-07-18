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
    var percent: String = "",
    var speed: String = "",
    var status: Int = 0, // 0 未开始、1 等待中、2 下载中、3 下载失败、4 格式转换、5 下载完成
    var url: String = "",
    var format: String = "flv"
) : Parcelable