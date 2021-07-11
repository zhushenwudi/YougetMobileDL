package com.ilab.yougetmobiledl.utils

import android.provider.Settings
import dev.utils.app.ResourceUtils.getContentResolver

object AppUtil {
    // 判断adb调试模式是否打开
    fun isADBEnable(): Boolean {
        return Settings.Secure.getInt(
            getContentResolver(),
            Settings.Secure.ADB_ENABLED,
            0
        ) > 0
    }
}