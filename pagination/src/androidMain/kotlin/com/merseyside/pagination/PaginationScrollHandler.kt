package com.merseyside.pagination

import android.view.View
import androidx.core.view.doOnAttach
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.merseyside.merseyLib.kotlin.logger.Logger
import com.merseyside.merseyLib.kotlin.logger.log
import com.merseyside.merseyLib.kotlin.utils.ifTrue
import com.merseyside.merseyLib.kotlin.utils.safeLet
import com.merseyside.utils.layoutManager.findFirstVisibleItemPosition
import com.merseyside.utils.layoutManager.findLastVisibleItemPosition

abstract class PaginationScrollHandler(
    private val loadItemsCountNextOffset: Int,
    private val loadItemsCountPrevOffset: Int
) {

    private var noNextPage: Boolean = false
    private var noPrevPage: Boolean = false

    private var lifecycleOwner: LifecycleOwner? = null

    var isPaging: Boolean = false
        private set

    private var isStartingPageLoading: Boolean = false

    private var isPrevPageLoading = false
    private var isNextPageLoading = false

    private var isSetScrollPositionEnabled: Boolean = true
    private var scrollToPositionHelper: ScrollToPositionHelper? = null

    protected var recyclerView: RecyclerView? = null
        private set

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            detachRecyclerView()
        }
    }

    private val childStateListener: RecyclerView.OnChildAttachStateChangeListener =
        object : RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                if (!isStartingPageLoading) {
                    requireRecycler {
                        val position = lazy { getChildAdapterPosition(view) }
                        onItemAttached(position)
                        if (!noNextPage) loadNextPageIfNeed(position)
                        if (!noPrevPage) loadPrevPageIfNeed(position)
                    }
                }
            }

            override fun onChildViewDetachedFromWindow(view: View) {
                requireRecycler {
                    val position = lazy { getChildAdapterPosition(view) }
                    onItemDetached(position)
                }
            }
        }

    protected abstract fun onLoadCurrentPage(onComplete: CompleteAction)
    protected abstract fun onLoadNextPage(onComplete: CompleteAction): Boolean
    protected abstract fun onLoadPrevPage(onComplete: CompleteAction): Boolean
    protected abstract fun isEmptyState(): Boolean

    protected open fun onItemAttached(position: Lazy<Int>) {}
    protected open fun onItemDetached(position: Lazy<Int>) {}

    fun setRecyclerView(recyclerView: RecyclerView?) {
        if (this.recyclerView == recyclerView) return
        detachRecyclerView()

        if (recyclerView != null) {
            attachRecyclerView(recyclerView)
        }
    }

    fun enableSetScrollPosition(enabled: Boolean) {
        isSetScrollPositionEnabled = enabled
    }

    protected abstract fun onRecyclerAttached(
        recyclerView: RecyclerView,
        lifecycleOwner: LifecycleOwner
    )

    protected abstract fun onRecyclerDetached(
        recyclerView: RecyclerView,
        lifecycleOwner: LifecycleOwner
    )

    private fun attachRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView

        if (isSetScrollPositionEnabled) {
            scrollToPositionHelper = ScrollToPositionHelper(recyclerView)
        }

        recyclerView.doOnAttach {
            lifecycleOwner = recyclerView.findViewTreeLifecycleOwner()
            requireLifecycleOwner().lifecycle.addObserver(lifecycleObserver)
            onRecyclerAttached(recyclerView, requireLifecycleOwner())
        }
    }

    private fun detachRecyclerView() {
        safeLet(recyclerView) { recycler ->
            onRecyclerDetached(recycler, requireLifecycleOwner())

            scrollToPositionHelper = null
            requireLifecycleOwner().lifecycle.removeObserver(lifecycleObserver)
            lifecycleOwner = null
            this.recyclerView = null
        }
    }

    protected fun startPaging(startingPosition: Int = 0) {
        startingPosition.log(prefix = "starting pos =")
        if (!isPaging) {
            isPaging = true
            requireRecycler {
                scrollToPositionHelper?.setPosition(startingPosition)
                if (isEmptyState()) mutableState { onComplete ->
                    onLoadCurrentPage(onComplete)
                } else {
                    enableObservingAttachedViews()
                }
            }
        } else Logger.logInfo("Already in active state")
    }

    fun stopPaging() {
        reset()
    }

    fun getFirstVisibleItemPosition(): Int {
        return requireRecycler {
            requireNotNull(layoutManager).findFirstVisibleItemPosition()
        }
    }

    fun getLastVisibleItemPosition(): Int {
        return requireRecycler {
            requireNotNull(layoutManager).findLastVisibleItemPosition()
        }
    }

    protected inline fun <R> requireRecycler(block: RecyclerView.() -> R): R {
        return recyclerView?.let {
            block(it)
        } ?: throw NullPointerException("Recycler view hasn't set!")
    }

    /**
     * Sets pagination to initial state
     * Also removes child state listener.
     * @see startPaging have to be called again.
     */
    protected open fun reset() {
        isPaging = false
        noNextPage = false
        noPrevPage = false
        isNextPageLoading = false
        isPrevPageLoading = false
        recyclerView?.removeOnChildAttachStateChangeListener(childStateListener)
    }

    private inline fun mutableState(mutate: (onComplete: CompleteAction) -> Unit) {
        isStartingPageLoading = true
        disableObservingAttachedViews()
        mutate {
            enableObservingAttachedViews()
            isStartingPageLoading = false
        }
    }

    private fun needToLoadNextPage(lastPosition: Int): Boolean = requireRecycler {
        if (lastPosition == -1) return false
        val itemCount = adapter?.itemCount
        safeLet(itemCount) { counts ->
            (counts - lastPosition) <= loadItemsCountNextOffset
        } ?: false
    }

    private fun needToLoadPrevPage(firstPosition: Int): Boolean {
        return firstPosition != -1 && firstPosition <= loadItemsCountPrevOffset
    }

    private fun loadNextPageIfNeed(position: Lazy<Int>) {
        if (isNextPageLoading) return
        needToLoadNextPage(position.value).ifTrue {
            isNextPageLoading = true
            noNextPage = !onLoadNextPage {
                isNextPageLoading = false
            }
        }
    }

    private fun loadPrevPageIfNeed(position: Lazy<Int>) {
        if (isPrevPageLoading) return
        needToLoadPrevPage(position.value).ifTrue {
            isPrevPageLoading = true
            noPrevPage = !onLoadPrevPage {
                isPrevPageLoading = false
            }
        }
    }

    private fun enableObservingAttachedViews() {
        recyclerView?.addOnChildAttachStateChangeListener(childStateListener)
    }

    private fun disableObservingAttachedViews() {
        recyclerView?.removeOnChildAttachStateChangeListener(childStateListener)
    }

    private fun requireLifecycleOwner() = requireNotNull(lifecycleOwner)
}