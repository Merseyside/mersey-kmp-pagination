package com.merseyside.pagination

import com.merseyside.pagination.contract.TwoWayPaginationContract

class TwoWayPaginationHandler<Paging : TwoWayPaginationContract<Data>, Data>(
    override val pagination: Paging,
    loadItemsCountDownOffset: Int = 5,
    loadItemsCountUpOffset: Int = loadItemsCountDownOffset,
): PaginationHandler<Paging, Data>(pagination, loadItemsCountDownOffset, loadItemsCountUpOffset) {

    override fun onLoadPrevPage() { pagination.loadPrevPage() }
}