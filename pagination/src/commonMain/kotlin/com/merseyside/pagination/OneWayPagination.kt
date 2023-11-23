package com.merseyside.pagination

import com.merseyside.merseyLib.kotlin.entity.result.Result
import com.merseyside.merseyLib.kotlin.logger.Logger
import com.merseyside.pagination.contract.OneWayPaginationContract
import com.merseyside.merseyLib.utils.core.savedState.SavedState
import com.merseyside.pagination.annotation.InternalPaginationApi
import com.merseyside.pagination.data.PagerData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

@OptIn(InternalPaginationApi::class)
abstract class OneWayPagination<PD, Data, Page>(
    parentScope: CoroutineScope,
    initPage: Page,
    pageSize: Int,
    savedState: SavedState = SavedState()
) : BasePagination<PD, Data, Page>(parentScope, initPage, pageSize, savedState),
    OneWayPaginationContract<Data> where PD : PagerData<Data, Page> {

    private val mutPageResultFlow = MutableSharedFlow<Result<Data>>()
    override val onPageResultFlow: Flow<Result<Data>> = mutPageResultFlow

    /**
     * Callback for current and next pages.
     */
    final override val onPageResult: Emitter<Data> = { result ->
        mutPageResultFlow.emit(result)
    }

    private var nextLoadingJob: Job? = null

    abstract suspend fun loadNextPage(page: Page, pageSize: Int): PD

    /**
     * @return true if loading or has next page
     */
    override fun loadNextPage(onComplete: CompleteAction): Boolean {
        isLoading {
            return true
        }

        val job = loadPage(
            { page, pageSize -> loadNextPage(requireNotNull(page), pageSize) },
            pagesManager.getNextPage(),
            emitter = onPageResult,
            onComplete = onComplete
        ) {
            "No next page!"
        }

        return if (job != null) {
            nextLoadingJob = job
            true
        } else false
    }

    private inline fun isLoading(ifLoading: () -> Boolean): Boolean {
        return if (nextLoadingJob?.isActive == true) {
            Logger.logInfo(tag, "Next page has been loading. Skipped.")
            ifLoading()
        } else false
    }

    override fun cancelLoading() {
        nextLoadingJob?.cancel()
    }
}