package com.merseyside.pagination.parametrized

import com.merseyside.merseyLib.kotlin.entity.result.Result
import com.merseyside.merseyLib.utils.core.savedState.SavedState
import com.merseyside.pagination.CompleteAction
import com.merseyside.pagination.OneWayPagination
import com.merseyside.pagination.annotation.InternalPaginationApi
import com.merseyside.pagination.contract.OneWayPaginationContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(InternalPaginationApi::class)
abstract class OneWayParametrizedPagination<Paging : OneWayPagination<*, Data, *>, Data, Params : Any>(
    parentScope: CoroutineScope,
    savedState: SavedState = SavedState(),
) : BaseParametrizedPagination<Paging, Data, Params>(parentScope, savedState),
    OneWayPaginationContract<Data> {

    private val mutPageResultFlow = MutableSharedFlow<Result<Data>>()
    override val onPageResultFlow: Flow<Result<Data>> = mutPageResultFlow

    private var collectJob: Job? = null

    override fun collectPagination(pagination: Paging) {
        collectJob = pagination
            .onPageResultFlow
            .onEach { mutPageResultFlow.emit(it) }
            .launchIn(parentScope)
    }

    override fun cancelLoading() {
        collectJob?.cancel()
        collectJob = null
    }

    override fun loadNextPage(onComplete: CompleteAction): Boolean {
        return currentPagination.loadNextPage(onComplete)
    }
}