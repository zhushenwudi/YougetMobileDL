package com.ilab.yougetmobiledl.network

import com.ilab.yougetmobiledl.model.DualStream
import com.ilab.yougetmobiledl.model.SplashPhoto
import com.ilab.yougetmobiledl.model.VideoInfo
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import retrofit2.http.Url

interface ApiService {
    /**
     * 获取开屏图片
     */
    @GET("/x/v2/splash/brand/list")
    suspend fun getSplashPhoto(
        @Query("appkey") appkey: String = "1d8b6e7d45233436",
        @Query("ts") ts: Int = 0,
        @Query("sign") sign: String = "78a89e153cd6231a4a4d55013aa063ce"
    ): ApiResponse<SplashPhoto>

    /**
     * 获取高清视频流信息
     */
    @Headers("domain:interface")
    @GET("/v2/playurl")
    suspend fun getHighDigitalVideoStream(
        @Query("appkey") appkey: String = "iVGUTjsxvpLeuDCf",
        @Query("cid") cid: Int,
        @Query("otype") otype: String = "json",
        @Query("qn") qn: Int = 112,
        @Query("sign") sign: String = ""
    ): DualStream

    /**
     * 获取视频基本信息
     */
    @GET("/x/web-interface/view")
    suspend fun getVideoInfo(
        @Query("bvid") bvid: String?,
        @Query("aid") aid: Int?
    ): ApiResponse<VideoInfo>

    /**
     * 判断视频地址是否有效
     */
    @GET
    suspend fun getHasCurrentVideo(@Url url: String): ResponseBody
}
