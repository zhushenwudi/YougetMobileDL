package com.ilab.yougetmobiledl.model

data class DualStream(
    val result: String?,
    val quality: Int,
    val format: String,
    val accept_description: List<String>,
    val accept_quality: List<Int>,
    val durl: List<Durl>
) {
    fun isSuccess() = result == "suee"
}

data class Durl(
    val length: Int = 0,
    val size: Int = 0,
    val url: String?
)