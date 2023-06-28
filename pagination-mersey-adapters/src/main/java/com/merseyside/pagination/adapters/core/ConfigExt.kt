package com.merseyside.pagination.adapters.core

import com.merseyside.adapters.core.feature.pagination.Config
import com.merseyside.adapters.core.model.VM
import com.merseyside.merseyLib.kotlin.entity.result.filterSuccessValues
import com.merseyside.pagination.contract.PaginationContract
import com.merseyside.pagination.contract.TwoWayPaginationContract

fun <Data, Parent, Model : VM<Parent>> Config<Parent, Model>.setPagination(
    paginationContract: PaginationContract<List<Data>>
) {
    onNextPageFlow = paginationContract.onPageResultFlow.filterSuccessValues()
}

fun <Data, Parent, Model : VM<Parent>> Config<Parent, Model>.setPagination(
    paginationContract: TwoWayPaginationContract<List<Data>>
) {
    onNextPageFlow = paginationContract.onPageResultFlow.filterSuccessValues()
    onPrevPageFlow = paginationContract.onPrevPageResultFlow.filterSuccessValues()
}