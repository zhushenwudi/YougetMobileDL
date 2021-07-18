package com.ilab.yougetmobiledl.widget

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler

class MyLinearLayoutManager(context: Context?) : LinearLayoutManager(context) {
    override fun supportsPredictiveItemAnimations(): Boolean {
        return false
    }

    override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        }
    }

    override fun scrollVerticallyBy(dy: Int, recycler: Recycler, state: RecyclerView.State): Int {
        try {
            return super.scrollVerticallyBy(dy, recycler, state)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }
}