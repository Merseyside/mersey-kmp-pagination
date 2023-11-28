package com.merseyside.pagination

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.merseyside.merseyLib.kotlin.logger.ILogger
import com.merseyside.merseyLib.kotlin.logger.Logger

class ScrollToPositionHelper(private val recyclerView: RecyclerView): ILogger {

    fun setPosition(position: Int) {
        val adapter = recyclerView.adapter

        if (adapter == null) {
            Logger.logInfo(tag, "Adapter hasn't been set! Position restoring is impossible.")
            return
        }

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

    override val tag: String
        get() = "ScrollToPositionHelper"
}