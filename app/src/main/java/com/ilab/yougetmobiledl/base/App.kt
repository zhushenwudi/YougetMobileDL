package com.ilab.yougetmobiledl.base

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.chaquo.python.android.PyApplication
import com.heima.easysp.SharedPreferencesUtils
import com.ilab.yougetmobiledl.BuildConfig
import com.ilab.yougetmobiledl.ext.appContext
import com.ilab.yougetmobiledl.model.MyObjectBox
import com.ilab.yougetmobiledl.viewmodel.MyEventVM
import com.skl.networkmonitor.NetType
import com.skl.networkmonitor.NetworkLiveData
import dev.DevUtils
import dev.utils.LogPrintUtils
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
        lateinit var instance: Context
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        mAppViewModelStore = ViewModelStore()
        sp = SharedPreferencesUtils.init(this)
        // 打开 lib 内部日志 - 线上 (release) 环境，不调用方法
        DevUtils.openLog()
        // 标示 debug 模式
        DevUtils.openDebug()
        eventVM = getAppViewModelProvider().get(MyEventVM::class.java)
        boxStore = MyObjectBox.builder().androidContext(this).build()
        boxStore.startObjectBrowser(8090)

        LogPrintUtils.setPrintLog(BuildConfig.DEBUG)

        ProcessLifecycleOwner.get().run {
            eventVM.globalToast.observe(this) {
                Toast.makeText(appContext, it, Toast.LENGTH_SHORT).show()
                LogPrintUtils.e(it)
            }

            NetworkLiveData.get(appContext).observe(this) {
                when (it.ordinal) {
                    NetType.WIFI.ordinal -> {
                        eventVM.wifiConnected.value = true
                    }
                    NetType.NET_2G.ordinal,
                    NetType.NET_3G.ordinal,
                    NetType.NET_4G.ordinal -> {
                        eventVM.wifiConnected.value = false
                    }
                    NetType.NOME.ordinal -> {
                        eventVM.wifiConnected.value = false
                    }
                }
            }
        }
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