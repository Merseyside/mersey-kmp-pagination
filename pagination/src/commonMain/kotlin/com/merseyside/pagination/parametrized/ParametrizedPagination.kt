package com.merseyside.pagination.parametrized

import com.merseyside.merseyLib.kotlin.entity.result.Result
import com.merseyside.merseyLib.kotlin.logger.Logger
import com.merseyside.merseyLib.kotlin.observable.*
import com.merseyside.merseyLib.utils.core.savedState.SavedState
import com.merseyside.pagination.CompleteAction
import com.merseyside.pagination.P
import com.merseyside.pagination.contract.PaginationContract
import com.merseyside.pagination.state.PagingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

abstract class ParametrizedPagination<Paging : P<Data>, Data, Params : Any>(
    protected val parentScope: CoroutineScope,
    private var defaultParams: Params? = null,
    private val savedState: SavedState = SavedState(),
    private var keepInstances: Boolean = true
) : PaginationContract<Data> {

    override val pageSize: Int
        get() = currentPagination.pageSize

    private var collectJob: Job? = null
    private val paginationMap: MutableMap<Params, Paging> = mutableMapOf()

    private val onPagingResetObservableEvent = SingleObservableEvent()
    override val onResetEvent: EventObservableField = onPagingResetObservableEvent

    private val onClearedObservableEvent = SingleObservableEvent()
    val onClearedEvent: EventObservableField = onClearedObservableEvent

    private val onPagingChangedMutableEvent = SingleObservableField<Params>()
    val onPagingChangedEvent: ObservableField<Params> = onPagingChangedMutableEvent

    private var _currentParams: Params? = null
    val currentParams: Params
        get() = _currentParams ?: throw NullPointerException("Params not set!")

    lateinit var currentPagination: Paging

    private val mutPageResultFlow = MutableSharedFlow<Result<Data>>()
    override val onPageResultFlow: Flow<Result<Data>> = mutPageResultFlow

    private val mutOnStateChangedEvent = MutableObservableField<Result<Data>>(Result.NotInitialized())
    override val onStateChangedEvent: ObservableField<Result<Data>> = mutOnStateChangedEvent
    private var mutableStateDisposable: Disposable<Result<Data>>? = null

    override val isFirstPageLoaded: Boolean
        get() = currentPagination.isFirstPageLoaded

    abstract fun createPagination(
        parentScope: CoroutineScope,
        params: Params
    ): Paging

    fun isInitialized(): Boolean {
        return _currentParams != null
    }

    fun getPaginationOrNull(params: Params): Paging? {
        return paginationMap[params]
    }

    fun setParams(params: Params): Boolean {
        if (isInitialized() && currentParams == params) {
            Logger.logInfo(this, "Trying to set the same params.")
            return false
        } else {
            cancelJob()
        }

        _currentParams = params
        currentPagination = getPagination(params)

        observeMutableState(currentPagination)
        collectPagination(currentPagination)
        onPaginationChanged(params)
        return true
    }

    fun setDefaultParams(params: Params) {
        defaultParams = params
    }

    /**
     * Return true if new provided params not equals to current params
     */
    @OptIn(ExperimentalContracts::class)
    fun updateAndSetParams(update: (Params) -> Params): Boolean {
        val params = if (isInitialized()) currentParams
        else defaultParams

        return if (requireParamsNotNull(params)) {
            val newParams = update(params)
            setParams(newParams)
        } else false
    }

    override fun loadCurrentPage(onComplete: CompleteAction): Boolean = setPaginationIfNeed {
        currentPagination.loadCurrentPage(onComplete)
    }

    override fun loadNextPage(onComplete: CompleteAction): Boolean {
        return currentPagination.loadNextPage(onComplete)
    }

    open fun collectPagination(pagination: Paging) {
        cancelJob()
        collectJob = pagination
            .onPageResultFlow
            .onEach { mutPageResultFlow.emit(it) }
            .launchIn(parentScope)
    }

    open fun observeMutableState(pagination: Paging) {
        mutableStateDisposable?.dispose()
        mutableStateDisposable =
            pagination.onStateChangedEvent.observe(ignoreCurrent = true) { state ->
                mutOnStateChangedEvent.value = state
            }
    }

    override fun resetPaging() {
        cancelJob()
        currentPagination.resetPaging()
        notifyPagingReset()
    }

    override fun setCurrentPosition(position: Int) {
        currentPagination.setCurrentPosition(position)
    }

    fun clear() {
        _currentParams = null
        onPagingChangedMutableEvent.value = null
        resetPaging()

        paginationMap.clear()
        onClearedObservableEvent.call()
    }

    /**
     * Make parametrized pagination store(or not) and reuse pagination instances or
     * create a new one every time params were changed.
     *
     * If @param keepInstances is false, then all previously stored instances will be cleared.
     */
    fun setKeepInstances(keepInstances: Boolean) {
        if (this.keepInstances != keepInstances) {
            if (!keepInstances) {
                clear()
            }

            this.keepInstances = keepInstances
        }
    }

    private fun getPagination(params: Params): Paging {
        return if (keepInstances) {
            getPaginationOrNull(params) ?: createPagination(parentScope, params).also { p ->
                paginationMap[params] = p
            }
        } else {
            createPagination(parentScope, params)
        }
    }

    /**
     * @return true if default params not null
     */
    private fun setDefaultParamsIfNotNull(): Boolean {
        defaultParams?.let { params ->
            setParams(params)
        }

        return defaultParams != null
    }

    protected open fun cancelJob() {
        collectJob?.cancel()
        collectJob = null
    }

    private fun notifyPagingReset() {
        onPagingResetObservableEvent.call()
    }

    private fun onPaginationChanged(params: Params) {
        onPagingChangedMutableEvent.value = params
    }

    private fun <R> setPaginationIfNeed(pagingJob: () -> R): R {
        if (!isInitialized()) {
            if (!setDefaultParamsIfNotNull()) throw NullPointerException(
                "Params not set and default params is also null!"
            )
        }

        return pagingJob()
    }

    override fun getPagingState(): PagingState {
        return PagingState(this)
    }

    @ExperimentalContracts
    private fun requireParamsNotNull(params: Params?): Boolean {
        contract {
            returns(true) implies (params != null)
        }

        return if (params != null) true
        else throw NullPointerException("Params not set and default params is also null!")
    }
}