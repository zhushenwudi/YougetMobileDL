package com.ilab.yougetmobiledl.service

import android.content.Intent
import android.util.Log
import androidx.lifecycle.*
import com.ilab.yougetmobiledl.base.App
import com.ilab.yougetmobiledl.base.App.Companion.isInitDB
import com.ilab.yougetmobiledl.base.eventVM
import com.ilab.yougetmobiledl.model.DownloadInfo
import com.ilab.yougetmobiledl.model.DownloadInfo_
import com.ilab.yougetmobiledl.model.VideoInfo
import com.ilab.yougetmobiledl.model.VideoInfo_
import com.ilab.yougetmobiledl.utils.AppUtil.TAG
import kotlinx.coroutines.*
import java.util.concurrent.LinkedBlockingQueue

class DownloadService : LifecycleService(), CoroutineScope by MainScope() {

    private var serviceObserver = ServiceObserver()
    private val queue = LinkedBlockingQueue<DownloadInfo>()

    private val downloadBox = App.boxStore.boxFor(DownloadInfo::class.java)
    private val downloadedBox = App.boxStore.boxFor(VideoInfo::class.java)

    enum class Event {
        ADD_ONE,
        REMOVE_ONE,
        START_ONE,
        PAUSE_ONE,
        START_ALL,
        PAUSE_ALL
    }

    // 监听生命周期 ==> 关闭设备并移除监听
    internal inner class ServiceObserver : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun release() {
            queue.removeAll { true }
            lifecycle.removeObserver(serviceObserver)
        }
    }

    @ExperimentalCoroutinesApi
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG(), "启动服务")

        if (isInitDB) {
            isInitDB = false
            val videoList = downloadBox.query()
                .equal(DownloadInfo_.status, 1).or()
                .equal(DownloadInfo_.status, 2).build().find()
            if (videoList.isNotEmpty()) {
                videoList.forEach {
                    it.status = 1
                    queue.put(it)
                }
            }
        }

        launch(Dispatchers.IO) {
            processingDownloadTasks()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        launch {
            when (intent?.getSerializableExtra("msg")) {
                // 添加一个视频
                Event.ADD_ONE -> {
                    intent.getParcelableArrayListExtra<DownloadInfo>("downloadList")?.run {
                        forEach { info ->
                            val downloadedList =
                                downloadedBox.query().equal(VideoInfo_.name, info.name).build()
                                    .find()
                            if (downloadedList.isNotEmpty()) return@forEach
                            val video =
                                downloadBox.query().equal(DownloadInfo_.name, info.name).build()
                                    .findFirst()
                            if (video?.status != 0) return@forEach
                            video.status = 1
                            downloadBox.put(video)
                            queue.put(video)
                            Log.d(TAG(), "添加成功")
                        }
                    }
                }
                // 删除一个视频
                Event.REMOVE_ONE -> {
                    intent.getParcelableArrayListExtra<DownloadInfo>("downloadList")?.run {
                        forEach { info ->
                            if (queue.contains(info)) {
                                // 在队列里
                                val video =
                                    downloadBox.query().equal(DownloadInfo_.name, info.name).build()
                                        .findFirst()
                                video?.run {
                                    downloadBox.remove(this)
                                    queue.remove(info)
                                }
                            } else {
                                val currentTask = eventVM.currentDownloadTask.value
                                currentTask?.run {
                                    if (getInfo().name == info.name) {
                                        // 正在下载
                                        onDelete()
                                    }
                                }
                            }
                        }
                    }
                }
                // 开始下载一个视频
                Event.START_ONE -> {
                    intent.getParcelableArrayListExtra<DownloadInfo>("downloadList")?.run {
                        forEach { info ->
                            val video =
                                downloadBox.query().equal(DownloadInfo_.name, info.name).build()
                                    .findFirst()
                            video?.run {
                                status = 1
                                downloadBox.put(this)
                                queue.put(this)
                            }
                        }
                    }
                }
                // 暂停下载一个视频
                Event.PAUSE_ONE -> {
                    intent.getParcelableArrayListExtra<DownloadInfo>("downloadList")?.run {
                        forEach { info ->
                            if (queue.contains(info)) {
                                val video =
                                    downloadBox.query().equal(DownloadInfo_.name, info.name).build()
                                        .findFirst()
                                video?.run {
                                    status = 0
                                    downloadBox.put(this)
                                    queue.remove(info)
                                }
                            } else {
                                Log.d(TAG(), "正在下载中，无法暂停")
                            }
                        }
                    }
                }
                // 开始下载所有视频
                Event.START_ALL -> {
                    val videoList = downloadBox.all
                    videoList?.run {
                        forEach {
                            if (!queue.contains(it)) {
                                it.status = 1
                                downloadBox.put(it)
                                queue.put(it)
                            }
                        }
                    }
                }
                // 暂停下载所有视频
                Event.PAUSE_ALL -> {
                    val videoList = downloadBox.all
                    videoList?.run {
                        forEach {
                            if (queue.contains(it)) {
                                it.status = 0
                                downloadBox.put(it)
                                queue.remove(it)
                            }
                        }
                    }
                }
            }

        }
        return super.onStartCommand(intent, flags, startId)
    }

    @ExperimentalCoroutinesApi
    suspend fun processingDownloadTasks() {
        while (queue.isNotEmpty()) {
            val downloadInfo = queue.take()
            val video =
                downloadBox.query().equal(DownloadInfo_.name, downloadInfo.name).build().findFirst()
            video?.run {
                status = 2
                downloadBox.put(this)
                Log.d(TAG(), "正在下载 ${downloadInfo.name}")
                val task = DownloadTask(this)
                withContext(Dispatchers.Main) {
                    eventVM.currentDownloadTask.value = task
                    task.onDownload(task.getInfo().url)
                }
                Log.d(TAG(), "下载完毕 ${downloadInfo.name}")
            }
        }
        waitingToStop()
    }

    @ExperimentalCoroutinesApi
    suspend fun waitingToStop() {
        delay(1000)
        if (queue.isEmpty()) {
            delay(10000)
            if (queue.isEmpty()) {
                Log.d(TAG(), "停止服务")
                stopSelf()
                return
            }
        }
        processingDownloadTasks()
    }
}