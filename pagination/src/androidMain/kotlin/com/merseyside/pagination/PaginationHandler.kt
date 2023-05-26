package com.merseyside.pagination

import com.merseyside.pagination.contract.PaginationContract
import com.merseyside.utils.pagination.PaginationScrollHandler
open class PaginationHandler<Data>(
    p: PaginationContract<Data>,
    loadItemsCountDownOffset: Int = 5,
    loadItemsCountUpOffset: Int = loadItemsCountDownOffset,
) : PaginationScrollHandler(loadItemsCountDownOffset, loadItemsCountUpOffset) {

    open val pagination: PaginationContract<Data> = p

    init {
        p.addOnPagingResetCallback {
            reset()
        }
    }

    override val onLoadFirstPage: (onComplete: () -> Unit) -> Unit = {
        pagination.loadCurrentPage(it)
    }

    override val onLoadNextPage: () -> Unit = {
        pagination.loadNextPage()
    }

    override val onLoadPrevPage: () -> Unit = {}
}