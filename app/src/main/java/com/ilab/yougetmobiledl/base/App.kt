package com.ilab.yougetmobiledl.base

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.chaquo.python.android.PyApplication
import com.heima.easysp.SharedPreferencesUtils
import com.ilab.yougetmobiledl.model.MyObjectBox
import com.ilab.yougetmobiledl.viewmodel.MyEventVM
import dev.DevUtils
import io.objectbox.BoxStore

val eventVM by lazy { App.eventVM }

class App : PyApplication(), ViewModelStoreOwner {
    private var mFactory: ViewModelProvider.Factory? = null
    private lateinit var mAppViewModelStore: ViewModelStore

    companion object {
        lateinit var boxStore: BoxStore
        lateinit var eventVM: MyEventVM
        lateinit var sp: SharedPreferencesUtils
        var isInitDB = true
    }

    override fun onCreate() {
        super.onCreate()
        mAppViewModelStore = ViewModelStore()
        sp = SharedPreferencesUtils.init(this)
        // 打开 lib 内部日志 - 线上 (release) 环境，不调用方法
        DevUtils.openLog()
        // 标示 debug 模式
        DevUtils.openDebug()
        eventVM = getAppViewModelProvider().get(MyEventVM::class.java)
        boxStore = MyObjectBox.builder().androidContext(this).build()
    }

    private fun getAppViewModelProvider(): ViewModelProvider {
        return ViewModelProvider(this, this.getAppFactory())
    }

    private fun getAppFactory(): ViewModelProvider.Factory {
        if (mFactory == null) {
            mFactory = ViewModelProvider.AndroidViewModelFactory.getInstance(this)
        }
        return mFactory as ViewModelProvider.Factory
    }

    override fun getViewModelStore() = mAppViewModelStore
}