package com.merseyside.pagination.contract

import com.merseyside.merseyLib.kotlin.entity.result.Result
import com.merseyside.pagination.CompleteAction
import kotlinx.coroutines.flow.Flow

interface OneWayPaginationContract<Data> : BasePaginationContract<Data> {

    val onPageResultFlow: Flow<Result<Data>>

    fun loadNextPage(onComplete: CompleteAction = {}): Boolean

}