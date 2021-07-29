package com.ilab.yougetmobiledl.network.interceptor

import dev.utils.common.encrypt.EncryptUtils
import okhttp3.Interceptor
import okhttp3.Response
import java.util.*

class AddParameterInterceptor : Interceptor {
    companion object {
        const val STREAM_SIGN = "aHRmhWMLkdeMuILqORnYZocwMBpMEOdt"
    }

    override fun intercept(chain: Interceptor.Chain): Response {

        //获取request
        val request = chain.request()

        //从request中获取原有的HttpUrl实列 oldHttpUrl
        val oldHttpUrl = request.url()

        // 获取所有的键
        val keyList = oldHttpUrl.queryParameterNames()

        if (request.method() == "GET" && keyList.contains("sign")) {
            // 构建前面字段
            val sign = mutableListOf<String>()

            keyList.forEachIndexed { index, str ->
                if (str != "sign") {
                    // 获取值
                    val valueList = oldHttpUrl.queryParameterValues(str)
                    if (valueList.isNotEmpty()) {
                        sign.add(index, "${str}=${valueList[0]}")
                    } else {
                        sign.add(index, "${str}=")
                    }
                }
            }

            val params = StringJoiner("&")
            sign.forEach {
                params.add(it)
            }
            val md5 = EncryptUtils.encryptMD5ToHexString(params.toString() + STREAM_SIGN)

            val url = oldHttpUrl.newBuilder()
                .addQueryParameter("sign", md5)
                .build()

            return chain.proceed(request.newBuilder().url(url).build())
        }

        return chain.proceed(request)
    }
}