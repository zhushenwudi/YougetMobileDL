package com.ilab.yougetmobiledl.base

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider

abstract class BaseActivity<VM : BaseViewModel, DB : ViewDataBinding> : FragmentActivity() {

    lateinit var mViewModel: VM

    var mDatabind: DB? = null

    abstract fun layoutId(): Int

    abstract fun initView(savedInstanceState: Bundle?)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDatabind = DataBindingUtil.setContentView(this, layoutId())
        mDatabind?.lifecycleOwner = this
        init(savedInstanceState)
    }

    private fun init(savedInstanceState: Bundle?) {
        mViewModel = ViewModelProvider(this).get(getVmClazz(this))
        registerUiChange()
        initView(savedInstanceState)
        createObserver()
    }

    /**
     * 创建LiveData数据观察者
     */
    abstract fun createObserver()

    /**
     * 注册 UI 事件
     */
    private fun registerUiChange() {
        mViewModel.loadingChange.showDialog.observe(this) {
            showLoading(it)
        }
        mViewModel.loadingChange.dismissDialog.observe(this) {
            dismissLoading()
        }
    }

    open fun showLoading(message: String = "请求网络中...") {}

    open fun dismissLoading() {}
}