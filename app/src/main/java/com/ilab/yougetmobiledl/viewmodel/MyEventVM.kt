package com.ilab.yougetmobiledl.viewmodel

import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.core.content.ContextCompat
import com.ilab.yougetmobiledl.base.BaseViewModel
import com.ilab.yougetmobiledl.base.Const.STORAGE_PERMISSION
import com.ilab.yougetmobiledl.base.event.EventMutableLiveData
import com.ilab.yougetmobiledl.ext.appContext
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

    val wifiConnected = EventMutableLiveData<Boolean>()

    init {
        mutableDownloadTasks.value = mutableListOf()
        mutableDownloadedTasks.value = mutableListOf()
        isGrantedPermission.value =
            ContextCompat.checkSelfPermission(appContext, STORAGE_PERMISSION) == PERMISSION_GRANTED
    }
}