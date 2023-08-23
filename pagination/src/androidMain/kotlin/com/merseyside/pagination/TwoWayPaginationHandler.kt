package com.merseyside.pagination

import com.merseyside.pagination.contract.TwoWayPaginationContract

class TwoWayPaginationHandler<Paging : TwoWayPaginationContract<Data>, Data>(
    override val pagination: Paging,
    saveScrollPosition: Boolean = false,
    loadItemsCountDownOffset: Int = 5,
    loadItemsCountUpOffset: Int = loadItemsCountDownOffset,
) : PaginationHandler<Paging, Data>(
    pagination, saveScrollPosition, loadItemsCountDownOffset, loadItemsCountUpOffset
) {

    override fun onLoadPrevPage() {
        pagination.loadPrevPage()
    }
}