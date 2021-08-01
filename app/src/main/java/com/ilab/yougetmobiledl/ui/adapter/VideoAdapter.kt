package com.ilab.yougetmobiledl.ui.adapter

import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.ilab.yougetmobiledl.R
import com.ilab.yougetmobiledl.model.DownloadedInfo
import com.squareup.picasso.Picasso

class VideoAdapter :
    BaseQuickAdapter<DownloadedInfo, BaseViewHolder>(R.layout.item_type_downloaded) {

    init {
        addChildClickViewIds(R.id.btnDel, R.id.clLayout)
        addChildLongClickViewIds(R.id.clLayout)
    }

    override fun convert(holder: BaseViewHolder, item: DownloadedInfo) {
        holder.setText(R.id.name, item.name)
        holder.setText(R.id.totalSize, item.totalSize)
        if (item.photo.isNotEmpty()) {
            Picasso.get().load(item.photo).fit()
                .into(holder.itemView.findViewById<ImageView>(R.id.photo))
        } else {
            Picasso.get().load(R.drawable.icon_holder).fit()
                .into(holder.itemView.findViewById<ImageView>(R.id.photo))
        }
    }
}