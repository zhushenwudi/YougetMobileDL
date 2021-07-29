package com.ilab.yougetmobiledl.ui.adapter.diff

import android.os.Bundle
import androidx.recyclerview.widget.DiffUtil
import com.ilab.yougetmobiledl.model.DownloadInfo

class DownloadDiffCallback : DiffUtil.ItemCallback<DownloadInfo>() {
    override fun areItemsTheSame(oldItem: DownloadInfo, newItem: DownloadInfo): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DownloadInfo, newItem: DownloadInfo): Boolean {
        return oldItem.status == newItem.status
                && oldItem.percent == newItem.percent
    }

    override fun getChangePayload(oldItem: DownloadInfo, newItem: DownloadInfo): Any {
        val bundle = Bundle()
        bundle.putInt("status", newItem.status)
        bundle.putInt("percent", newItem.percent)
        return bundle
    }
}