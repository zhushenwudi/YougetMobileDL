package com.ilab.yougetmobiledl.ext

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.ilab.yougetmobiledl.base.event.EventMutableLiveData

object KtxAppLifeObserver : LifecycleObserver {

    var isForeground = EventMutableLiveData<Boolean>()

    //在前台
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onForeground() {
        isForeground.value = true
    }

    //在后台
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onBackground() {
        isForeground.value = false
    }

}