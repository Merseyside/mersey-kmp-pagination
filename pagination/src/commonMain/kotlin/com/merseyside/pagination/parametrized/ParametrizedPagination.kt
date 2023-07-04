package com.merseyside.pagination.parametrized

import com.merseyside.merseyLib.kotlin.entity.result.Result
import com.merseyside.merseyLib.kotlin.logger.Logger
import com.merseyside.merseyLib.kotlin.observable.EventObservableField
import com.merseyside.merseyLib.kotlin.observable.ObservableField
import com.merseyside.merseyLib.kotlin.observable.SingleObservableEvent
import com.merseyside.merseyLib.kotlin.observable.SingleObservableField
import com.merseyside.pagination.CompleteAction
import com.merseyside.pagination.P
import com.merseyside.pagination.contract.PaginationContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

abstract class ParametrizedPagination<Paging : P<Data>, Data, Params : Any>(
    protected val parentScope: CoroutineScope,
    private var defaultParams: Params? = null,
    var keepInstances: Boolean = false
) : PaginationContract<Data> {

    private var collectJob: Job? = null
    private val paginationMap: MutableMap<Params, Paging> = mutableMapOf()

    private val onPagingResetObservableEvent = SingleObservableEvent()
    override val onResetEvent: EventObservableField = onPagingResetObservableEvent

    private val onPagingChangedSingleEvent = SingleObservableField<Params>()
    val onPagingChangedEvent: ObservableField<Params> = onPagingChangedSingleEvent

    lateinit var currentParams: Params
        private set

    lateinit var currentPagination: Paging

    private val mutPageResultFlow = MutableSharedFlow<Result<Data>>()
    override val onPageResultFlow: Flow<Result<Data>> = mutPageResultFlow

    override val isFirstPageLoaded: Boolean
        get() = currentPagination.isFirstPageLoaded

    abstract fun createPagination(parentScope: CoroutineScope, params: Params): Paging

    fun isInitialized(): Boolean {
        return this::currentParams.isInitialized
    }

    fun getPaginationOrNull(params: Params): Paging? {
        return paginationMap[params]
    }

    fun setParams(params: Params): Boolean {
        if (isInitialized() && currentParams == params) {
            Logger.logInfo(this, "Trying to set the same params.")
            return false
        }

        if (isInitialized()) resetPaging()
        currentParams = params

        currentPagination = getPagination(params)
        collectPagination(currentPagination)
        onPaginationChanged(params)
        return true
    }

    fun setDefaultParams(params: Params) {
        setParams(params)
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

    /**
     * Return true if new provided params not equals to current params
     */
    fun updateAndSetParams(update: (Params) -> Params): Boolean {
        val newParams = update(currentParams)
        return setParams(newParams)
    }

    override fun loadInitialPage(onComplete: CompleteAction): Boolean = setPaginationIfNeed {
        currentPagination.loadInitialPage(onComplete)
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

    override fun resetPaging() {
        cancelJob()
        notifyPagingReset()
    }

    fun clear() {
        paginationMap.clear()
    }

    private fun cancelJob() {
        collectJob?.cancel()
        collectJob = null
    }

    private fun notifyPagingReset() {
        onPagingResetObservableEvent.call()
    }

    private fun onPaginationChanged(params: Params) {
        onPagingChangedSingleEvent.value = params
    }

    private fun <R> setPaginationIfNeed(pagingJob: () -> R): R {
        if (!isInitialized()) {
            if (!setDefaultParamsIfNotNull()) throw NullPointerException(
                "Params not set" +
                        " and default params is also null!"
            )
        }

        return pagingJob()
    }
}