package com.ilab.yougetmobiledl.ui.fragment

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.ilab.yougetmobiledl.R
import com.ilab.yougetmobiledl.base.BaseFragment
import com.ilab.yougetmobiledl.base.eventVM
import com.ilab.yougetmobiledl.databinding.VideoDownloadFragmentBinding
import com.ilab.yougetmobiledl.ext.init
import com.ilab.yougetmobiledl.model.DownloadedInfo
import com.ilab.yougetmobiledl.ui.activity.MainActivity
import com.ilab.yougetmobiledl.ui.adapter.VideoAdapter
import com.ilab.yougetmobiledl.viewmodel.VideoDownloadedViewModel
import com.ilab.yougetmobiledl.widget.MyDiyDecoration
import com.ilab.yougetmobiledl.widget.MyLinearLayoutManager
import com.mcxtzhang.swipemenulib.SwipeMenuLayout
import dev.utils.app.DialogUtils
import dev.utils.common.FileUtils
import kotlinx.android.synthetic.main.video_download_fragment.*
import java.io.File

class VideoDownloadedFragment :
    BaseFragment<VideoDownloadedViewModel, VideoDownloadFragmentBinding>() {

    private val activity by lazy { requireActivity() as MainActivity }
    private val mAdapter by lazy { VideoAdapter() }
    private var dialog: AlertDialog? = null

    override fun layoutId() = R.layout.video_download_fragment

    override fun initView(savedInstanceState: Bundle?) {

        refreshLayout.setOnRefreshListener {
            mAdapter.setList(eventVM.mutableDownloadedTasks.value)
            refreshLayout.finishRefresh()
        }

        mAdapter.setOnItemChildClickListener { adapter, view, position ->
            if (view.id == R.id.btnDel) {
                (view.parent as SwipeMenuLayout).quickClose()
                activity.remove(adapter.getItem(position) as DownloadedInfo)
            } else {
                val intent = Intent(Intent.ACTION_VIEW)
                val info = adapter.getItem(position) as DownloadedInfo
                val file = File(info.path)
                if (FileUtils.isFileExists(file)) {
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
                } else {
                    dialog = DialogUtils.createAlertDialog(
                        requireContext(), "未找到该视频文件", "是否删除该条记录",
                        "确定", "取消", object : DialogUtils.DialogListener() {
                            override fun onLeftButton(dialog: DialogInterface?) {
                                activity.remove(info)
                            }

                            override fun onRightButton(dialog: DialogInterface?) {}
                        })
                    dialog?.show()
                }
            }
        }

        mAdapter.setOnItemChildLongClickListener { adapter, _, position ->
            val info = adapter.getItem(position) as DownloadedInfo
            // TODO: 视频展示页
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
        eventVM.mutableDownloadedTasks.observe(viewLifecycleOwner) {
            mAdapter.setList(it)
        }
    }

    override fun initData() {
        refreshLayout.autoRefresh()
    }

    override fun onPause() {
        super.onPause()
        dialog?.hide()
        dialog = null
    }

    companion object {
        fun newInstance() = VideoDownloadedFragment()
    }
}