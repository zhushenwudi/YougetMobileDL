package com.ilab.yougetmobiledl.ui.fragment

import android.os.Bundle
import com.ilab.yougetmobiledl.R
import com.ilab.yougetmobiledl.base.BaseFragment
import com.ilab.yougetmobiledl.base.eventVM
import com.ilab.yougetmobiledl.databinding.VideoDownloadFragmentBinding
import com.ilab.yougetmobiledl.ext.init
import com.ilab.yougetmobiledl.model.DownloadInfo
import com.ilab.yougetmobiledl.model.DownloadInfo.Companion.STATUS_CONVERT_FAIL
import com.ilab.yougetmobiledl.model.DownloadInfo.Companion.STATUS_ERROR
import com.ilab.yougetmobiledl.model.DownloadInfo.Companion.STATUS_NONE
import com.ilab.yougetmobiledl.model.DownloadInfo.Companion.STATUS_PREPARE_DOWNLOAD
import com.ilab.yougetmobiledl.ui.activity.MainActivity
import com.ilab.yougetmobiledl.ui.adapter.DownloadAdapter
import com.ilab.yougetmobiledl.ui.adapter.diff.DownloadDiffCallback
import com.ilab.yougetmobiledl.viewmodel.VideoDownloadingViewModel
import com.ilab.yougetmobiledl.widget.MyDiyDecoration
import com.ilab.yougetmobiledl.widget.MyLinearLayoutManager
import com.mcxtzhang.swipemenulib.SwipeMenuLayout
import dev.utils.app.ClipboardUtils
import dev.utils.app.toast.ToastUtils
import kotlinx.android.synthetic.main.video_download_fragment.*

class VideoDownloadingFragment :
    BaseFragment<VideoDownloadingViewModel, VideoDownloadFragmentBinding>() {

    private val activity by lazy { requireActivity() as MainActivity }
    private val mAdapter by lazy { DownloadAdapter() }

    override fun layoutId() = R.layout.video_download_fragment

    override fun initView(savedInstanceState: Bundle?) {

        mAdapter.setDiffCallback(DownloadDiffCallback())

        refreshLayout.setOnRefreshListener {
            mAdapter.setDiffNewData(eventVM.mutableDownloadTasks.value)
            refreshLayout.finishRefresh()
        }

        mAdapter.setOnItemChildClickListener { adapter, view, position ->
            if (view.id == R.id.btnDel) {
                (view.parent as SwipeMenuLayout).quickClose()
                activity.remove(adapter.getItem(position) as DownloadInfo)
            } else {
                val info = adapter.getItem(position) as DownloadInfo
                when (info.status) {
                    // 未开始 -> 开始、删除
                    STATUS_NONE -> activity.start(info)
                    // 等待中 -> 暂停、删除
                    STATUS_PREPARE_DOWNLOAD -> activity.pause(info)
                    // 转换失败 -> 转换、删除
                    STATUS_CONVERT_FAIL -> activity.convert(info)
                    // 下载失败 -> 开始、删除
                    STATUS_ERROR -> activity.start(info)
                }
            }
        }

        mAdapter.setOnItemChildLongClickListener { adapter, _, position ->
            val info = adapter.getItem(position) as DownloadInfo
            ClipboardUtils.copyText(info.url)
            ToastUtils.showLong("URL地址已复制")
            true
        }

        recyclerView.init(MyLinearLayoutManager(requireContext()), mAdapter).run {
            val decoration = MyDiyDecoration()
            decoration.setColor(resources.getColor(R.color.lightGray))
            addItemDecoration(decoration)
        }

        mAdapter.setEmptyView(R.layout.layout_empty)
    }

    override fun createObserver() {
        eventVM.mutableDownloadTasks.observe(viewLifecycleOwner) {
            val tempList = mutableListOf<DownloadInfo>()
            it.forEach { d ->
                tempList.add(d.clone())
            }
            mAdapter.setDiffNewData(tempList)
        }
    }

    override fun initData() {
        refreshLayout.autoRefresh()
    }

    companion object {
        fun newInstance() = VideoDownloadingFragment()
    }
}