package com.ilab.yougetmobiledl.ui.fragment

import android.os.Bundle
import com.ilab.yougetmobiledl.R
import com.ilab.yougetmobiledl.base.BaseFragment
import com.ilab.yougetmobiledl.base.eventVM
import com.ilab.yougetmobiledl.databinding.VideoDownloadFragmentBinding
import com.ilab.yougetmobiledl.model.DownloadInfo
import com.ilab.yougetmobiledl.model.DownloadInfo.Companion.STATUS_ERROR
import com.ilab.yougetmobiledl.model.DownloadInfo.Companion.STATUS_NONE
import com.ilab.yougetmobiledl.model.DownloadInfo.Companion.STATUS_PREPARE_DOWNLOAD
import com.ilab.yougetmobiledl.ui.activity.MainActivity
import com.ilab.yougetmobiledl.ui.adapter.DownloadAdapter
import com.ilab.yougetmobiledl.utils.init
import com.ilab.yougetmobiledl.viewmodel.VideoDownloadingViewModel
import com.ilab.yougetmobiledl.widget.MyDiyDecoration
import com.ilab.yougetmobiledl.widget.MyLinearLayoutManager
import kotlinx.android.synthetic.main.video_download_fragment.*

class VideoDownloadingFragment :
    BaseFragment<VideoDownloadingViewModel, VideoDownloadFragmentBinding>() {

    private val activity by lazy { requireActivity() as MainActivity }
    private val mAdapter by lazy { DownloadAdapter() }

    override fun layoutId() = R.layout.video_download_fragment

    override fun initView(savedInstanceState: Bundle?) {

        eventVM.mutableDownloadTasks.observe(viewLifecycleOwner) {
            mAdapter.setList(it)
        }

        refreshLayout.setOnRefreshListener {
            mAdapter.setDiffNewData(eventVM.mutableDownloadTasks.value)
            refreshLayout.finishRefresh(500)
        }

        mAdapter.setOnItemClickListener { adapter, _, position ->
            val info = adapter.getItem(position) as DownloadInfo
            when (info.status) {
                STATUS_NONE -> activity.start(info)
                STATUS_PREPARE_DOWNLOAD -> activity.pause(info)
                STATUS_ERROR -> activity.start(info)
            }
        }

        recyclerView.init(MyLinearLayoutManager(requireContext()), mAdapter).run {
            val decoration = MyDiyDecoration()
            decoration.setColor(resources.getColor(R.color.lightGray))
            addItemDecoration(decoration)
        }
        mAdapter.setEmptyView(R.layout.layout_empty)

        refreshLayout.autoRefresh()
    }

    companion object {
        fun newInstance() = VideoDownloadingFragment()
    }
}