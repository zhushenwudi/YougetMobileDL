package com.ilab.yougetmobiledl.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
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

fun Activity.openDevSetting() {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
        startActivity(intent)
    } catch (e: Exception) {
        try {
            val componentName = ComponentName(
                "com.android.settings",
                "com.android.settings.DevelopmentSettings"
            )
            val intent = Intent()
            intent.component = componentName
            intent.action = "android.intent.action.View"
            startActivity(intent)
        } catch (e1: Exception) {
            try {
                // 部分小米手机采用这种方式跳转
                val intent = Intent("com.android.settings.APPLICATION_DEVELOPMENT_SETTINGS")
                startActivity(intent)
            } catch (e2: Exception) {
            }
        }
    }
}