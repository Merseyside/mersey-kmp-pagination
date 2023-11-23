package com.merseyside.pagination.contract

import com.merseyside.merseyLib.kotlin.entity.result.Result
import com.merseyside.pagination.CompleteAction
import com.merseyside.pagination.annotation.InternalPaginationApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge

interface TwoWayPaginationContract<Data> : OneWayPaginationContract<Data> {

    val onPrevPageResultFlow: Flow<Result<Data>>

    /**
     * Merge next and prev page flows
     */
    fun getMergedFlows(): Flow<Result<Data>> {
        return merge(onPageResultFlow, onPrevPageResultFlow)
    }

    @InternalPaginationApi
    fun loadPrevPage(onComplete: CompleteAction = {}): Boolean
}