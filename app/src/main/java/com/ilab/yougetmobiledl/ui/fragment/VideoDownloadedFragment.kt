package com.ilab.yougetmobiledl.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.content.FileProvider
import com.ilab.yougetmobiledl.R
import com.ilab.yougetmobiledl.base.App
import com.ilab.yougetmobiledl.base.BaseFragment
import com.ilab.yougetmobiledl.databinding.VideoDownloadedFragmentBinding
import com.ilab.yougetmobiledl.model.VideoInfo
import com.ilab.yougetmobiledl.ui.adapter.VideoAdapter
import com.ilab.yougetmobiledl.utils.init
import com.ilab.yougetmobiledl.viewmodel.VideoDownloadedViewModel
import com.ilab.yougetmobiledl.widget.MyDiyDecoration
import com.ilab.yougetmobiledl.widget.MyLinearLayoutManager
import kotlinx.android.synthetic.main.video_downloaded_fragment.*
import java.io.File

class VideoDownloadedFragment :
    BaseFragment<VideoDownloadedViewModel, VideoDownloadedFragmentBinding>() {

    private val videoAdapter by lazy { VideoAdapter() }

    private val downloadedBox = App.boxStore.boxFor(VideoInfo::class.java)

    override fun layoutId() = R.layout.video_downloaded_fragment

    override fun initView(savedInstanceState: Bundle?) {

        refreshLayout.setOnRefreshListener {
            videoAdapter.setList(downloadedBox.all)
            refreshLayout.finishRefresh(true)
        }

        videoAdapter.setOnItemClickListener { adapter, view, position ->
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

        recyclerView.init(MyLinearLayoutManager(requireContext()), videoAdapter).run {
            val decoration = MyDiyDecoration()
            decoration.setColor(resources.getColor(R.color.lightGray))
            addItemDecoration(decoration)
        }

        videoAdapter.setList(downloadedBox.all)
    }

    companion object {
        fun newInstance() = VideoDownloadedFragment()
    }
}