package com.ilab.yougetmobiledl.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.ilab.yougetmobiledl.R
import com.ilab.yougetmobiledl.base.BaseFragment
import com.ilab.yougetmobiledl.base.Const.STORAGE_PERMISSION
import com.ilab.yougetmobiledl.base.eventVM
import com.ilab.yougetmobiledl.databinding.VideoFragmentBinding
import com.ilab.yougetmobiledl.ext.clickNoRepeat
import com.ilab.yougetmobiledl.ext.interceptLongClick
import com.ilab.yougetmobiledl.ext.requestPermission
import com.ilab.yougetmobiledl.ui.activity.WebActivity
import com.ilab.yougetmobiledl.viewmodel.VideoViewModel
import kotlinx.android.synthetic.main.video_fragment.*

class VideoFragment : BaseFragment<VideoViewModel, VideoFragmentBinding>() {

    override fun layoutId() = R.layout.video_fragment

    override fun initView(savedInstanceState: Bundle?) {
        mDatabind?.vm = mViewModel

        btnPermission.clickNoRepeat {
            requestPermission(STORAGE_PERMISSION)
        }

        ivTransform.clickNoRepeat {
            if (eventVM.wifiConnected.value == true) {
                startActivity(Intent(requireActivity(), WebActivity::class.java))
            } else {
                eventVM.globalToast.value = "请先打开 wifi"
            }
        }

        viewpager2.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = 2

            override fun createFragment(position: Int): Fragment {
                return if (position == 0) {
                    VideoDownloadedFragment.newInstance()
                } else {
                    VideoDownloadingFragment.newInstance()
                }
            }
        }

        val tabLayoutMediator = TabLayoutMediator(tabLayout, viewpager2) { tab, position ->
            tab.interceptLongClick()
            if (position == 0) {
                tab.text = "已下载"
            } else if (position == 1) {
                tab.text = "下载中"
            }
        }

        tabLayoutMediator.attach()

        requestPermission(STORAGE_PERMISSION)
    }

    override fun createObserver() {
        eventVM.isGrantedPermission.observe(viewLifecycleOwner) {
            if (it) {
                viewpager2.visibility = View.VISIBLE
            } else {
                viewpager2.visibility = View.GONE
            }
        }
    }
}