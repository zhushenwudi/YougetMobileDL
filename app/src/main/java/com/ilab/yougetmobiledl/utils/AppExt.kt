package com.ilab.yougetmobiledl.utils

import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ftd.livepermissions.LivePermissions
import com.ftd.livepermissions.PermissionResult
import com.google.android.material.tabs.TabLayout
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx
import dev.utils.app.toast.ToastUtils
import kotlin.math.abs
import kotlin.system.exitProcess

// 最近一次点击的时间
private var mLastClickTime: Long = 0
private var exitTime = 0L
const val TV_EXIT = "再按一次退出程序"

// 最近一次点击的控件ID
private var mLastClickViewId = 0

fun Fragment.showToast(msg: String, time: Int = Toast.LENGTH_LONG) {
    Toast.makeText(requireContext(), msg, time).show()
}

fun exit() {
    if (System.currentTimeMillis() - exitTime > 2000L) {
        ToastUtils.showLong(TV_EXIT)
        exitTime = System.currentTimeMillis()
    } else {
        exitProcess(0)
    }
}

fun Fragment.requestPermission(
    permission: String,
    onGrant: () -> Unit = {},
    onRationale: () -> Unit = {},
    onDeny: () -> Unit = {}
) {
    LivePermissions(this).request(permission)
        .observe(viewLifecycleOwner) {
            when (it) {
                is PermissionResult.Grant -> {
                    onGrant.invoke()
                }
                is PermissionResult.Rationale -> {
                    onRationale.invoke()
                }
                is PermissionResult.Deny -> {
                    onDeny.invoke()
                }
            }
        }
}

fun Fragment.requestPermissions(
    permissions: Array<String>,
    onGrant: () -> Unit = {},
    onRationale: () -> Unit = {},
    onDeny: () -> Unit = {}
) {
    LivePermissions(this).requestArray(permissions)
        .observe(viewLifecycleOwner) {
            when (it) {
                is PermissionResult.Grant -> {
                    onGrant.invoke()
                }
                is PermissionResult.Rationale -> {
                    onRationale.invoke()
                }
                is PermissionResult.Deny -> {
                    onDeny.invoke()
                }
            }
        }
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

fun BottomNavigationViewEx.interceptLongClick(vararg ids: Int) {
    val bottomNavigationMenuView = this.getChildAt(0) as ViewGroup
    for (index in ids.indices) {
        bottomNavigationMenuView.getChildAt(index).findViewById<View>(ids[index])
            .setOnLongClickListener {
                true
            }
    }
}

fun TabLayout.Tab.interceptLongClick() {
    val view = this.view as ViewGroup
    view.setOnLongClickListener {
        true
    }
}