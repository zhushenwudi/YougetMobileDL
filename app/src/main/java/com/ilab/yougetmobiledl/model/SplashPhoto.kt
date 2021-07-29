package com.ilab.yougetmobiledl.model

data class SplashPhoto(
    val list: PhotoList?,
    val show: ShowList?
)

data class PhotoList(
    val id: Int?,
    val thumb: String?
)

data class ShowList(
    val id: Int?
)