package com.ilab.yougetmobiledl.viewmodel

import androidx.lifecycle.ViewModel
import com.ilab.yougetmobiledl.base.event.EventMutableLiveData
import com.ilab.yougetmobiledl.service.DownloadTask

class MyEventVM : ViewModel() {

    val currentDownloadTask = EventMutableLiveData<DownloadTask>()
}