package com.ilab.yougetmobiledl.model

data class DownloadInfo(
    val url: String,
    val totalSize: String,
    val percent: String,
    val speed: String
)
