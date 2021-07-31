package com.ilab.yougetmobiledl.ui.adapter

import android.os.Bundle
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.ilab.yougetmobiledl.R
import com.ilab.yougetmobiledl.model.DownloadInfo
import com.ilab.yougetmobiledl.model.DownloadInfo.Companion.STATUS_CONVERT
import com.ilab.yougetmobiledl.model.DownloadInfo.Companion.STATUS_DOWNLOADING
import com.ilab.yougetmobiledl.model.DownloadInfo.Companion.STATUS_NONE
import com.ilab.yougetmobiledl.model.DownloadInfo.Companion.STATUS_PREPARE_DOWNLOAD
import com.squareup.picasso.Picasso

class DownloadAdapter :
    BaseQuickAdapter<DownloadInfo, BaseViewHolder>(R.layout.item_type_downloading) {

    init {
        addChildClickViewIds(R.id.btnDel, R.id.clLayout)
        addChildLongClickViewIds(R.id.clLayout)
    }

    override fun convert(holder: BaseViewHolder, item: DownloadInfo) {
        holder.setText(R.id.name, item.name)
        if (item.pic.isNotEmpty()) {
            Picasso.get().load(item.pic).fit()
                .into(holder.itemView.findViewById<ImageView>(R.id.photo))
        } else {
            Picasso.get().load(R.drawable.icon_holder).fit()
                .into(holder.itemView.findViewById<ImageView>(R.id.photo))
        }
        holder.setText(R.id.totalSize, item.totalSize)
        setStatus(holder, item)
        holder.setGone(R.id.btnDel, item.status == STATUS_DOWNLOADING)
    }

    override fun convert(holder: BaseViewHolder, item: DownloadInfo, payloads: List<Any>) {
        if (payloads.isNotEmpty()) {
            val bundle = payloads[0] as Bundle
            item.status = bundle.getInt("status")
            item.percent = bundle.getInt("percent")
            setStatus(holder, item)
            holder.setGone(R.id.btnDel, item.status == STATUS_DOWNLOADING)
        }
    }

    private fun setStatus(holder: BaseViewHolder, item: DownloadInfo) {
        val tvStatus = when (item.status) {
            STATUS_NONE -> "未开始"
            STATUS_PREPARE_DOWNLOAD -> "等待中"
            STATUS_DOWNLOADING -> "下载中" + if (item.percent == 0) "" else "${item.percent}%"
            STATUS_CONVERT -> "转换格式中"
            else -> "下载失败"
        }
        holder.setText(R.id.status, tvStatus)
    }
}