package com.merseyside.pagination

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.merseyside.merseyLib.kotlin.logger.Logger
import com.merseyside.merseyLib.kotlin.logger.log
import com.merseyside.merseyLib.kotlin.utils.ifFalse
import com.merseyside.merseyLib.kotlin.utils.ifTrue
import com.merseyside.merseyLib.kotlin.utils.safeLet
import com.merseyside.utils.layoutManager.findFirstVisibleItemPosition
import com.merseyside.utils.layoutManager.findLastVisibleItemPosition

abstract class PaginationScrollHandler(
    private val loadItemsCountNextOffset: Int,
    private val loadItemsCountPrevOffset: Int
) {

    var isPaging: Boolean = false
        private set

    private var recyclerView: RecyclerView? = null

    private val childStateListener: RecyclerView.OnChildAttachStateChangeListener =
        object : RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                requireRecycler {
                    val position = getChildAdapterPosition(view)
                    onPositionChanged(position)
                    loadNextPageIfNeed(position)
                    loadPrevPageIfNeed(position)
                }
            }

            override fun onChildViewDetachedFromWindow(view: View) {}
        }

    abstract fun onLoadCurrentPage()
    abstract fun onLoadNextPage()
    abstract fun onLoadPrevPage()

    abstract fun onPositionChanged(position: Int)

    fun setRecyclerView(recyclerView: RecyclerView?) {
        val prev = this.recyclerView
        this.recyclerView = recyclerView
        if (prev == null && isPaging) {
            startPaging()
        } else if (prev != null) {
            reset()
        }
    }

    fun startPaging() {
        isPaging = true
        ifRecyclerNotNull {
            onLoadCurrentPage()
        }
    }

    fun stopPaging() {
        reset()
    }

    fun <R> ifRecyclerNotNull(block: RecyclerView.() -> R): R? {
        return try {
            requireRecycler(block)
        } catch (_: NullPointerException) {
            Logger.logErr("Can not start paging because recycler didn't set!")
            null
        }
    }

    fun <R> requireRecycler(block: RecyclerView.() -> R): R {
        return recyclerView?.let {
            block(it)
        } ?: throw NullPointerException("Recycler view hasn't set!")
    }

    /**
     * Sets pagination to initial state
     * Also removes child state listener.
     * @see startPaging have to be called again.
     */
    protected fun reset() {
        isPaging = false
        recyclerView?.removeOnChildAttachStateChangeListener(childStateListener)
    }

    protected fun setMutableState(state: Boolean) {
        state.log("state")
        if (state) recyclerView?.removeOnChildAttachStateChangeListener(childStateListener)
        else recyclerView?.addOnChildAttachStateChangeListener(childStateListener)
    }

    private fun RecyclerView.loadMoreIfNeeds(complete: () -> Unit) {
        layoutManager?.let { manager ->
            loadNextPageIfNeed(manager.findLastVisibleItemPosition()).ifFalse(complete)
            loadPrevPageIfNeed(manager.findFirstVisibleItemPosition()).ifFalse(complete)
        }
    }

    private fun needToLoadNextPage(lastPosition: Int): Boolean = requireRecycler {
        val itemCount = adapter?.itemCount
        safeLet(itemCount) { counts ->
            (counts - lastPosition) <= loadItemsCountNextOffset
        } ?: false
    }

    private fun needToLoadPrevPage(firstPosition: Int): Boolean {
        return firstPosition == loadItemsCountPrevOffset
    }

    private fun loadNextPageIfNeed(position: Int): Boolean {
        return needToLoadNextPage(position).ifTrue {
            onLoadNextPage()
        }
    }

    private fun loadPrevPageIfNeed(position: Int): Boolean {
        return needToLoadPrevPage(position).ifTrue {
            onLoadPrevPage()
        }
    }
}