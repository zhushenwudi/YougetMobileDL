package com.ilab.yougetmobiledl.ui.fragment

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.ilab.yougetmobiledl.R
import com.ilab.yougetmobiledl.base.BaseFragment
import com.ilab.yougetmobiledl.databinding.VideoFragmentBinding
import com.ilab.yougetmobiledl.ext.clickNoRepeat
import com.ilab.yougetmobiledl.ext.interceptLongClick
import com.ilab.yougetmobiledl.ext.requestPermissions
import com.ilab.yougetmobiledl.ui.activity.MainActivity
import com.ilab.yougetmobiledl.viewmodel.VideoViewModel
import kotlinx.android.synthetic.main.video_fragment.*

class VideoFragment : BaseFragment<VideoViewModel, VideoFragmentBinding>() {

    override fun layoutId() = R.layout.video_fragment

    override fun initView(savedInstanceState: Bundle?) {
        mDatabind?.vm = mViewModel

        val height = (mActivity as MainActivity).statusBarHeight
        mViewModel.statusBarHeight.value = height

        btnPermission.clickNoRepeat {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri =
                Uri.fromParts("package", requireActivity().packageName, null)
            intent.data = uri
            startActivity(intent)
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
        val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        requestPermissions(
            permissions = permissions,
            onGrant = {
                viewpager2.visibility = View.VISIBLE
            },
            onRationale = {
                viewpager2.visibility = View.GONE
            },
            onDeny = {
                viewpager2.visibility = View.GONE
            }
        )
    }
}