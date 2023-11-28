package com.merseyside.pagination.state

import com.merseyside.merseyLib.kotlin.entity.result.Result
import com.merseyside.merseyLib.kotlin.entity.result.isInitialized
import com.merseyside.merseyLib.kotlin.entity.result.isLoading
import com.merseyside.merseyLib.kotlin.observable.ObservableField
import com.merseyside.merseyLib.kotlin.observable.ext.mapNotNull
import com.merseyside.pagination.contract.BasePaginationContract

class PagingState(private val paginationContract: BasePaginationContract<*>) {

    val stateObservable: ObservableField<out Result<*>> =
        paginationContract.onStateChangedEvent

    val isLoadingObservable: ObservableField<Boolean> =
        stateObservable.mapNotNull { result -> result.isLoading() }

    val isDataLoadedObservable: ObservableField<Boolean> =
        stateObservable.mapNotNull { result -> result.isInitialized() }

}