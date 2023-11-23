package com.merseyside.pagination

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver

class ScrollToPositionHelper(private val recyclerView: RecyclerView) {

    fun setPosition(position: Int) {
        val adapter = requireNotNull(recyclerView.adapter)
        if (adapter.itemCount != 0) {
            scroll(position)
        } else {
            val observer = object : AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    super.onItemRangeInserted(positionStart, itemCount)
                    if (itemCount - 1 >= position) {
                        scroll(position)
                        adapter.unregisterAdapterDataObserver(this)
                    }
                }
            }

            adapter.registerAdapterDataObserver(observer)
        }
    }

    private fun scroll(position: Int) {
        when(val layoutManager = recyclerView.layoutManager) {
            is LinearLayoutManager -> layoutManager.scrollToPositionWithOffset(position, 0)
            else -> throw UnsupportedOperationException()
        }
    }
}