package com.merseyside.pagination.ext

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.merseyside.merseyLib.kotlin.entity.result.Result
import com.merseyside.pagination.PaginationHandler
import com.merseyside.pagination.TwoWayPaginationHandler
import com.merseyside.pagination.contract.PaginationContract
import com.merseyside.pagination.contract.TwoWayPaginationContract

fun <Paging : PaginationContract<Data>, Data>
        PaginationHandler<Paging, Data>.onPageLiveData(): LiveData<Result<Data>> {
    return pagination.onPageResultFlow.asLiveData()
}

fun <Paging : TwoWayPaginationContract<Data>, Data>
        TwoWayPaginationHandler<Paging, Data>.onPrevPageLiveData(): LiveData<Result<Data>> {
    return pagination.onPrevPageResultFlow.asLiveData()
}

