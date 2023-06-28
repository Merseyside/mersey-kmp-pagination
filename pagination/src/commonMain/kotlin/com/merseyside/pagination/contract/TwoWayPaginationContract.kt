package com.merseyside.pagination.contract

import com.merseyside.merseyLib.kotlin.entity.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge

interface TwoWayPaginationContract<Data> : PaginationContract<Data> {

    val onPrevPageResultFlow: Flow<Result<Data>>

    /**
     * Merge next and prev page flows
     */
    fun getMergedFlows(): Flow<Result<Data>> {
        return merge(onPageResultFlow, onPrevPageResultFlow)
    }

    fun loadPrevPage(onComplete: () -> Unit = {}): Boolean
}