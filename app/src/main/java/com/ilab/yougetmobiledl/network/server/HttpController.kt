package com.ilab.yougetmobiledl.network.server

import android.os.Build
import android.util.Log
import com.ilab.yougetmobiledl.utils.AppUtil
import com.yanzhenjie.andserver.annotation.GetMapping
import com.yanzhenjie.andserver.annotation.PostMapping
import com.yanzhenjie.andserver.annotation.RequestParam
import com.yanzhenjie.andserver.annotation.RestController
import com.yanzhenjie.andserver.framework.body.FileBody
import com.yanzhenjie.andserver.http.multipart.MultipartFile
import dev.utils.app.NetWorkUtils
import dev.utils.common.FileUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

@RestController
class HttpController {

    @GetMapping("/getDeviceName")
    fun getDeviceName(): JSONObject {
        val obj = JSONObject()
        obj.put("device", Build.DEVICE)
        val ip = NetWorkUtils.getIpAddressByWifi()
        Log.e("aaa", ip)
        obj.put("ip", "http://$ip:8080")
        return obj
    }

    @GetMapping("/list")
    fun getFileList(): JSONArray {
        val files = FileUtils.listFilesInDirWithFilter(AppUtil.getSDCardPath()) {
            it.name.endsWith("mp4") || it.name.endsWith("flv")
        }
        val arr = JSONArray()
        files?.forEach {
            val obj = JSONObject()
            obj.put("path", it.absolutePath)
            obj.put("name", it.name)
            obj.put("size", it.length())
            arr.put(obj)
        }
        return arr
    }

    @GetMapping("/download")
    fun downloadFile(@RequestParam("path") path: String): FileBody {
        return FileBody(File(path))
    }

    @PostMapping("/upload")
    fun uploadFile(@RequestParam("file") file: MultipartFile): Boolean {
        return try {
            file.transferTo(File("${AppUtil.getSDCardPath()}/${file.filename}"))
            true
        } catch (e: Exception) {
            false
        }
        // 更新数据库
    }

    @PostMapping("/delete")
    fun deleteFile(@RequestParam("path") path: String): Boolean {
        return FileUtils.deleteFile(path)
    }
}