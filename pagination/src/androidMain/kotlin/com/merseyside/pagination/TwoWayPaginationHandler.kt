package com.merseyside.pagination

import com.merseyside.pagination.contract.TwoWayPaginationContract

class TwoWayPaginationHandler<Data>(
    override val pagination: TwoWayPaginationContract<Data>,
    loadItemsCountDownOffset: Int = 5,
    loadItemsCountUpOffset: Int = loadItemsCountDownOffset,
): PaginationHandler<Data>(pagination, loadItemsCountDownOffset, loadItemsCountUpOffset) {

    override val onLoadPrevPage: () -> Unit = { pagination.loadPrevPage() }
}