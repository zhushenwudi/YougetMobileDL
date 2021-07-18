package com.ilab.yougetmobiledl.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import com.ilab.yougetmobiledl.R
import com.ilab.yougetmobiledl.base.BaseFragment
import com.ilab.yougetmobiledl.databinding.HomeFragmentBinding
import com.ilab.yougetmobiledl.ui.activity.MainActivity
import com.ilab.yougetmobiledl.utils.*
import com.ilab.yougetmobiledl.viewmodel.HomeViewModel
import com.youth.banner.Banner
import com.youth.banner.adapter.BannerImageAdapter
import kotlinx.android.synthetic.main.home_fragment.*

@SuppressLint("SetTextI18n")
class HomeFragment : BaseFragment<HomeViewModel, HomeFragmentBinding>() {

    override fun layoutId() = R.layout.home_fragment

    override fun initView(savedInstanceState: Bundle?) {
        mDatabind?.vm = mViewModel

        (banner as Banner<Int, BannerImageAdapter<Int>>).init(
            arrayListOf(
                R.drawable.banner_1,
                R.drawable.banner_2,
                R.drawable.banner_3,
            )
        )

        mViewModel.downloadStatus.observe(viewLifecycleOwner) {
            it?.let {
                when (it) {
                    HomeViewModel.Status.SUCCESS -> {
                        showToast("下载完成")
                        mViewModel.downloadStatus.value = HomeViewModel.Status.IDLE
                    }
                    HomeViewModel.Status.URL_ERROR -> {
                        showToast("URL不可用")
                        mViewModel.downloadStatus.value = HomeViewModel.Status.IDLE
                    }
                    HomeViewModel.Status.SDCARD_NOT_FOUND -> {
                        showToast("未找到内部存储，无法下载")
                        mViewModel.downloadStatus.value = HomeViewModel.Status.IDLE
                    }
                    else -> {
                    }
                }
                tvInfo.setBackgroundColor(resources.getColor(R.color.transparent))
            }
        }

        mViewModel.downloadInfo.observe(viewLifecycleOwner) {
            it?.let {
                showToast("即将下载: ${it.name}")
                (requireActivity() as MainActivity).download(arrayListOf(it))
            }
//            it?.let {
//                tvInfo.text = """
//                    地址: ${it.url}
//                    文件总大小: ${it.totalSize}
//                    下载进度: ${it.percent}
//                    下载速度: ${it.speed}
//                """.trimIndent()
//            } ?: if (mViewModel.downloadStatus.value == HomeViewModel.Status.DOWNLOAD) {
//                tvInfo.text = "下载中..."
//            } else {
//                tvInfo.text = ""
//            }
        }

        button.clickNoRepeat {
            mViewModel.getVideoList("https://www.bilibili.com/video/BV1sM4y1M7qR")
        }

        btnDownload.clickNoRepeat {
            requestPermission(
                permission = Manifest.permission.WRITE_EXTERNAL_STORAGE,
                onGrant = {
                    if (mViewModel.downloadStatus.value == HomeViewModel.Status.IDLE) {
                        etUrl.text.toString().trim().let { et ->
                            if (et.isNotEmpty() && !AppUtil.isUrl(et)) {
                                showToast("请输入正确的url格式")
                                return@requestPermission
                            }
                            if (et.isEmpty()) {
                                showToast("开始下载默认视频")
//                                mViewModel.getVideoList("https://www.bilibili.com/video/BV1sM4y1M7qR")
                                mViewModel.getVideoList("https://www.bilibili.com/video/BV1nA411G7Ch?t=12")
                            } else if (AppUtil.isUrl(et)) {
                                showToast("开始下载")
                                mViewModel.getVideoList(et)
                            }
                            tvInfo.setBackgroundColor(resources.getColor(R.color.tianyi))
                        }
                    } else {
                        showToast("有任务正在下载中，请稍候...")
                    }
                },
                onRationale = {
                    showToast("您拒绝了权限")
                },
                onDeny = {
                    showToast("您即将前往设置菜单，并请授权")
                }
            )
        }
    }
}