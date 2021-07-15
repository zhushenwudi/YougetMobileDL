package com.ilab.yougetmobiledl.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ilab.yougetmobiledl.R
import com.ilab.yougetmobiledl.databinding.VideoFragmentBinding
import com.ilab.yougetmobiledl.ui.activity.MainActivity
import com.ilab.yougetmobiledl.viewmodel.VideoViewModel

class VideoFragment : Fragment() {

    private lateinit var viewModel: VideoViewModel
    private lateinit var binding: VideoFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.video_fragment, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(VideoViewModel::class.java)
        binding.vm = viewModel

        val height = (activity as MainActivity).statusBarHeight
        viewModel.statusBarHeight.value = height
    }
}