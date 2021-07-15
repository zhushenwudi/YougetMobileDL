package com.ilab.yougetmobiledl.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.ilab.yougetmobiledl.R
import com.ilab.yougetmobiledl.databinding.ActivityMainBinding
import com.ilab.yougetmobiledl.viewmodel.MainViewModel
import dev.utils.app.ScreenUtils.getStatusBarHeight
import kotlinx.android.synthetic.main.activity_main.*

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    private var mBinding: ActivityMainBinding? = null
    private var mViewModel: MainViewModel? = null
    val statusBarHeight by lazy { getStatusBarHeight() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        val navController = Navigation.findNavController(this, R.id.fragment)
        NavigationUI.setupWithNavController(bottomNavigationView, navController)
    }
}