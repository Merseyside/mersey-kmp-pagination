package com.merseyside.pagination.contract

import com.merseyside.merseyLib.kotlin.entity.result.Result
import kotlinx.coroutines.flow.Flow

interface TwoWayPaginationContract<Data> : PaginationContract<Data> {

    val onPrevPageResultFlow: Flow<Result<Data>>

    fun loadPrevPage(onComplete: () -> Unit = {}): Boolean
}