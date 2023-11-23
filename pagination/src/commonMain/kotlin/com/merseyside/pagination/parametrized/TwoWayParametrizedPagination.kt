package com.merseyside.pagination.parametrized

import com.merseyside.merseyLib.kotlin.entity.result.Result
import com.merseyside.merseyLib.utils.core.savedState.SavedState
import com.merseyside.pagination.TwoWayPagination
import com.merseyside.pagination.annotation.InternalPaginationApi
import com.merseyside.pagination.contract.PaginationContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(InternalPaginationApi::class)
abstract class TwoWayParametrizedPagination<Paging : TwoWayPagination<*, Data, *>, Data, Params : Any>(
    parentScope: CoroutineScope,
    savedState: SavedState,
) : OneWayParametrizedPagination<Paging, Data, Params>(
    parentScope,
    savedState
), PaginationContract<Data> {

    private var collectPrevJob: Job? = null

    private val mutPrevPageResultFlow = MutableSharedFlow<Result<Data>>()
    override val onPrevPageResultFlow: Flow<Result<Data>> = mutPrevPageResultFlow

    override fun loadPrevPage(onComplete: () -> Unit): Boolean {
        return currentPagination.loadPrevPage(onComplete)
    }

    override fun collectPagination(pagination: Paging) {
        super.collectPagination(pagination)

        collectPrevJob = pagination
            .onPrevPageResultFlow
            .onEach { mutPrevPageResultFlow.emit(it) }
            .launchIn(parentScope)
    }

    override fun cancelLoading() {
        super.cancelLoading()
        collectPrevJob?.cancel()
        collectPrevJob = null
    }
}