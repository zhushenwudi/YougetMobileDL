package com.ilab.yougetmobiledl.model

data class BangumiInfo(
    val code: Int,
    val message: String,
    val result: Bangumi?,
)

data class Bangumi(
    val season_id: Int, // 番剧集 id
    val season_title: String, // 番剧名
    val episodes: List<Episode>, // 分P信息
)

data class Episode(
    val id: Int, // ep id
    val long_title: String,
    val share_url: String,
    val cover: String,
    val title: String
)