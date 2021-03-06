package com.ilab.yougetmobiledl.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.navigation.NavGraph
import androidx.navigation.NavGraphNavigator
import androidx.navigation.Navigation
import androidx.navigation.NavigatorProvider
import androidx.navigation.ui.NavigationUI
import com.ilab.yougetmobiledl.R
import com.ilab.yougetmobiledl.base.BaseActivity
import com.ilab.yougetmobiledl.base.eventVM
import com.ilab.yougetmobiledl.databinding.ActivityMainBinding
import com.ilab.yougetmobiledl.ext.exit
import com.ilab.yougetmobiledl.ext.interceptLongClick
import com.ilab.yougetmobiledl.model.DownloadInfo
import com.ilab.yougetmobiledl.model.DownloadedInfo
import com.ilab.yougetmobiledl.service.DownloadService
import com.ilab.yougetmobiledl.ui.fragment.HomeFragment
import com.ilab.yougetmobiledl.ui.fragment.VideoFragment
import com.ilab.yougetmobiledl.utils.*
import com.ilab.yougetmobiledl.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

@SuppressLint("SetTextI18n")
class MainActivity : BaseActivity<MainViewModel, ActivityMainBinding>() {
    private val navController by lazy { Navigation.findNavController(this, R.id.fragment) }
    private val mIntent by lazy { Intent(this@MainActivity, DownloadService::class.java) }

    override fun layoutId() = R.layout.activity_main

    override fun initView(savedInstanceState: Bundle?) {

        // 屏蔽长按点击
        bottomNavigationView.interceptLongClick(R.id.home_fragment, R.id.video_fragment)

        supportFragmentManager.findFragmentById(R.id.fragment)?.let {
            val fragmentNavigator = FragmentNavigatorHideShow(this, it.childFragmentManager, it.id)
            val provider = navController.navigatorProvider
            provider.addNavigator(fragmentNavigator)
            val navGraph = initNavGraph(provider, fragmentNavigator)
            navController.graph = navGraph
            bottomNavigationView.setOnNavigationItemSelectedListener { item ->
                navController.navigate(item.itemId)
                true
            }
        }

        NavigationUI.setupWithNavController(bottomNavigationView, navController)

        startService()
    }

    override fun createObserver() {
        eventVM.wifiConnected.observe(this) {
            if (!it) {
                eventVM.globalToast.value = "您处于非 wifi 环境，已暂停全部等待中的任务"
                pauseAll()
            }
        }
    }

    private fun initNavGraph(
        provider: NavigatorProvider,
        fragmentNavigator: FragmentNavigatorHideShow
    ): NavGraph {
        val navGraph = NavGraph(NavGraphNavigator(provider))

        // 用自定义的导航器来创建目的地
        val home = fragmentNavigator.createDestination()
        home.id = R.id.home_fragment
        HomeFragment::class.java.canonicalName?.let { home.className = it }
        navGraph.addDestination(home)

        val video = fragmentNavigator.createDestination()
        video.id = R.id.video_fragment
        VideoFragment::class.java.canonicalName?.let { video.className = it }
        navGraph.addDestination(video)

        // 设置导航首页
        navGraph.startDestination = R.id.home_fragment
        return navGraph
    }

    override fun onBackPressed() {
        val currentId = navController.currentDestination?.id
        val startId = navController.graph.startDestination
        // 如果当前目的地不是HomeFragment，则先回到HomeFragment
        if (currentId != startId) {
            bottomNavigationView.selectedItemId = startId
        } else {
            stopService(mIntent)
            exit()
        }
    }

    fun add(info: DownloadInfo) {
        mIntent.putExtra("msg", DownloadService.Event.ADD_ONE)
        mIntent.putExtra("downloadInfo", info)
        startService()
    }

    fun start(info: DownloadInfo) {
        mIntent.putExtra("msg", DownloadService.Event.START_ONE)
        mIntent.putExtra("downloadInfo", info)
        startService()
    }

    fun remove(info: DownloadInfo) {
        mIntent.putExtra("msg", DownloadService.Event.REMOVE_DOWNLOAD_ONE)
        mIntent.putExtra("downloadInfo", info)
        startService()
    }

    fun remove(info: DownloadedInfo) {
        mIntent.putExtra("msg", DownloadService.Event.REMOVE_DOWNLOADED_ONE)
        mIntent.putExtra("downloadedInfo", info)
        startService()
    }

    fun pause(info: DownloadInfo) {
        mIntent.putExtra("msg", DownloadService.Event.PAUSE_ONE)
        mIntent.putExtra("downloadInfo", info)
        startService()
    }

    fun convert(info: DownloadInfo) {
        mIntent.putExtra("msg", DownloadService.Event.CONVERT)
        mIntent.putExtra("downloadInfo", info)
        startService()
    }

    fun startAll() {
        mIntent.putExtra("msg", DownloadService.Event.START_ALL)
        startService()
    }

    private fun pauseAll() {
        mIntent.putExtra("msg", DownloadService.Event.PAUSE_ALL)
        startService()
    }

    private fun startService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(mIntent)
        } else {
            startService(mIntent)
        }
    }

    fun toSelfSetting() {
        val mIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        mIntent.data = Uri.fromParts("package", packageName, null)
        startActivity(mIntent)
    }

    fun jumpToGithub() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://github.com/zhushenwudi/YougetMobileDL")
        startActivity(intent)
    }

    fun playMedia(file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        val contentUri = FileProvider.getUriForFile(
            this,
            "$packageName.FileProvider",
            file
        )
        intent.setDataAndType(contentUri, "video/*")
        startActivity(intent)
    }
}