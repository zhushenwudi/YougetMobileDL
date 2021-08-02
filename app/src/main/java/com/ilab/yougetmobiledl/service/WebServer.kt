package com.ilab.yougetmobiledl.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.yanzhenjie.andserver.AndServer
import com.yanzhenjie.andserver.Server
import java.util.concurrent.TimeUnit

class WebServer : Service() {
    private var server: Server? = null

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        server = AndServer.webServer(this)
            .port(8080)
            .timeout(10, TimeUnit.SECONDS)
            .build()
        server?.startup()
        return START_STICKY
    }

    override fun onDestroy() {
        server?.shutdown()
        server = null
        super.onDestroy()
    }
}