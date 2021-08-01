package com.ilab.yougetmobiledl.viewmodel

import com.ilab.yougetmobiledl.base.BaseViewModel
import com.ilab.yougetmobiledl.base.event.EventMutableLiveData
import com.ilab.yougetmobiledl.model.DownloadInfo
import com.ilab.yougetmobiledl.model.DownloadedInfo

class MyEventVM : BaseViewModel() {
    val isGrantedPermission = EventMutableLiveData<Boolean>()

    // 用于视图更新的下载中视频列表
    val mutableDownloadTasks = EventMutableLiveData<MutableList<DownloadInfo>>()

    // 用于视图更新的已下载视频列表
    val mutableDownloadedTasks = EventMutableLiveData<MutableList<DownloadedInfo>>()

    val globalToast = EventMutableLiveData<String>()

    val requestFailed = EventMutableLiveData<String>()

    init {
        mutableDownloadTasks.value = mutableListOf()
        mutableDownloadedTasks.value = mutableListOf()
        isGrantedPermission.value = false
    }
}