package com.merseyside.pagination

import com.merseyside.merseyLib.kotlin.entity.result.Result
import com.merseyside.merseyLib.kotlin.logger.Logger
import com.merseyside.pagination.contract.TwoWayPaginationContract
import com.merseyside.merseyLib.utils.core.savedState.SavedState
import com.merseyside.pagination.annotation.InternalPaginationApi
import com.merseyside.pagination.data.PagerData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

@OptIn(InternalPaginationApi::class)
abstract class TwoWayPagination<PD, Data, Page>(
    parentScope: CoroutineScope,
    initPage: Page,
    pageSize: Int,
    savedState: SavedState = SavedState()
) : OneWayPagination<PD, Data, Page>(parentScope, initPage, pageSize, savedState = savedState),
    TwoWayPaginationContract<Data> where PD : PagerData<Data, Page> {

    private val mutPrevPageResultFlow = MutableSharedFlow<Result<Data>>()
    override val onPrevPageResultFlow: Flow<Result<Data>> = mutPrevPageResultFlow

    private var prevLoadingJob: Job? = null

    private var onPrevPageResult: Emitter<Data> = { result ->
        mutPrevPageResultFlow.emit(result)
    }

    abstract suspend fun loadPrevPage(page: Page, pageSize: Int): PD

    override fun loadPrevPage(onComplete: CompleteAction): Boolean {
        isLoading {
            return true
        }

        val job = loadPage(
            { page, pageSize -> loadPrevPage(requireNotNull(page), pageSize) },
            pagesManager.getPrevPage(),
            emitter = onPrevPageResult,
            onComplete = onComplete
        ) { "No previous page!" }

        return if (job != null) {
            prevLoadingJob = job
            true
        } else false
    }

    private inline fun isLoading(ifLoading: () -> Boolean): Boolean {
        return if (prevLoadingJob?.isActive == true) {
            Logger.logInfo(tag, "Prev page has been loading. Skipped.")
            ifLoading()
        } else false
    }

    override fun cancelLoading() {
        super.cancelLoading()
        prevLoadingJob?.cancel()
    }
}