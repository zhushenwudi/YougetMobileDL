package com.ilab.yougetmobiledl.ui.fragment

import android.os.Bundle
import android.util.Log
import com.ilab.yougetmobiledl.R
import com.ilab.yougetmobiledl.base.BaseFragment
import com.ilab.yougetmobiledl.base.eventVM
import com.ilab.yougetmobiledl.databinding.VideoDownloadingFragmentBinding
import com.ilab.yougetmobiledl.viewmodel.VideoDownloadingViewModel

class VideoDownloadingFragment :
    BaseFragment<VideoDownloadingViewModel, VideoDownloadingFragmentBinding>() {

    override fun layoutId() = R.layout.video_downloading_fragment

    override fun initView(savedInstanceState: Bundle?) {

        eventVM.currentDownloadTask.observe(viewLifecycleOwner) {
            val info = it.getInfo()
            Log.e("aaa", "${info.name}: ${info.speed} , ${info.percent}, ${info.status}")
        }
    }

    companion object {
        fun newInstance() = VideoDownloadingFragment()
    }
}