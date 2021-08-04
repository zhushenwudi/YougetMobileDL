package com.ilab.yougetmobiledl.network.server

import android.os.Build
import com.ilab.yougetmobiledl.base.eventVM
import com.ilab.yougetmobiledl.db.DBController
import com.ilab.yougetmobiledl.model.DownloadedInfo
import com.ilab.yougetmobiledl.utils.AppUtil
import com.yanzhenjie.andserver.annotation.*
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
        val ip = NetWorkUtils.getIpAddressByWifi()
        val obj = JSONObject()
        obj.put("device", Build.DEVICE)
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
            // 写入文件
            val path = "${AppUtil.getSDCardPath()}/${file.filename}"
            file.transferTo(File(path))
            // 更新数据库
            val pathLength = FileUtils.getFileLength(path).toDouble()
            val info = DownloadedInfo::class.java.newInstance()
            info.name = file.filename?.takeIf { it.contains('.') }?.let {
                return@let it.split('.')[0]
            } ?: System.currentTimeMillis().toString()
            info.path = path
            info.photo = AppUtil.createCover(info.path, info.name)
            info.totalSize = FileUtils.formatByteMemorySize(2, pathLength)
            info.type = 0
            DBController.createOrUpdate(info)
            // 更新视图
            val tempList = eventVM.mutableDownloadedTasks.value
            tempList?.add(info)
            eventVM.mutableDownloadedTasks.postValue(tempList)
            true
        } catch (e: Exception) {
            false
        }
    }

    @PostMapping("/delete")
    fun deleteFile(@RequestParam("path") path: String): Boolean {
        var isSuccess = false
        try {
            // 删除 视频文件
            if (!FileUtils.deleteFile(path)) {
                return isSuccess
            }
            // 通过 文件路径 找到 已下载 信息实体
            val downloadedInfo = DBController.findDownloadedInfoByPath(path)
            downloadedInfo?.let {
                // 如果封面是截图生成的，需要删除 png 文件
                if (!it.photo.startsWith("http")) {
                    FileUtils.listFilesInDir("${AppUtil.getSDCardPath()}/temp/")
                        .filter { f -> f.absolutePath.contains(it.name + ".png") }
                        .forEach { f -> FileUtils.deleteFile(f) }
                }

                // 删除 已下载 中记录
                DBController.delete(it)

                // 更新视图
                val tempList = eventVM.mutableDownloadedTasks.value
                if (tempList?.isNotEmpty() == true) {
                    val iterator = tempList.iterator()
                    while (iterator.hasNext()) {
                        val info = iterator.next()
                        if (info.id == it.id) {
                            iterator.remove()
                        }
                    }
                }
                eventVM.mutableDownloadedTasks.postValue(tempList)
            }
            isSuccess = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return isSuccess
    }
}