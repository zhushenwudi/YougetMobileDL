package com.ilab.yougetmobiledl.network.interceptor

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response

object URLConstant {
    const val BASE_URL = "https://api.bilibili.com/"
    const val BASE_INTERFACE_URL = "https://interface.bilibili.com/"
    const val INTERFACE = "interface"
    const val DOMAIN = "domain"
}

class ChangeBaseUrlInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        //获取request
        val request = chain.request()

        //从request中获取原有的HttpUrl实列 oldHttpUrl
        val oldHttpUrl = request.url()

        //获取request的创建者
        val builder = request.newBuilder()

        //从request中获取headers 通过给定的建的url_name
        val headerValues = request.headers(URLConstant.DOMAIN)

        if (headerValues.size > 0) {
            builder.removeHeader(URLConstant.DOMAIN)

            val newBaseUrl: HttpUrl? = when (headerValues[0]) {
                URLConstant.INTERFACE -> {
                    HttpUrl.parse(URLConstant.BASE_INTERFACE_URL)
                }
                else -> {
                    HttpUrl.parse(URLConstant.BASE_URL)
                }
            }
            newBaseUrl?.run {
                val newHttpUrl =
                    oldHttpUrl.newBuilder().scheme(scheme()).host(host())
                        .port(port()).build()
                return chain.proceed(builder.url(newHttpUrl).build())
            }
        }
        return chain.proceed(request)
    }
}