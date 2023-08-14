package com.merseyside.pagination.contract

import com.merseyside.merseyLib.kotlin.entity.result.Result
import com.merseyside.merseyLib.kotlin.observable.ObservableField
import com.merseyside.pagination.formatter.DataFormatter
import kotlinx.coroutines.flow.Flow

interface PaginationContract<Data> : BasePaginationContract<Data> {

    val onPageResultFlow: Flow<Result<Data>>

    fun loadNextPage(onComplete: () -> Unit = {}): Boolean

}