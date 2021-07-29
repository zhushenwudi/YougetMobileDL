package com.ilab.yougetmobiledl.model

data class ConvertInfo(
    val isSuccess: Boolean,
    val message: String?,
    val data: DownloadInfo?
)
