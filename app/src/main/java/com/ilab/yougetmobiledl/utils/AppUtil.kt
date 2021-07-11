package com.ilab.yougetmobiledl.utils

import android.os.Environment
import android.provider.Settings
import dev.utils.app.ResourceUtils.getContentResolver
import java.util.regex.Pattern

object AppUtil {
    // 判断adb调试模式是否打开
    fun isADBEnable(): Boolean {
        return Settings.Secure.getInt(
            getContentResolver(),
            Settings.Secure.ADB_ENABLED,
            0
        ) > 0
    }

    fun isUrl(str: String): Boolean {
        val pattern = "(https?|http)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]"
        val matchers = Pattern.compile(pattern).matcher(str)
        if (matchers.find()) {
            return matchers.group(0) === str
        }
        return false
    }

    fun getSDCardPath(): String? {
        val sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
        if (sdCardExist) {
            return Environment.getExternalStorageDirectory().absolutePath
        }
        return null
    }
}