package com.merseyside.pagination

import com.merseyside.merseyLib.kotlin.entity.result.isLoading
import com.merseyside.pagination.contract.PaginationContract

open class PaginationHandler<Paging : PaginationContract<Data>, Data>(
    paging: Paging,
    private val saveScrollPosition: Boolean = false,
    loadItemsCountDownOffset: Int = 5,
    loadItemsCountUpOffset: Int = loadItemsCountDownOffset,
) : PaginationScrollHandler(loadItemsCountDownOffset, loadItemsCountUpOffset) {

    open val pagination: Paging = paging

    init {
        paging.onResetEvent.observe { reset() }
        paging.onStateChangedEvent.observe(ignoreCurrent = true) { state ->
            setMutableState(state.isLoading())
        }
    }

    override fun onLoadCurrentPage() {
        pagination.loadCurrentPage()
    }

    override fun onLoadNextPage() {
        pagination.loadNextPage()
    }

    override fun onLoadPrevPage() {}
    override fun onItemAttached(position: Int) {
        if (saveScrollPosition) pagination.setCurrentPosition(position)
    }

    override fun onItemDetached(position: Int) {}

    fun resetPagination() {
        pagination.resetPaging()
    }

    fun withPaging(block: Paging.() -> Unit) {
        block(pagination)
    }
}