package com.ilab.yougetmobiledl.utils

import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs

// 最近一次点击的时间
private var mLastClickTime: Long = 0

// 最近一次点击的控件ID
private var mLastClickViewId = 0

fun AppCompatActivity.showToast(msg: String, time: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, msg, time).show()
}

fun View.clickNoRepeat(
    interval: Long = 500,
    withOthers: Boolean = false,
    action: (view: View) -> Unit
) {
    setOnClickListener {
        if (!isFastDoubleClick(it, interval, withOthers)) {
            action(it)
        }
    }
}

private fun isFastDoubleClick(v: View, intervalMillis: Long, withOthers: Boolean = false): Boolean {
    val viewId = v.id
    val time = System.currentTimeMillis()
    val timeInterval = abs(time - mLastClickTime)
    return if ((withOthers && timeInterval < intervalMillis)
        || (!withOthers && timeInterval < intervalMillis && viewId == mLastClickViewId)
    )
        true
    else {
        mLastClickTime = time
        mLastClickViewId = viewId
        false
    }
}