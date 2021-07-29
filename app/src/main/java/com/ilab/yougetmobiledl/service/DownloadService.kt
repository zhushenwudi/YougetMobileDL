package com.ilab.yougetmobiledl.service

import android.annotation.TargetApi
import android.app.*
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.ilab.yougetmobiledl.R
import com.ilab.yougetmobiledl.base.App.Companion.isInitDB
import com.ilab.yougetmobiledl.base.eventVM
import com.ilab.yougetmobiledl.download.DownloadManagerImpl
import com.ilab.yougetmobiledl.model.DownloadInfo
import com.ilab.yougetmobiledl.model.DownloadInfo.Companion.STATUS_CONVERT_FAIL
import com.ilab.yougetmobiledl.model.DownloadInfo.Companion.STATUS_PREPARE_DOWNLOAD
import com.ilab.yougetmobiledl.model.DownloadedInfo
import com.ilab.yougetmobiledl.utils.AppUtil.TAG
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.PriorityBlockingQueue

@RequiresApi(Build.VERSION_CODES.O)
class DownloadService : LifecycleService(), CoroutineScope by MainScope() {

    private var serviceObserver = ServiceObserver()

    private val pQueue = PriorityBlockingQueue<DownloadInfo>(10)
    private var isRunning = true
    private var currentInfo: DownloadInfo? = null

    private val channel by lazy {
        NotificationChannel(
            ID,
            NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
    }

    enum class Event {
        ADD_ONE,
        REMOVE_DOWNLOAD_ONE,
        REMOVE_DOWNLOADED_ONE,
        START_ONE,
        PAUSE_ONE,
        START_ALL,
        PAUSE_ALL,
        CONVERT
    }

    // 监听生命周期 ==> 关闭服务并移除监听
    internal inner class ServiceObserver : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun release() {
            pQueue.removeAll { true }
            isRunning = false
            stopForeground(true)
            lifecycle.removeObserver(serviceObserver)
        }
    }

    @ExperimentalCoroutinesApi
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG(), "启动服务")
        lifecycleScope.launch(Dispatchers.IO) {
            val mutex = Mutex()
            while (isRunning) {
                if (pQueue.isNotEmpty()) {
                    // 队列里还有任务
                    if (!mutex.isLocked) {
                        // 没有任务在执行
                        val info = pQueue.take()
                        Log.d(TAG(), "正在处理的任务名: ${info.name}")
                        currentInfo = info
                        // 状态为 转换失败 -> 重新转换
                        if (info.status == STATUS_CONVERT_FAIL) {
                            val res = downloadManager.convert(info) {
                                currentInfo?.percent = it
                                sendNotification(currentInfo)
                            }
                            if (res.first) {
                                eventVM.globalToast.postValue("转换成功 ${info.name}")
                            } else {
                                eventVM.globalToast.postValue("转换失败: ${res.second}")
                            }
                        }
                        // 状态为 准备下载 -> 下载
                        else if (info.status == STATUS_PREPARE_DOWNLOAD) {
                            val res = downloadManager.download(info) {
                                currentInfo?.percent = it
                                sendNotification(currentInfo)
                            }
                            if (res.first) {
                                eventVM.globalToast.postValue("下载成功 ${info.name}")
                            } else {
                                eventVM.globalToast.postValue("下载失败: ${res.second}")
                            }
                        }
                    }
                } else if (!mutex.isLocked) {
                    // 没有下载任务 && 没有任务在执行
                    sendNotification(null)
                }
                delay(1000)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        sendNotification(null)

        // 初始化程序
        if (isInitDB) {
            isInitDB = false
            pQueue.addAll(downloadManager.getLatestDownloadTasks())
        }

        launch {
            val downloadInfo = intent?.getParcelableExtra<DownloadInfo>("downloadInfo")
            when (intent?.getSerializableExtra("msg")) {
                // 添加一个视频
                Event.ADD_ONE -> downloadInfo?.let { pQueue.add(downloadManager.add(it)) }
                // 删除一个视频
                Event.REMOVE_DOWNLOAD_ONE -> {
                    downloadInfo?.let { downloadManager.remove(it) }
                }
                Event.REMOVE_DOWNLOADED_ONE -> {
                    val downloadedInfo = intent.getParcelableExtra<DownloadedInfo>("downloadedInfo")
                    downloadedInfo?.let { downloadManager.remove(it) }
                }
                // 开始下载一个视频(下载、转换格式)
                Event.START_ONE, Event.CONVERT -> downloadInfo?.let {
                    pQueue.add(downloadManager.resume(it))
                }
                // 暂停下载一个视频
                Event.PAUSE_ONE -> downloadInfo?.let { downloadManager.pause(it) }
                // 开始下载所有视频
                Event.START_ALL -> {
                    downloadManager.resumeAll()
                    val downloadList = eventVM.mutableDownloadTasks.value
                    downloadList?.forEach list@{ info ->
                        var isHas = false
                        pQueue.forEach queue@{ p ->
                            if (info.id == p.id) {
                                isHas = true
                                return@list
                            }
                        }
                        if (!isHas) {
                            pQueue.add(info)
                        }
                    }
                }
                // 暂停下载所有视频
                Event.PAUSE_ALL -> {
                    downloadManager.pauseAll()
                    val downloadList = eventVM.mutableDownloadTasks.value
                    downloadList?.forEach { info ->
                        if (info.status == STATUS_PREPARE_DOWNLOAD) {
                            pQueue.removeIf { it.id == info.id }
                        }
                    }
                }
            }
        }
        return START_REDELIVER_INTENT
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun sendNotification(info: DownloadInfo?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            var mName = "等待下载..."
            var notification: Notification? = null
            info?.run {
                if (name.length > 14) mName = name.substring(0, 14) + "..."
                notification = Notification.Builder(applicationContext, ID)
                    .setContentTitle(mName)
                    .setContentText("$percent%")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setProgress(100, info.percent, false)
                    .build()
            } ?: run {
                notification = Notification.Builder(applicationContext, ID)
                    .setContentTitle(mName)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .build()
            }

            startForeground(1, notification)
        }
    }

    companion object {
        const val ID = "com.ilab.yougetmobiledl.service.DownloadService"
        const val NAME = "DownloadService"

        // 下载器管理类(单例)
        val downloadManager = DownloadManagerImpl
    }
}