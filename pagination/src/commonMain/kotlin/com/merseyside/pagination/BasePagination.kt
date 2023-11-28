package com.merseyside.pagination

import com.merseyside.merseyLib.kotlin.entity.result.Result
import com.merseyside.merseyLib.kotlin.logger.ILogger
import com.merseyside.merseyLib.kotlin.logger.Logger
import com.merseyside.merseyLib.kotlin.observable.*
import com.merseyside.merseyLib.utils.core.savedState.SavedState
import com.merseyside.merseyLib.utils.core.savedState.delegate.saveable
import com.merseyside.pagination.annotation.InternalPaginationApi
import com.merseyside.pagination.contract.BasePaginationContract
import com.merseyside.pagination.data.PagerData
import com.merseyside.pagination.formatter.DataFormatter
import com.merseyside.pagination.pagesManager.PaginationPagesManager
import com.merseyside.pagination.state.PagingState
import kotlinx.coroutines.*

@OptIn(InternalPaginationApi::class)
abstract class BasePagination<PD, Data, Page>(
    private val parentScope: CoroutineScope,
    initPage: Page,
    pageSize: Int,
    protected val savedState: SavedState
) : BasePaginationContract<Data>, ILogger where PD : PagerData<Data, Page> {

    @Suppress("CanBePrimaryConstructorProperty")
    final override val pageSize: Int = pageSize

    protected val pagesManager: PaginationPagesManager<Page> by savedState.saveable { savedState ->
        PaginationPagesManager(initPage, pageSize, savedState)
    }

    private val onPagingResetObservableEvent = SingleObservableEvent()
    final override val onResetEvent: EventObservableField = onPagingResetObservableEvent

    private val mutOnStateChangedEvent by lazy {
        val initialValue = if (pagesManager.isInitialPageLoaded) Result.Success<Data>()
        else Result.NotInitialized()
        SingleObservableField(initialValue)
    }
    final override val onStateChangedEvent: ObservableField<Result<Data>> = mutOnStateChangedEvent
    private val dataFormatters: MutableList<DataFormatter<Data>> = mutableListOf()

    private var currentPageLoadingJob: Job? = null
    abstract val onPageResult: Emitter<Data>

    override val isInitialPageLoaded: Boolean
        get() = pagesManager.isInitialPageLoaded


    private var onReady: (startingPosition: Int) -> Unit = {}

    init {
        onStateChangedEvent.observe { state ->
            when (state) {
                is Result.NotInitialized -> savedState.setOnPreSaveStateCallback(null)
                else -> savedState.setOnPreSaveStateCallback { onPreSaveState(pagesManager) }
            }
        }
    }

    override fun notifyWhenReady(onReady: (startingPosition: Int) -> Unit) {
        this.onReady = onReady
        onReady(getSavedPosition())
    }

    override fun removeNotifyWhenReady() {
        onReady = {}
    }

    fun getSavedPosition(): Int {
        return pagesManager.savedStartingPosition
    }

    abstract suspend fun loadPage(page: Page?, pageSize: Int): PD

    abstract fun isDataEmpty(data: Data): Boolean

    /**
     * Loads starting page
     */
    override fun loadCurrentPage(onComplete: CompleteAction): Boolean {
        Logger.log("Trying to load current page")
        check(!pagesManager.isInitialPageLoaded) {
            "Starting page has been already loaded!"
        }

        isLoading {
            throw IllegalStateException(
                "Asked for loading current page" +
                        " but has been already loading!"
            )
        }

        val job = loadPage(
            ::loadPage,
            pagesManager.startingPage,
            canPageBeNull = true,
            emitter = onPageResult,
            onComplete
        ) { "No current page!" }

        return if (job != null) {
            currentPageLoadingJob = job
            true
        } else false
    }

    internal inline fun loadPage(
        crossinline loadPage: suspend (page: Page?, pageSize: Int) -> PD,
        page: Page?,
        canPageBeNull: Boolean = false,
        crossinline emitter: Emitter<Data>,
        crossinline onComplete: CompleteAction,
        onNoPage: () -> String
    ): Job? {
        return if (canPageBeNull || page != null) {
            loadPageInternal(page, emitter, onComplete, loadPage)
        } else {
            Logger.logInfo(tag, onNoPage())
            null
        }
    }

    private inline fun loadPageInternal(
        page: Page?,
        crossinline emitResult: Emitter<Data>,
        crossinline onComplete: CompleteAction = {},
        crossinline dataProvider: suspend (page: Page?, pageSize: Int) -> PD
    ): Job {
        check(parentScope.coroutineContext.isActive) {
            "Cancelled coroutine context!"
        }

        return parentScope.launch {
            mutateState(emitResult) { emit ->
                try {
                    emit(Result.Loading())
                    val newData = dataProvider(page, pageSize)
                    onDataLoaded(page, newData)
                    emit(Result.Success(formatData(newData.data)))
                } catch (e: Exception) {
                    Logger.logErr(e)
                    emit(Result.Error(e))
                }
            }
        }.also { it.invokeOnCompletion { onComplete() } }
    }

    override fun reset() {
        pagesManager.reset()
        onReset()
    }

    override fun softReset() {
        pagesManager.softReset()
        onReset()
    }

    protected open fun onReset() {
        mutOnStateChangedEvent.value = Result.NotInitialized()
        notifyPagingReset()
        onReady(getSavedPosition())
    }

    override fun setOnSavePagingPositionCallback(callback: PaginationPagesManager.OnSavePagingPositionCallback?) {
        pagesManager.setOnSavePagingPositionCallback(callback)
    }

    fun isLoadedData(): Boolean {
        return pagesManager.isInitialPageLoaded
    }

    private fun onDataLoaded(loadedPage: Page?, pagerData: PD) {
        pagesManager.onPageLoaded(
            loadedPage,
            isDataEmpty(pagerData.data),
            pagerData.nextPage,
            pagerData.prevPage
        )
    }

    private fun notifyPagingReset() {
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

    override fun saveState() {
        savedState.preSave()
        savedState.log("saved state =")
    }

    open fun onPreSaveState(pagesManager: PaginationPagesManager<Page>) {}

    private suspend inline fun mutateState(
        crossinline emitter: Emitter<Data>,
        mutate: (Emitter<Data>) -> Unit
    ) {
        mutate { result ->
            emitter(result)
            mutOnStateChangedEvent.value = result
        }
    }

    private inline fun isLoading(ifLoading: () -> Boolean): Boolean {
        return if (currentPageLoadingJob?.isActive == true) {
            Logger.logInfo(tag, "Starting page has been loading. Skipped.")
            ifLoading()
        } else false
    }

    override val tag: String
        get() = this::class.simpleName ?: "UnknownPagination"
}

typealias CompleteAction = () -> Unit
typealias Emitter<Data> = suspend (Result<Data>) -> Unit