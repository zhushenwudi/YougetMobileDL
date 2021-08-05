package com.ilab.yougetmobiledl.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.ilab.yougetmobiledl.R
import com.ilab.yougetmobiledl.base.App
import com.ilab.yougetmobiledl.base.BaseActivity
import com.ilab.yougetmobiledl.base.BaseViewModel
import com.ilab.yougetmobiledl.databinding.ActivitySplashBinding
import com.ilab.yougetmobiledl.model.SplashInfo
import com.ilab.yougetmobiledl.network.apiService
import com.ilab.yougetmobiledl.utils.AppUtil
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.await

class SplashActivity : BaseActivity<BaseViewModel, ActivitySplashBinding>() {
    private val splashBox = App.boxStore.boxFor(SplashInfo::class.java)

    override fun layoutId() = R.layout.activity_splash

    override fun initView(savedInstanceState: Bundle?) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val size = splashBox.all.size

                withContext(Dispatchers.Main) {
                    if (size == 0) {
                        Glide.with(this@SplashActivity)
                            .load(SPLASH_DEF_PHOTO)
                            .into(ivSplash)
                    } else {
                        Glide.with(this@SplashActivity)
                            .load(splashBox.all[(0 until size).shuffled().last()].pic)
                            .into(ivSplash)
                    }
                }

                val splashInfo = apiService.getSplashPhoto().await()
                if (splashInfo.isSuccess()) {
                    val splash = splashInfo.data
                    val tempList = mutableListOf<SplashInfo>()
                    splash.show.forEach { show ->
                        splash.list.forEach { list ->
                            if (show.id == list.id) {
                                val info = SplashInfo::class.java.newInstance()
                                info.pId = list.id
                                info.pic = list.thumb
                                tempList.add(info)
                            }
                        }
                    }
                    if (tempList.size > 0) {
                        splashBox.removeAll()
                        tempList.forEach {
                            splashBox.put(it)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        AppUtil.countDownCoroutines(
            total = 1,
            onFinish = {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            },
            scope = lifecycleScope
        )
    }

    override fun createObserver() {}

    companion object {
        const val SPLASH_DEF_PHOTO =
            "http://i0.hdslb.com/bfs/feed-admin/53345cd0bd88a1a1baf4b16c8a67f741e1f1461d.png"
    }
}