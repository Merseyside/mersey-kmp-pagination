package com.merseyside.pagination

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.merseyside.merseyLib.kotlin.observable.lifecycle.asLiveData
import com.merseyside.pagination.annotation.InternalPaginationApi
import com.merseyside.pagination.contract.OneWayPaginationContract
import com.merseyside.pagination.contract.TwoWayPaginationContract
import com.merseyside.pagination.parametrized.ParametrizedPagination
import com.merseyside.pagination.parametrized.ext.onResetOrPagingChangedEvent
import com.merseyside.pagination.positionSaver.PaginationPositionSaver

@OptIn(InternalPaginationApi::class)
open class PaginationHandler<Paging : OneWayPaginationContract<Data>, Data>(
    private val paging: Paging,
    loadItemsCountDownOffset: Int = 5,
    loadItemsCountUpOffset: Int = loadItemsCountDownOffset,
) : PaginationScrollHandler(loadItemsCountDownOffset, loadItemsCountUpOffset) {

    open val pagination: Paging = paging

    private val resetEventLiveData: LiveData<Unit>
    private var positionSaver: PaginationPositionSaver<*>? = null

    init {
        val resetEvent = if (paging is ParametrizedPagination<*, *, *>) {
            paging.onResetOrPagingChangedEvent()
        } else paging.onResetEvent

        resetEventLiveData = resetEvent.asLiveData()
    }

    override fun onRecyclerAttached(recyclerView: RecyclerView, lifecycleOwner: LifecycleOwner) {
        resetEventLiveData.observe(lifecycleOwner) { reset() }
        paging.notifyWhenReady { startingPosition ->
            startPaging(startingPosition)
        }
    }

    override fun onRecyclerDetached(recyclerView: RecyclerView, lifecycleOwner: LifecycleOwner) {
        paging.removeNotifyWhenReady()
        resetEventLiveData.removeObservers(lifecycleOwner)
    }

    override fun onLoadCurrentPage(onComplete: CompleteAction) {
        pagination.loadCurrentPage(onComplete)
    }

    override fun onLoadNextPage(onComplete: CompleteAction): Boolean {
        return pagination.loadNextPage(onComplete)
    }

    override fun onLoadPrevPage(onComplete: CompleteAction): Boolean {
        return if (pagination is TwoWayPaginationContract<*>) {
            (pagination as TwoWayPaginationContract<*>).loadPrevPage(onComplete)
        } else {
            onComplete()
            false
        }
    }

    override fun isEmptyState(): Boolean {
        return !pagination.isInitialPageLoaded
    }

    fun setPositionSaver(positionSaver: PaginationPositionSaver<*>?) {
        this.positionSaver = positionSaver
        if (positionSaver != null) {
            pagination.setOnSavePagingPositionCallback {
                requireRecycler {
                    val visibleItemPosition = getFirstVisibleItemPosition()
                    positionSaver.getPagingItemPosition(this, visibleItemPosition)
                }
            }
        }
    }

    fun restartPagination() {
        pagination.resetPaging()
    }

    fun withPaging(block: Paging.() -> Unit) {
        block(pagination)
    }
}