package com.ilab.yougetmobiledl.model

import android.os.Parcelable
import com.ilab.yougetmobiledl.utils.NoArg
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import kotlinx.android.parcel.Parcelize

@NoArg
@Parcelize
@Entity
data class DownloadedInfo(
    @Id
    var id: Long = 0,

    var name: String = "",
    var path: String = "",
    val totalSize: String = "",
    var photo: String = "",
    var url: String = "",
    var type: Int = 1, // 1: 视频 2: 音频
) : Parcelable