package com.ilab.yougetmobiledl.viewmodel

import androidx.lifecycle.ViewModel
import com.ilab.yougetmobiledl.base.event.EventMutableLiveData
import com.ilab.yougetmobiledl.model.DownloadInfo
import com.ilab.yougetmobiledl.model.VideoInfo

class MyEventVM : ViewModel() {
    // 用于视图更新的下载中视频列表
    val mutableDownloadTasks = EventMutableLiveData<MutableList<DownloadInfo>>()

    // 用于视图更新的已下载视频列表
    val mutableDownloadedTasks = EventMutableLiveData<MutableList<VideoInfo>>()

    init {
        mutableDownloadTasks.value = mutableListOf()
        mutableDownloadedTasks.value = mutableListOf()
    }
}