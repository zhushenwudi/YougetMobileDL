package com.ilab.yougetmobiledl.viewmodel

import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.databinding.BindingAdapter
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel()

@BindingAdapter("android:layout_marginTop")
fun setTopMargin(view: View, topMargin: Int) {
    val layoutParams = view.layoutParams as MarginLayoutParams
    layoutParams.setMargins(
        layoutParams.leftMargin, topMargin,
        layoutParams.rightMargin, layoutParams.bottomMargin
    )
    view.layoutParams = layoutParams
}

@BindingAdapter("app:layout_width")
fun setLayoutWidth(view: View, width: Float) {
    val params = view.layoutParams
    params.height = width.toInt()
    view.layoutParams = params
}

@BindingAdapter("app:layout_height")
fun setLayoutHeight(view: View, height: Float) {
    val params = view.layoutParams
    params.height = height.toInt()
    view.layoutParams = params
}