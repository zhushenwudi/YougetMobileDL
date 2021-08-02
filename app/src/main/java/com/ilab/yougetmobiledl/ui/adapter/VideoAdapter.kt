package com.ilab.yougetmobiledl.ui.adapter

import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.ilab.yougetmobiledl.R
import com.ilab.yougetmobiledl.model.DownloadedInfo

class VideoAdapter :
    BaseQuickAdapter<DownloadedInfo, BaseViewHolder>(R.layout.item_type_downloaded) {

    init {
        addChildClickViewIds(R.id.btnDel, R.id.clLayout)
        addChildLongClickViewIds(R.id.clLayout)
    }

    override fun convert(holder: BaseViewHolder, item: DownloadedInfo) {
        holder.setText(R.id.name, item.name)
        holder.setText(R.id.totalSize, item.totalSize)
        Glide.with(holder.itemView)
            .load(item.photo).fitCenter()
            .placeholder(R.drawable.icon_holder)
            .into(holder.itemView.findViewById(R.id.photo))
    }
}