package com.ilab.yougetmobiledl.base

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.reflect.ParameterizedType

abstract class BaseFragment<VM : ViewModel, DB : ViewDataBinding> : Fragment() {
    private val handler = Handler()

    private var isFirst = true

    lateinit var mViewModel: VM

    var mDatabind: DB? = null

    lateinit var mActivity: AppCompatActivity

    abstract fun layoutId(): Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mDatabind = DataBindingUtil.inflate(inflater, layoutId(), container, false)
        mDatabind?.lifecycleOwner = this
        return mDatabind?.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as AppCompatActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isFirst = true
        mViewModel = ViewModelProvider(this).get(getVmClazz(this))
        initView(savedInstanceState)
        createObserver()
        initData()
    }

    override fun onResume() {
        super.onResume()
        onVisible()
    }

    abstract fun initView(savedInstanceState: Bundle?)

    open fun createObserver() {}

    open fun initData() {}

    private fun onVisible() {
        if (lifecycle.currentState == Lifecycle.State.STARTED && isFirst) {
            // 延迟加载 防止 切换动画还没执行完毕时数据就已经加载好了，这时页面会有渲染卡顿
            handler.postDelayed({
                isFirst = false
            }, 300)
        }
    }
}

@Suppress("UNCHECKED_CAST")
fun <VM> getVmClazz(obj: Any): VM {
    return (obj.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as VM
}