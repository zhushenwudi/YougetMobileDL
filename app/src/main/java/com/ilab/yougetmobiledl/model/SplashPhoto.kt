package com.ilab.yougetmobiledl.model

import android.os.Parcelable
import com.ilab.yougetmobiledl.utils.NoArg
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import kotlinx.android.parcel.Parcelize

data class SplashPhoto(
    val list: List<PhotoList>,
    val show: List<ShowList>
)

data class PhotoList(
    val id: Int = 0,
    val thumb: String = ""
)

data class ShowList(
    val id: Int = 0
)

@NoArg
@Entity
@Parcelize
data class SplashInfo(
    @Id
    var id: Long = 0,

    var pId: Int = 0,
    var pic: String?
) : Parcelable