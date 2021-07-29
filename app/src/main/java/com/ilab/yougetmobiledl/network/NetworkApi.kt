package com.ilab.yougetmobiledl.network

import com.google.gson.GsonBuilder
import com.ilab.yougetmobiledl.network.interceptor.AddParameterInterceptor
import com.ilab.yougetmobiledl.network.interceptor.ChangeBaseUrlInterceptor
import com.ilab.yougetmobiledl.network.interceptor.URLConstant
import com.ilab.yougetmobiledl.network.interceptor.logging.LogInterceptor
import dev.utils.app.NetWorkUtils
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val apiService: ApiService by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
    NetworkApi.INSTANCE.getApi(ApiService::class.java, URLConstant.BASE_URL)
}

class NetworkApi : BaseNetworkApi() {
    companion object {
        val INSTANCE: NetworkApi by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            NetworkApi()
        }

        private const val REQUEST_TYPE = "application/json;charset=UTF-8"

        fun createBody(obj: Any): RequestBody {
            return RequestBody.create(MediaType.parse(REQUEST_TYPE), obj.toString())
        }
    }

    override fun setHttpClientBuilder(builder: OkHttpClient.Builder): OkHttpClient.Builder {
        if (!NetWorkUtils.isAvailableByPing()) {
            throw NoNetworkException()
        }
        builder.apply {
            addInterceptor(AddParameterInterceptor())
            addInterceptor(ChangeBaseUrlInterceptor())
            addInterceptor(LogInterceptor())
            connectTimeout(10, TimeUnit.SECONDS)
        }
        return builder
    }

    override fun setRetrofitBuilder(builder: Retrofit.Builder): Retrofit.Builder {
        return builder.apply {
            addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        }
    }

    class NoNetworkException : RuntimeException()
}