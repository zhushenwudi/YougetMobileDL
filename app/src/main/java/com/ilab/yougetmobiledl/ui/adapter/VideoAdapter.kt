package com.ilab.yougetmobiledl.ui.adapter

import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.ilab.yougetmobiledl.R
import com.ilab.yougetmobiledl.model.VideoInfo
import com.squareup.picasso.Picasso

class VideoAdapter : BaseQuickAdapter<VideoInfo, BaseViewHolder>(R.layout.item_type_downloaded) {

    override fun convert(holder: BaseViewHolder, item: VideoInfo) {
        holder.setText(R.id.name, item.name)
        holder.setText(R.id.totalSize, item.totalSize)
        if (item.photo.isNotEmpty()) {
            Picasso.get().load("file://" + item.photo)
                .into(holder.itemView.findViewById<ImageView>(R.id.photo))
        }
    }
}