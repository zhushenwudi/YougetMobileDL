package com.ilab.yougetmobiledl.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.ilab.yougetmobiledl.R
import com.ilab.yougetmobiledl.base.App
import com.ilab.yougetmobiledl.databinding.ActivitySplashBinding
import com.ilab.yougetmobiledl.model.SplashInfo
import com.ilab.yougetmobiledl.network.apiService
import com.ilab.yougetmobiledl.utils.AppUtil
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.await

class SplashActivity : AppCompatActivity() {
    private var mBinding: ActivitySplashBinding? = null
    private val splashBox = App.boxStore.boxFor(SplashInfo::class.java)

    companion object {
        const val SPLASH_DEF_PHOTO =
            "http://i0.hdslb.com/bfs/feed-admin/53345cd0bd88a1a1baf4b16c8a67f741e1f1461d.png"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash)

        AppUtil.countDownCoroutines(
            total = 3,
            onFinish = {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            },
            scope = lifecycleScope
        )

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val size = splashBox.all.size

                withContext(Dispatchers.Main) {
                    if (size == 0) {
                        Picasso.get().load(SPLASH_DEF_PHOTO).into(ivSplash)
                    } else {
                        Picasso.get().load(splashBox.all[(0 until size).shuffled().last()].pic)
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
    }
}