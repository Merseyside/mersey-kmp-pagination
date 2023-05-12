package com.merseyside.pagination.contract

import com.merseyside.merseyLib.kotlin.entity.result.Result
import kotlinx.coroutines.flow.Flow

interface PaginationContract<Data> : BasePaginationContract {

    fun loadNextPage(onComplete: () -> Unit = {}): Boolean

    val onPageResultFlow: Flow<Result<Data>>
}