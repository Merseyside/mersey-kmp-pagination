package com.merseyside.pagination.parametrized

import com.merseyside.merseyLib.kotlin.entity.result.Result
import com.merseyside.pagination.TwoWayPaginationData
import com.merseyside.pagination.contract.TwoWayPaginationContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

abstract class TwoWayParametrizedPagination<Paging : TwoWayPaginationData<Data>, Data, Params : Any>(
    parentScope: CoroutineScope,
    keepInstances: Boolean = false
) : ParametrizedPagination<Paging, Data, Params>(parentScope, keepInstances), TwoWayPaginationContract<Data> {

    private var collectPrevJob: Job? = null

    private val mutPrevPageResultFlow = MutableSharedFlow<Result<Data>>()
    override val onPrevPageResultFlow: Flow<Result<Data>> = mutPrevPageResultFlow

    override fun loadPrevPage(onComplete: () -> Unit): Boolean {
        return currentPagination.loadPrevPage(onComplete)
    }

    override fun collectPagination(pagination: Paging) {
        super.collectPagination(pagination)

        cancelPrevJob()
        collectPrevJob = pagination
            .onPrevPageResultFlow
            .onEach { mutPrevPageResultFlow.emit(it) }
            .launchIn(parentScope)
    }

    private fun cancelPrevJob() {
        collectPrevJob?.cancel()
        collectPrevJob = null
    }
}