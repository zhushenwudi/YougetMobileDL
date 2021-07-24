package com.ilab.yougetmobiledl.model

data class StreamInfo(
    val url: String,
    val title: String,
    val site: String,
    val streams: Stream
)

data class Stream(
    val flv: Flv?,
    val flv720: Flv?,
    val flv480: Flv?,
    val flv360: Flv?,
    val `dash-flv`: DashFlv?,
    val `dash-flv720`: DashFlv?,
    val `dash-flv480`: DashFlv?,
    val `dash-flv360`: DashFlv?,
)

data class Flv(
    val container: String,
    val quality: String,
    val size: Int,
    val src: List<String>
)

data class DashFlv(
    val container: String,
    val quality: String,
    val size: Int,
    val src: List<List<String>>
)