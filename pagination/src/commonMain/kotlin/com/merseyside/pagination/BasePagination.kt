package com.merseyside.pagination

import com.merseyside.merseyLib.kotlin.entity.result.Result
import com.merseyside.merseyLib.kotlin.logger.ILogger
import com.merseyside.merseyLib.kotlin.logger.Logger
import com.merseyside.merseyLib.kotlin.observable.EventObservableField
import com.merseyside.merseyLib.kotlin.observable.SingleObservableEvent
import com.merseyside.merseyLib.utils.core.savedState.SavedState
import com.merseyside.merseyLib.utils.core.savedState.delegate.saveable
import com.merseyside.pagination.contract.BasePaginationContract
import com.merseyside.pagination.pagesManager.PaginationPagesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class BasePagination<PD, Data, Page>(
    parentScope: CoroutineScope,
    initPage: Page,
    private val savedState: SavedState = SavedState()
): BasePaginationContract, CoroutineScope, ILogger where PD : PagerData<Data, Page> {

    protected val pagesManager: PaginationPagesManager<Page> by savedState.saveable { savedState ->
        PaginationPagesManager(initPage, savedState)
    }

    override val coroutineContext: CoroutineContext = parentScope.coroutineContext
    private var loadingJob: Job? = null

    private val onPagingResetObservableEvent = SingleObservableEvent()
    override val onResetEvent: EventObservableField = onPagingResetObservableEvent

    /**
     * Callback for current, initial and next pages.
     */
    protected abstract val onPageResult: suspend (Result<Data>) -> Unit

    override val isFirstPageLoaded: Boolean
        get() = pagesManager.isFirstPageLoaded

    abstract suspend fun loadPage(page: Page?): PD

    override fun loadInitialPage(onComplete: CompleteAction): Boolean {
        if (isLoading()) {
            onComplete()
            return false
        }

        pagesManager.reset()

        return loadCurrentPage(onComplete)
    }

    override fun loadCurrentPage(onComplete: CompleteAction): Boolean {
        if (isLoading()) {
            onComplete()
            Logger.logInfo(tag, "Loading. Skipped.")
            return false
        }
        val page = pagesManager.initPage

        loadPageInternal(page, onPageResult, onComplete, ::loadPage)

        return true
    }

    override fun resetPaging() {
        pagesManager.reset()
        notifyPagingReset()
    }

    fun isLoadedData(): Boolean {
        return pagesManager.isFirstPageLoaded
    }

    fun isLoading(): Boolean {
        return loadingJob?.isActive == true
    }

    protected fun loadPageInternal(
        page: Page?,
        emitResult: suspend (Result<Data>) -> Unit,
        onComplete: CompleteAction = {},
        dataProvider: suspend (Page?) -> PD
    ) {
        loadingJob = launch {
            try {
                emitResult(Result.Loading())
                val newData = dataProvider(page)
                onDataLoaded(page, newData)
                emitResult(Result.Success(newData.data))
            } catch (e: Exception) {
                e.printStackTrace()
                emitResult(Result.Error(e))
            }

        }.also { it.invokeOnCompletion { onComplete() } }
    }

    protected fun onDataLoaded(loadedPage: Page?, pagerData: PD) {
        pagesManager.onPageLoaded(loadedPage, pagerData.nextPage.log("next page"), pagerData.prevPage)
    }

    private fun notifyPagingReset() {
        onPagingResetObservableEvent.call()
    }
}

typealias CompleteAction = () -> Unit