package com.ilab.yougetmobiledl.ext

import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.youth.banner.Banner
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder

fun Banner<Int, BannerImageAdapter<Int>>.init(mutableList: List<Int>) {
    setAdapter(object : BannerImageAdapter<Int>(mutableList) {
        override fun onBindView(holder: BannerImageHolder, image: Int, position: Int, size: Int) {
            Picasso.get().load(image).into(holder.imageView)
        }
    }).addBannerLifecycleObserver(this.findViewTreeLifecycleOwner())
}

// 绑定普通的Recyclerview
fun RecyclerView.init(
    layoutManger: RecyclerView.LayoutManager,
    bindAdapter: RecyclerView.Adapter<*>,
    isScroll: Boolean = true
): RecyclerView {
    layoutManager = layoutManger
    setHasFixedSize(true)
    adapter = bindAdapter
    isNestedScrollingEnabled = isScroll
    return this
}