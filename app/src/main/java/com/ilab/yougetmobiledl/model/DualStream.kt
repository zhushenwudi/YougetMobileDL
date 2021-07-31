package com.ilab.yougetmobiledl.model

data class DualStream(
    val result: String?,
    val format: String,
    val durl: List<Durl>
) {
    fun isSuccess() = result == "suee"
}

data class Durl(
    val length: Int = 0,
    val size: Int = 0,
    val url: String?,
    val badge: String = ""
)