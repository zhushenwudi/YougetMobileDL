package com.ilab.yougetmobiledl.model

data class BangumiDualStream(
    val code: Int,
    val message: String,
    val result: DualStream?
) {
    fun isSuccess() = message == "success" && code == 0
}