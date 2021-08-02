package com.ilab.yougetmobiledl.viewmodel

import androidx.lifecycle.MutableLiveData
import com.ilab.yougetmobiledl.base.BaseViewModel

class WebViewModel : BaseViewModel() {
    val statusBarHeight = MutableLiveData<Int>()
}