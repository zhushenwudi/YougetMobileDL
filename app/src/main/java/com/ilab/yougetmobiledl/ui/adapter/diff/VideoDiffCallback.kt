package com.ilab.yougetmobiledl.ui.adapter.diff

import androidx.recyclerview.widget.DiffUtil
import com.ilab.yougetmobiledl.model.VideoInfo

class VideoDiffCallback : DiffUtil.ItemCallback<VideoInfo>() {
    override fun areItemsTheSame(oldItem: VideoInfo, newItem: VideoInfo): Boolean {
//        if (oldItem.itemType == newItem.itemType &&
//            newItem.itemType == VideoInfo.TYPE_DOWNLOADED) {
//            if (oldItem.name != newItem.name) {
//                return false
//            } else if (oldItem.path != newItem.path) {
//                return false
//            }
//            return true
//        } else return false
        return true
    }

    override fun areContentsTheSame(oldItem: VideoInfo, newItem: VideoInfo) = true
}