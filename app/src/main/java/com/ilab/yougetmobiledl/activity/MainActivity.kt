package com.ilab.yougetmobiledl.activity

import android.Manifest
import android.annotation.SuppressLint
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
import com.ilab.yougetmobiledl.utils.*
import com.ilab.yougetmobiledl.viewmodel.MainViewModel
import com.youth.banner.Banner
import com.youth.banner.adapter.BannerImageAdapter
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

        (banner as Banner<Int, BannerImageAdapter<Int>>).init(
            arrayListOf(
                R.drawable.banner_1,
                R.drawable.banner_2,
                R.drawable.banner_3,
            )
        )

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
                                    if (et.isNotEmpty() && !AppUtil.isUrl(et)) {
                                        showToast("请输入正确的url格式")
                                        return@observe
                                    }
                                    if (et.isEmpty()) {
                                        showToast("开始下载默认视频")
                                        mViewModel?.download("https://www.bilibili.com/video/BV1sM4y1M7qR")
                                    } else if (AppUtil.isUrl(et)) {
                                        showToast("开始下载")
                                        mViewModel?.download(et)
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
    }
}