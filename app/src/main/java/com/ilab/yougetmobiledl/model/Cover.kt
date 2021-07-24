package com.ilab.yougetmobiledl.model

data class Cover(
    val code: Int,
    val message: String,
    val data: DataBean?
)

data class DataBean(
    val pic: String
)