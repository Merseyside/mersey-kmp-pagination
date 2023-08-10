package com.merseyside.pagination

import com.merseyside.pagination.contract.PaginationContract
import com.merseyside.utils.pagination.PaginationScrollHandler

open class PaginationHandler<Paging : PaginationContract<Data>, Data>(
    paging: Paging,
    loadItemsCountDownOffset: Int = 5,
    loadItemsCountUpOffset: Int = loadItemsCountDownOffset,
) : PaginationScrollHandler(loadItemsCountDownOffset, loadItemsCountUpOffset) {

    open val pagination: Paging = paging

    init {
        paging.onResetEvent.observe { reset() }
    }

    override val onLoadFirstPage: (onComplete: () -> Unit) -> Unit = {
        pagination.loadInitialPage(it)
    }

    override val onLoadNextPage: (onComplete: () -> Unit) -> Unit = {
        pagination.loadNextPage(it)
    }

    override val onLoadPrevPage: (onComplete: () -> Unit) -> Unit = { onComplete ->
        onComplete()
    }

    fun withPaging(block: Paging.() -> Unit) {
        block(pagination)
    }
}