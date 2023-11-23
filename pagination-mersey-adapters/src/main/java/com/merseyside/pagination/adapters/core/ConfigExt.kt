package com.merseyside.pagination.adapters.core

import com.merseyside.adapters.core.feature.pagination.Config
import com.merseyside.adapters.core.model.VM
import com.merseyside.merseyLib.kotlin.entity.result.filterSuccessValues
import com.merseyside.pagination.contract.OneWayPaginationContract
import com.merseyside.pagination.contract.PaginationContract
import kotlinx.coroutines.flow.filterNotNull

fun <Data, Parent, Model : VM<Parent>> Config<Parent, Model>.setPagination(
    paginationContract: OneWayPaginationContract<List<Data>>
) {
    with(paginationContract) {
        onNextPageFlow = onPageResultFlow.filterSuccessValues().filterNotNull()
        resetObservableEvent = onResetEvent
    }
}

fun <Data, Parent, Model : VM<Parent>> Config<Parent, Model>.setPagination(
    twoPaginationContract: PaginationContract<List<Data>>
) {
    setPagination(paginationContract = twoPaginationContract)
    onPrevPageFlow = twoPaginationContract.onPrevPageResultFlow.filterSuccessValues().filterNotNull()
}