package com.ilab.yougetmobiledl.utils

import androidx.lifecycle.findViewTreeLifecycleOwner
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