package com.ilab.yougetmobiledl.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.ftd.livepermissions.LivePermissions
import com.ftd.livepermissions.PermissionResult
import com.ilab.yougetmobiledl.R
import com.ilab.yougetmobiledl.databinding.ActivityMainBinding
import com.ilab.yougetmobiledl.utils.AppUtil
import com.ilab.yougetmobiledl.utils.clickNoRepeat
import com.ilab.yougetmobiledl.utils.showToast
import com.ilab.yougetmobiledl.viewmodel.MainViewModel
import com.squareup.picasso.Picasso
import com.youth.banner.Banner
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    private var mBinding: ActivityMainBinding? = null
    private var mViewModel: MainViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        mViewModel?.downloadStatus?.observe(this) {
            it?.let {
                when (it) {
                    MainViewModel.Status.SUCCESS -> {
                        showToast("下载完成")
                        mViewModel?.downloadStatus?.value = MainViewModel.Status.IDLE
                    }
                    MainViewModel.Status.URL_ERROR -> {
                        showToast("URL不可用")
                        mViewModel?.downloadStatus?.value = MainViewModel.Status.IDLE
                    }
                    MainViewModel.Status.SDCARD_NOT_FOUND -> {
                        showToast("未找到内部存储，无法下载")
                        mViewModel?.downloadStatus?.value = MainViewModel.Status.IDLE
                    }
                    else -> {
                    }
                }
                tvInfo.setBackgroundColor(resources.getColor(R.color.transparent))
            }
        }

        mViewModel?.downloadInfo?.observe(this) {
            it?.let {
                tvInfo.text = """
                    地址: ${it.url}
                    文件总大小: ${it.totalSize}
                    下载进度: ${it.percent}
                    下载速度: ${it.speed}
                """.trimIndent()
            } ?: if (mViewModel?.downloadStatus?.value == MainViewModel.Status.DOWNLOAD) {
                tvInfo.text = "下载中..."
            } else {
                tvInfo.text = ""
            }
        }

        btnDownload.clickNoRepeat {
            LivePermissions(this)
                .request(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).observe(this) {
                    when (it) {
                        is PermissionResult.Grant -> {
                            if (mViewModel?.downloadStatus?.value == MainViewModel.Status.IDLE) {
                                etUrl.text.toString().trim().let { et ->
                                    showToast("开始下载")
                                    if (et != "" && AppUtil.isUrl(et)) {
                                        mViewModel?.download(et)
                                    } else {
                                        mViewModel?.download("https://www.bilibili.com/video/BV1sM4y1M7qR")
                                    }
                                    tvInfo.setBackgroundColor(resources.getColor(R.color.tianyi))
                                }
                            } else {
                                showToast("有任务正在下载中，请稍候...")
                            }
                        }
                        is PermissionResult.Rationale -> {
                            showToast("您拒绝了权限")
                        }
                        is PermissionResult.Deny -> {
                            showToast("您即将前往设置菜单，并请授权")
                            lifecycleScope.launch(Dispatchers.Main) {
                                delay(3000)
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri = Uri.fromParts("package", packageName, null)
                                intent.data = uri
                                startActivity(intent)
                            }
                        }
                    }
                }
        }

        btnDev.clickNoRepeat {
            openDevSetting()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!AppUtil.isADBEnable()) {
            showToast("检测到您未打开USB调试模式将无法显示下载状态")
        }

        (banner as Banner<Int, BannerImageAdapter<Int>>).setAdapter(object :
            BannerImageAdapter<Int>(
                arrayListOf(
                    R.drawable.banner_1,
                    R.drawable.banner_2,
                    R.drawable.banner_3,
                )
            ) {
            override fun onBindView(
                holder: BannerImageHolder,
                image: Int,
                position: Int,
                size: Int
            ) {
                Picasso.get().load(image).into(holder.imageView)
            }
        })
            .addBannerLifecycleObserver(this)
    }

    private fun openDevSetting() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            try {
                val componentName = ComponentName(
                    "com.android.settings",
                    "com.android.settings.DevelopmentSettings"
                )
                val intent = Intent()
                intent.component = componentName
                intent.action = "android.intent.action.View"
                startActivity(intent)
            } catch (e1: Exception) {
                try {
                    // 部分小米手机采用这种方式跳转
                    val intent = Intent("com.android.settings.APPLICATION_DEVELOPMENT_SETTINGS")
                    startActivity(intent)
                } catch (e2: Exception) {
                }
            }
        }
    }
}