package com.ilab.yougetmobiledl.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import com.ilab.yougetmobiledl.R
import com.ilab.yougetmobiledl.base.BaseFragment
import com.ilab.yougetmobiledl.base.eventVM
import com.ilab.yougetmobiledl.databinding.HomeFragmentBinding
import com.ilab.yougetmobiledl.ext.clickNoRepeat
import com.ilab.yougetmobiledl.ext.init
import com.ilab.yougetmobiledl.ext.requestPermission
import com.ilab.yougetmobiledl.model.Episode
import com.ilab.yougetmobiledl.ui.activity.MainActivity
import com.ilab.yougetmobiledl.utils.*
import com.ilab.yougetmobiledl.viewmodel.HomeViewModel
import com.ilab.yougetmobiledl.widget.MyProgress
import com.youth.banner.Banner
import com.youth.banner.adapter.BannerImageAdapter
import dev.utils.app.DialogUtils
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

        button.clickNoRepeat {
//            mViewModel.getVideoList("https://b23.tv/VkWj3j")
            mViewModel.getVideoList("https://www.bilibili.com/bangumi/play/ep409584")
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
//                            mViewModel.getVideoList("https://www.bilibili.com/video/BV1GV411p7P9")
                            mViewModel.getVideoList("https://www.bilibili.com/bangumi/play/ss38931")
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

    override fun createObserver() {
        mViewModel.downloadInfo.observe(viewLifecycleOwner) {
            (requireActivity() as MainActivity).add(it)
        }

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
                    HomeViewModel.Status.ONLY_VIP -> {
                        loading.refreshDetailLabel("需要大会员才能下载")
                    }
                    else -> {
                        loading.dismiss()
                    }
                }
            }
        }

        mViewModel.chooseEP.observe(viewLifecycleOwner) { bangumi ->
            val items = mutableListOf<String>()
            val episodes = bangumi.episodes
            val checkedItems = mutableListOf<Boolean>()
            episodes.forEach { ep ->
                items.add("第${ep.title}话 ${ep.long_title}")
                checkedItems.add(false)
            }
            DialogUtils.createMultiChoiceDialog(
                requireContext(),
                items.toTypedArray(),
                checkedItems.toBooleanArray(),
                bangumi.season_title,
                null,
                "下载",
                object : DialogUtils.MultiChoiceListener() {
                    override fun onPositiveButton(
                        dialog: DialogInterface?,
                        checkedItems: BooleanArray
                    ) {
                        val chooseList = mutableListOf<Episode>()
                        checkedItems.forEachIndexed { index, isChoose ->
                            if (isChoose) {
                                chooseList.add(episodes[index])
                            }
                        }
                        mViewModel.getBangumiHighDigitalStream(chooseList)
                    }
                }
            ).show()
        }

    }
}