package com.ilab.yougetmobiledl.utils

import android.annotation.SuppressLint
import android.os.Environment
import android.provider.Settings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.utils.LogPrintUtils
import dev.utils.app.PathUtils
import dev.utils.app.ResourceUtils.getContentResolver
import dev.utils.app.image.BitmapUtils
import dev.utils.app.image.ImageUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

object AppUtil {
    private var mLastClick: Long = 0
    val gson = Gson()

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
            return PathUtils.getAppExternal().appDownloadPath
        }
        return null
    }

    // json 字符串 -> 对象
    inline fun <reified T> fromJson(json: String): T? {
        return try {
            val type = object : TypeToken<T>() {}.type
            return gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }

    // 对象 -> json 字符串
    inline fun <reified T> toJson(obj: T): String {
        return gson.toJson(obj)
    }

    // 倒计时
    fun countDownCoroutines(
        total: Int = 4, onTick: (Int) -> Unit = {}, onFinish: () -> Unit,
        scope: CoroutineScope = GlobalScope
    ): Job {
        return flow {
            for (i in total downTo 0) {
                emit(i)
                delay(1000)
            }
        }.flowOn(Dispatchers.Default)
            .onCompletion { onFinish.invoke() }
            .onEach { onTick.invoke(it) }
            .flowOn(Dispatchers.Main)
            .launchIn(scope)
    }

    // 输入搜索
    fun <T, R> searchFilter(
        stateFlow: MutableStateFlow<T>,
        filter: (T) -> Boolean,
        flatMap: (T) -> Flow<R>,
        onResult: (R) -> Unit,
        scope: CoroutineScope
    ) {
        stateFlow
            .debounce(400)
            .filter { filter.invoke(it) }
            .distinctUntilChanged()
            .flatMapLatest { flatMap.invoke(it) }
            .catch { LogPrintUtils.e("${it.message}") }
            .flowOn(Dispatchers.Default)
            .onEach { onResult.invoke(it) }
            .flowOn(Dispatchers.Main)
            .launchIn(scope)
    }

    // 数组深拷贝
    fun Array<*>.copy(): Array<*> {
        return this.copyOf()
    }

    // 字符串深拷贝
    fun String.copy(): String {
        return this + ""
    }

    // 获取 TAG
    fun Any.TAG(): String {
        return this::class.java.simpleName
    }

    // interval 定时器
    fun tickerFlow() = flow {
        while (true) {
            emit(Unit)
            delay(1000)
        }
    }.flowOn(Dispatchers.IO)

    // 函数节流
    fun funcThrottle(milliSeconds: Long = 500): Boolean {
        if (System.currentTimeMillis() - mLastClick <= milliSeconds) {
            return true
        }
        mLastClick = System.currentTimeMillis()
        return false
    }

    // 匹配是否为数字
    fun isNumeric(str: String): Boolean {
        // 该正则表达式可以匹配所有的数字 包括负数
        val pattern = Pattern.compile("-?[0-9]+(\\.[0-9]+)?")
        val bigStr = try {
            BigDecimal(str).toString()
        } catch (e: java.lang.Exception) {
            return false //异常 说明包含非数字。
        }
        val isNum = pattern.matcher(bigStr) // matcher是全匹配
        return isNum.matches()
    }

    @SuppressLint("SimpleDateFormat")
    fun convertTimestamp2Date(time: Long): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return simpleDateFormat.format(Date(time))
    }

    /***
     * 获取url参数列表;
     * @param url
     * @return Map
     */
    fun getUrlParamsMap(url: String): Map<String, String> {
        val params = url.substringAfter('?')
        val keyValues = params.split("&").toTypedArray()
        val result = mutableMapOf<String, String>()
        keyValues.forEach { keyValue ->
            val strSplit = keyValue.split("=").toTypedArray()
            result[strSplit[0]] = strSplit[1]
        }
        return result
    }

    /**
     * 获取封面
     */
    fun createCover(path: String, name: String): String {
        val bitmap = BitmapUtils.getVideoThumbnail(path)
        val coverPath = "${getSDCardPath()}/temp/${name}.png"
        ImageUtils.saveBitmapToSDCardPNG(bitmap, coverPath)
        bitmap.recycle()
        return coverPath
    }
}