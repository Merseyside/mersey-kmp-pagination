package com.merseyside.pagination

import com.merseyside.merseyLib.kotlin.entity.result.Result
import com.merseyside.merseyLib.kotlin.logger.ILogger
import com.merseyside.merseyLib.kotlin.logger.Logger
import com.merseyside.merseyLib.kotlin.observable.EventObservableField
import com.merseyside.merseyLib.kotlin.observable.MutableObservableField
import com.merseyside.merseyLib.kotlin.observable.ObservableField
import com.merseyside.merseyLib.kotlin.observable.SingleObservableEvent
import com.merseyside.merseyLib.utils.core.savedState.SavedState
import com.merseyside.merseyLib.utils.core.savedState.delegate.saveable
import com.merseyside.pagination.contract.BasePaginationContract
import com.merseyside.pagination.data.PagerData
import com.merseyside.pagination.formatter.DataFormatter
import com.merseyside.pagination.pagesManager.PaginationPagesManager
import com.merseyside.pagination.state.PagingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class BasePagination<PD, Data, Page>(
    parentScope: CoroutineScope,
    initPage: Page,
    override val pageSize: Int,
    private val savedState: SavedState = SavedState()
) : BasePaginationContract<Data>, CoroutineScope, ILogger where PD : PagerData<Data, Page> {

    protected val pagesManager: PaginationPagesManager<Page> by savedState.saveable { savedState ->
        PaginationPagesManager(initPage, pageSize, savedState)
    }

    override val coroutineContext: CoroutineContext = parentScope.coroutineContext
    private var loadingJob: Job? = null

    private val onPagingResetObservableEvent = SingleObservableEvent()
    override val onResetEvent: EventObservableField = onPagingResetObservableEvent

    private val mutOnStateChangedEvent by lazy {
        val initialValue = if (pagesManager.isFirstPageLoaded) Result.Success<Data>()
        else Result.NotInitialized()
        MutableObservableField(initialValue)
    }
    override val onStateChangedEvent: ObservableField<Result<Data>> = mutOnStateChangedEvent

    internal val dataFormatters: MutableList<DataFormatter<Data>> = mutableListOf()

    /**
     * Callback for current and next pages.
     */
    protected abstract val onPageResult: suspend (Result<Data>) -> Unit

    override val isFirstPageLoaded: Boolean
        get() = pagesManager.isFirstPageLoaded

    abstract suspend fun loadPage(page: Page?, pageSize: Int): PD

    override fun loadCurrentPage(onComplete: CompleteAction): Boolean {
        if (isLoading()) {
            onComplete()
            Logger.logInfo(tag, "Loading. Skipped.")
            return false
        }
        val page = pagesManager.currentPage

        loadPageInternal(page, onPageResult, onComplete, ::loadPage)

        return true
    }

    override fun resetPaging() {
        pagesManager.reset()
        notifyPagingReset()
    }

    override fun setCurrentPosition(position: Int) {
        pagesManager.setCurrentPosition(position)
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
        dataProvider: suspend (page: Page?, pageSize: Int) -> PD
    ) {
        loadingJob = launch {
            mutateState(emitResult) { emit ->
                try {
                    emit(Result.Loading())
                    val newData = dataProvider(page, pageSize)
                    onDataLoaded(page, newData)
                    emit(Result.Success(formatData(newData.data)))
                } catch (e: Exception) {
                    e.printStackTrace()
                    emit(Result.Error(e))
                }
            }
        }.also { it.invokeOnCompletion { onComplete() } }
    }

    protected fun onDataLoaded(loadedPage: Page?, pagerData: PD) {
        pagesManager.onPageLoaded(loadedPage, pagerData.nextPage, pagerData.prevPage)
    }

    private fun notifyPagingReset() {
        mutOnStateChangedEvent.value = Result.NotInitialized()
        onPagingResetObservableEvent.call()
    }

    fun addDataFormatter(dataFormatter: DataFormatter<Data>): DataFormatter<Data> {
        dataFormatters.add(dataFormatter)
        return dataFormatter
    }

    fun removeFormatter(dataFormatter: DataFormatter<Data>) {
        dataFormatters.remove(dataFormatter)
    }

    override fun getPagingState(): PagingState {
        return PagingState(this)
    }

    private fun formatData(data: Data): Data {
        var formattedData = data
        dataFormatters.forEach { formatter -> formattedData = formatter.format(formattedData) }
        return formattedData
    }

    private suspend fun mutateState(
        resultObserver: suspend (Result<Data>) -> Unit = {},
        mutate: suspend (Emitter<Data>) -> Unit
    ) {
        mutate { result ->
            resultObserver(result)
            mutOnStateChangedEvent.value = result
        }
    }
}

typealias CompleteAction = () -> Unit
typealias Emitter<Data> = suspend (Result<Data>) -> Unit