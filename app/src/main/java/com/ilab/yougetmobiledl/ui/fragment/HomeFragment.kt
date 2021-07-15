package com.ilab.yougetmobiledl.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.ftd.livepermissions.LivePermissions
import com.ftd.livepermissions.PermissionResult
import com.ilab.yougetmobiledl.R
import com.ilab.yougetmobiledl.utils.AppUtil
import com.ilab.yougetmobiledl.utils.clickNoRepeat
import com.ilab.yougetmobiledl.utils.init
import com.ilab.yougetmobiledl.utils.showToast
import com.ilab.yougetmobiledl.viewmodel.HomeViewModel
import com.youth.banner.Banner
import com.youth.banner.adapter.BannerImageAdapter
import kotlinx.android.synthetic.main.home_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("SetTextI18n")
class HomeFragment : Fragment() {
    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        (banner as Banner<Int, BannerImageAdapter<Int>>).init(
            arrayListOf(
                R.drawable.banner_1,
                R.drawable.banner_2,
                R.drawable.banner_3,
            )
        )

        viewModel.downloadStatus.observe(viewLifecycleOwner) {
            it?.let {
                when (it) {
                    HomeViewModel.Status.SUCCESS -> {
                        showToast("下载完成")
                        viewModel.downloadStatus.value = HomeViewModel.Status.IDLE
                    }
                    HomeViewModel.Status.URL_ERROR -> {
                        showToast("URL不可用")
                        viewModel.downloadStatus.value = HomeViewModel.Status.IDLE
                    }
                    HomeViewModel.Status.SDCARD_NOT_FOUND -> {
                        showToast("未找到内部存储，无法下载")
                        viewModel.downloadStatus.value = HomeViewModel.Status.IDLE
                    }
                    else -> {
                    }
                }
                tvInfo.setBackgroundColor(resources.getColor(R.color.transparent))
            }
        }

        viewModel.downloadInfo.observe(viewLifecycleOwner) {
            it?.let {
                tvInfo.text = """
                    地址: ${it.url}
                    文件总大小: ${it.totalSize}
                    下载进度: ${it.percent}
                    下载速度: ${it.speed}
                """.trimIndent()
            } ?: if (viewModel.downloadStatus.value == HomeViewModel.Status.DOWNLOAD) {
                tvInfo.text = "下载中..."
            } else {
                tvInfo.text = ""
            }
        }

        btnDownload.clickNoRepeat {
            LivePermissions(this)
                .request(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).observe(viewLifecycleOwner) {
                    when (it) {
                        is PermissionResult.Grant -> {
                            if (viewModel.downloadStatus.value == HomeViewModel.Status.IDLE) {
                                etUrl.text.toString().trim().let { et ->
                                    if (et.isNotEmpty() && !AppUtil.isUrl(et)) {
                                        showToast("请输入正确的url格式")
                                        return@observe
                                    }
                                    if (et.isEmpty()) {
                                        showToast("开始下载默认视频")
                                        viewModel.download("https://www.bilibili.com/video/BV1sM4y1M7qR")
                                    } else if (AppUtil.isUrl(et)) {
                                        showToast("开始下载")
                                        viewModel.download(et)
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
                                val uri =
                                    Uri.fromParts("package", requireActivity().packageName, null)
                                intent.data = uri
                                startActivity(intent)
                            }
                        }
                    }
                }
        }
    }

}