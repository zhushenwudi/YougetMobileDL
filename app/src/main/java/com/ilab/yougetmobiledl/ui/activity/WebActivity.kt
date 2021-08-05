package com.ilab.yougetmobiledl.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.ilab.yougetmobiledl.R
import com.ilab.yougetmobiledl.base.BaseActivity
import com.ilab.yougetmobiledl.base.eventVM
import com.ilab.yougetmobiledl.databinding.ActivityWebBinding
import com.ilab.yougetmobiledl.ext.clickNoRepeat
import com.ilab.yougetmobiledl.service.WebServer
import com.ilab.yougetmobiledl.viewmodel.WebViewModel
import com.wcl.notchfit.NotchFit
import com.wcl.notchfit.args.NotchScreenType
import dev.utils.app.NetWorkUtils
import kotlinx.android.synthetic.main.activity_web.*

class WebActivity : BaseActivity<WebViewModel, ActivityWebBinding>() {

    private val mIntent by lazy { Intent(this, WebServer::class.java) }

    override fun layoutId() = R.layout.activity_web

    override fun initView(savedInstanceState: Bundle?) {
        mDatabind?.vm = mViewModel

        NotchFit.fit(
            this, NotchScreenType.TRANSLUCENT
        ) { notchProperty ->
            if (notchProperty.isNotchEnable) {
                val marginLayoutParams = clWebLayout.layoutParams as ViewGroup.MarginLayoutParams
                marginLayoutParams.topMargin = notchProperty.notchHeight
                clWebLayout.requestLayout()
            }
        }

        Glide.with(this).asGif().load(R.drawable.gif_transform).into(ivGif)

        if (eventVM.wifiConnected.value == true) {
            startService(mIntent)

            tvIp.text = "http://${NetWorkUtils.getIpAddressByWifi()}:8080"
        } else {
            forceCloseWeb()
        }

        btnBack.clickNoRepeat {
            finish()
        }
    }

    override fun createObserver() {
        eventVM.wifiConnected.observe(this) {
            if (!it) {
                forceCloseWeb()
            }
        }
    }

    private fun forceCloseWeb() {
        eventVM.globalToast.value = "请先打开 wifi"
        finish()
    }

    override fun onPause() {
        super.onPause()
        stopService(mIntent)
    }
}