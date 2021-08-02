package com.ilab.yougetmobiledl.network.server

import android.content.Context
import com.yanzhenjie.andserver.annotation.Config
import com.yanzhenjie.andserver.framework.config.Multipart
import com.yanzhenjie.andserver.framework.config.WebConfig
import com.yanzhenjie.andserver.framework.website.AssetsWebsite
import java.io.File

@Config
class AppConfig : WebConfig {
    override fun onConfig(context: Context, delegate: WebConfig.Delegate) {
        // 增加一个静态网站
        delegate.addWebsite(AssetsWebsite(context, "/httpweb"))

        // 自定义配置表单请求和文件上传的条件
        delegate.setMultipart(
            Multipart.newBuilder()
                .allFileMaxSize(1000000000) // 单个请求上传文件总大小
                .fileMaxSize(1000000000) // 单个文件的最大大小
                .maxInMemorySize(1024 * 10) // 保存上传文件时buffer大小
                .uploadTempDir(File(context.cacheDir, "_server_upload_cache_")) // 文件保存目录
                .build()
        )
    }
}