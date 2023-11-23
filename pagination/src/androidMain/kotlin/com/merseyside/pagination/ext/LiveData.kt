package com.merseyside.pagination.ext

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.merseyside.merseyLib.kotlin.entity.result.Result
import com.merseyside.pagination.PaginationHandler
import com.merseyside.pagination.contract.OneWayPaginationContract
import com.merseyside.pagination.contract.PaginationContract
import com.merseyside.pagination.contract.TwoWayPaginationContract

fun <Paging : OneWayPaginationContract<Data>, Data>
        PaginationHandler<Paging, Data>.onPageLiveData(): LiveData<Result<Data>> {
    return pagination.onPageResultFlow.asLiveData()
}

