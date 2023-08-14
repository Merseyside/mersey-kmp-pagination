package com.merseyside.pagination

import com.merseyside.pagination.contract.PaginationContract

open class PaginationHandler<Paging : PaginationContract<Data>, Data>(
    paging: Paging,
    loadItemsCountDownOffset: Int = 5,
    loadItemsCountUpOffset: Int = loadItemsCountDownOffset,
) : PaginationScrollHandler(loadItemsCountDownOffset, loadItemsCountUpOffset) {

    open val pagination: Paging = paging

    init {
        paging.onResetEvent.observe { reset() }
        paging.onMutableStateChangedEvent.observe(ignoreCurrent = true) { state ->
            setMutableState(state)
        }
    }

    override fun onLoadCurrentPage() {
        pagination.loadCurrentPage()
    }

    override fun onLoadNextPage() {
        pagination.loadNextPage()
    }

    override fun onLoadPrevPage() {}

    override fun onPositionChanged(position: Int) {
        pagination.setCurrentPosition(position)
    }

    fun resetPagination() {
        pagination.resetPaging()
    }

    fun withPaging(block: Paging.() -> Unit) {
        block(pagination)
    }
}