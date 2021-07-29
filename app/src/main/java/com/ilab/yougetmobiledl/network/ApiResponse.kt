package com.ilab.yougetmobiledl.network

data class ApiResponse<T>(
    val code: Int,
    val message: String?,
    val data: T
) {
    fun isSuccess() = code == 0

    fun getRespData() = data

    fun getRespMsg() = message
}