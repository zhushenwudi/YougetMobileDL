package com.ilab.yougetmobiledl.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import com.ilab.yougetmobiledl.R
import com.ilab.yougetmobiledl.base.BaseFragment
import com.ilab.yougetmobiledl.base.eventVM
import com.ilab.yougetmobiledl.databinding.HomeFragmentBinding
import com.ilab.yougetmobiledl.ext.clickNoRepeat
import com.ilab.yougetmobiledl.ext.init
import com.ilab.yougetmobiledl.ext.requestPermission
import com.ilab.yougetmobiledl.ui.activity.MainActivity
import com.ilab.yougetmobiledl.utils.*
import com.ilab.yougetmobiledl.viewmodel.HomeViewModel
import com.ilab.yougetmobiledl.widget.MyProgress
import com.youth.banner.Banner
import com.youth.banner.adapter.BannerImageAdapter
import kotlinx.android.synthetic.main.home_fragment.*

@SuppressLint("SetTextI18n")
class HomeFragment : BaseFragment<HomeViewModel, HomeFragmentBinding>() {

    private val loading by lazy {
        MyProgress.create(requireContext())
            .setLabel("请稍候")
            .setDetailsLabel("")
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
    }

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
                    HomeViewModel.Status.FIND_VIDEO_INFO -> {
                        loading.refreshDetailLabel("寻找视频资源...")
                    }
                    HomeViewModel.Status.FIND_VIDEO_ERROR -> {
                        loading.refreshDetailLabel("寻找视频资源...失败")
                    }
                    HomeViewModel.Status.PARSE_VIDEO_ERROR -> {
                        loading.refreshDetailLabel("解析视频资源...失败")
                    }
                    HomeViewModel.Status.TIMEOUT_ERROR -> {
                        loading.refreshDetailLabel("请求超时")
                    }
                    HomeViewModel.Status.READY_FOR_DOWNLOAD -> {
                        loading.refreshDetailLabel("解析完毕，准备下载")
                    }
                    HomeViewModel.Status.ALREADY_DOWNLOAD -> {
                        loading.refreshDetailLabel("文件已在队列或已完成")
                    }
                    else -> {
                        loading.dismiss()
                    }
                }
            }
        }

        mViewModel.downloadInfo.observe(viewLifecycleOwner) {
            (requireActivity() as MainActivity).add(it)
        }

        button.clickNoRepeat {
            mViewModel.getVideoList("https://www.bilibili.com/video/BV1yv411H76y")
            loading.show()
        }

        btnDownload.clickNoRepeat {
            requestPermission(
                permission = Manifest.permission.WRITE_EXTERNAL_STORAGE,
                onGrant = {
                    etUrl.text.toString().trim().let { et ->
                        if (et.isNotEmpty() && !AppUtil.isUrl(et)) {
                            eventVM.globalToast.postValue("请输入正确的url格式")
                            return@requestPermission
                        }
                        if (et.isEmpty()) {
                            mViewModel.getVideoList("https://www.bilibili.com/video/BV1GV411p7P9")
                        } else {
                            mViewModel.getVideoList(et)
                        }
                        loading.show()
                    }
                },
                onRationale = {
                    eventVM.globalToast.postValue("您拒绝了权限")
                },
                onDeny = {
                    eventVM.globalToast.postValue("您即将前往设置菜单，并请授权")
                }
            )
        }
    }
}