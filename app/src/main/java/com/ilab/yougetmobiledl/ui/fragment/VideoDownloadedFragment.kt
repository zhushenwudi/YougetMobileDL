package com.ilab.yougetmobiledl.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.content.FileProvider
import com.ilab.yougetmobiledl.R
import com.ilab.yougetmobiledl.base.BaseFragment
import com.ilab.yougetmobiledl.base.eventVM
import com.ilab.yougetmobiledl.databinding.VideoDownloadFragmentBinding
import com.ilab.yougetmobiledl.model.VideoInfo
import com.ilab.yougetmobiledl.ui.adapter.VideoAdapter
import com.ilab.yougetmobiledl.utils.init
import com.ilab.yougetmobiledl.viewmodel.VideoDownloadedViewModel
import com.ilab.yougetmobiledl.widget.MyDiyDecoration
import com.ilab.yougetmobiledl.widget.MyLinearLayoutManager
import kotlinx.android.synthetic.main.video_download_fragment.*
import java.io.File

class VideoDownloadedFragment :
    BaseFragment<VideoDownloadedViewModel, VideoDownloadFragmentBinding>() {

    private val mAdapter by lazy { VideoAdapter() }

    override fun layoutId() = R.layout.video_download_fragment

    override fun initView(savedInstanceState: Bundle?) {

        refreshLayout.setOnRefreshListener {
            mAdapter.setDiffNewData(eventVM.mutableDownloadedTasks.value)
            refreshLayout.finishRefresh(500)
        }

        eventVM.mutableDownloadedTasks.observe(viewLifecycleOwner) {
            mAdapter.setList(it)
        }

        mAdapter.setOnItemClickListener { adapter, _, position ->
            val intent = Intent(Intent.ACTION_VIEW)
            val file = File((adapter.data as MutableList<VideoInfo>)[position].path)
            val uri: Uri
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                val contentUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireActivity().packageName + ".FileProvider",
                    file
                )
                intent.setDataAndType(contentUri, "video/*")
            } else {
                uri = Uri.fromFile(file)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.setDataAndType(uri, "video/*")
            }

            startActivity(intent)
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
        fun newInstance() = VideoDownloadedFragment()
    }
}