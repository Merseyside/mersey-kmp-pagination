package com.merseyside.pagination.contract

import com.merseyside.merseyLib.kotlin.entity.result.Result
import com.merseyside.pagination.formatter.DataFormatter
import kotlinx.coroutines.flow.Flow

interface PaginationContract<Data> : BasePaginationContract<Data> {

    fun loadNextPage(onComplete: () -> Unit = {}): Boolean

    val onPageResultFlow: Flow<Result<Data>>

}