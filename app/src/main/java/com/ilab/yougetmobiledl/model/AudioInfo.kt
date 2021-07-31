package com.ilab.yougetmobiledl.model

data class AudioInfo(
    val code: Int,
    val msg: String,
    val data: Audio
)

data class Audio(
    val title: String? = "",
    val cover: String?,
    val size: Int? = 0
)
