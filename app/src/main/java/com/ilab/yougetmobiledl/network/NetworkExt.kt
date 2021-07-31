package com.ilab.yougetmobiledl.network

import android.net.ParseException
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import com.ilab.yougetmobiledl.base.BaseViewModel
import com.ilab.yougetmobiledl.base.eventVM
import dev.utils.LogPrintUtils
import kotlinx.coroutines.*
import org.apache.http.conn.ConnectTimeoutException
import org.json.JSONException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

const val NO_NETWORK = "请先连接网络"
const val AUTHOR_VERIFY_FAIL = "认证失败，请重新登录"
const val SERVER_ERROR = "服务繁忙，请稍后再试"
const val UNKNOW_ERROR = "未知错误"
const val DNS_ERROR = "网络域名配置错误"
const val DATA_ERROR = "请联系管理员查看数据格式"
const val SSL_ERROR = "证书出错，请稍后再试"
const val TIMEOUT_ERROR = "网络连接超时，请稍后重试"
const val NETWORK_ERROR = "网络连接错误，请稍后重试"

/**
 * 过滤服务器结果，失败抛异常
 * @param block 请求体方法，必须要用suspend关键字修饰
 * @param success 成功回调
 * @param error 失败回调 可不传
 * @param isShowDialog 是否显示加载框
 * @param loadingMessage 加载框提示内容
 */
fun <T> BaseViewModel.request(
    block: suspend () -> ApiResponse<T>,
    success: (T) -> Unit,
    error: (String) -> Unit = {},
    isShowDialog: Boolean = true,
    isShowError: Boolean = true,
    loadingMessage: String = "请求网络中..."
): Job {
    //如果需要弹窗 通知Activity/fragment弹窗
    return viewModelScope.launch {
        runCatching {
            if (isShowDialog) withContext(Dispatchers.Main) {
                loadingChange.showDialog.value = loadingMessage
            }
            //请求体
            block()
        }.onSuccess {
            //网络请求成功 关闭弹窗
            if (isShowDialog) withContext(Dispatchers.Main) {
                loadingChange.dismissDialog.value = false
            }
            runCatching {
                //校验请求结果码是否正确，不正确会抛出异常走下面的onFailure
                executeResponse(it) { t -> success(t) }
            }.onFailure { e ->
                e.printStackTrace()
                //失败回调
                error(parseNetError(isShowError, e))
            }
        }.onFailure {
            //网络请求异常 关闭弹窗
            if (isShowDialog) withContext(Dispatchers.Main) {
                loadingChange.dismissDialog.value = false
            }
            it.printStackTrace()
            //失败回调
            error(parseNetError(isShowError, it))
        }
    }
}

/**
 *  不过滤请求结果
 * @param block 请求体 必须要用suspend关键字修饰
 * @param success 成功回调
 * @param error 失败回调 可不给
 * @param isShowDialog 是否显示加载框
 * @param loadingMessage 加载框提示内容
 */
fun <T> BaseViewModel.requestNoCheck(
    block: suspend () -> T,
    success: (T) -> Unit,
    error: (String) -> Unit = {},
    isShowDialog: Boolean = false,
    loadingMessage: String = "请求网络中..."
): Job {
    //如果需要弹窗 通知Activity/fragment弹窗
    return viewModelScope.launch {
        runCatching {
            if (isShowDialog) withContext(Dispatchers.Main) {
                loadingChange.showDialog.value = loadingMessage
            }
            //请求体
            block()
        }.onSuccess {
            //网络请求成功 关闭弹窗
            withContext(Dispatchers.Main) { loadingChange.dismissDialog.value = false }
            //成功回调
            success(it)
        }.onFailure {
            //网络请求异常 关闭弹窗
            withContext(Dispatchers.Main) { loadingChange.dismissDialog.value = false }
            //失败回调
            error(parseNetError(isShowDialog, it))
        }
    }
}

/**
 * 请求结果过滤，判断请求服务器请求结果是否成功，不成功则会抛出异常
 */
suspend fun <T> executeResponse(
    response: ApiResponse<T>,
    success: suspend CoroutineScope.(T) -> Unit
) {
    coroutineScope {
        when {
            response.isSuccess() -> {
                success(response.getRespData())
            }
            else -> {
                response.getRespMsg()?.let { LogPrintUtils.e(it) }
                throw Throwable(response.getRespMsg())
            }
        }
    }
}

fun parseNetError(isShowError: Boolean, e: Throwable): String {
    var resp: String
    when (e) {
        is NetworkApi.NoNetworkException -> resp = NO_NETWORK
        is ConnectException -> resp = NETWORK_ERROR
        is HttpException -> {
            e.response()?.code().let {
                resp = when (it) {
                    401 -> AUTHOR_VERIFY_FAIL
                    else -> "$SERVER_ERROR - $it"
                }
            }
        }
        is UnknownHostException -> resp = DNS_ERROR
        is JsonParseException, is JSONException, is ParseException, is MalformedJsonException -> resp =
            DATA_ERROR
        is SSLException -> resp = SSL_ERROR
        is ConnectTimeoutException, is SocketTimeoutException -> resp = TIMEOUT_ERROR
        else -> resp = e.message ?: UNKNOW_ERROR
    }

    if (isShowError) {
        eventVM.requestFailed.value = resp
    }
    return resp
}
