package com.merseyside.pagination.positionSaver

import androidx.recyclerview.widget.RecyclerView
import com.merseyside.merseyLib.kotlin.logger.log

abstract class PaginationPositionSaver<Adapter : RecyclerView.Adapter<*>> {

    @Suppress("UNCHECKED_CAST")
    internal fun getPagingItemPosition(recyclerView: RecyclerView, currentPosition: Int): Int {
        val adapter = requireNotNull(recyclerView.adapter)
        return getPagingItemPosition(adapter as Adapter, currentPosition)
    }

    abstract fun getPagingItemPosition(adapter: Adapter, currentPosition: Int): Int
}

class SimplePaginationPositionSaver(): PaginationPositionSaver<RecyclerView.Adapter<*>>() {
    override fun getPagingItemPosition(
        adapter: RecyclerView.Adapter<*>,
        currentPosition: Int
    ): Int {
        return currentPosition
    }

}