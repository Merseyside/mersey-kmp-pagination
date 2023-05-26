package com.merseyside.pagination.parametrized

import com.merseyside.merseyLib.kotlin.entity.result.Result
import com.merseyside.merseyLib.kotlin.logger.Logger
import com.merseyside.pagination.PaginationData
import com.merseyside.pagination.contract.PaginationContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

abstract class ParametrizedPagination<Paging : PaginationData<Data>, Data, Params : Any>(
    protected val parentScope: CoroutineScope,
    var keepInstances: Boolean = false
) : PaginationContract<Data> {

    private var collectJob: Job? = null
    private val paginationMap: MutableMap<Params, Paging> = mutableMapOf()

    private var onPagingResetCallbacks: MutableList<() -> Unit> = mutableListOf()
    private var onPaginationChangedCallbacks: MutableList<(Paging, Params) -> Unit> = mutableListOf()

    var currentParams: Params? = null
        private set

    fun requireParams(): Params {
        return currentParams ?: throw NullPointerException("Params not set!")
    }

    private var _currentPagination: Paging? = null
    val currentPagination: Paging
        get() {
            if (_currentPagination == null) throw IllegalStateException(
                "Pagination is not initialized." +
                        "Probable current params not set."
            )
            else return _currentPagination!!
        }

    private val mutPageResultFlow = MutableSharedFlow<Result<Data>>()
    override val onPageResultFlow: Flow<Result<Data>> = mutPageResultFlow

    override val isFirstPageLoaded: Boolean
        get() = _currentPagination?.isFirstPageLoaded ?: false

    abstract fun createPagination(params: Params): Paging

    fun getPagination(params: Params): Paging? {
        return paginationMap[params]
    }

    fun setParams(params: Params): Boolean {
        if (currentParams == params) {
            Logger.logInfo(this, "Trying to set the same params.")
            return false
        }

        if (!keepInstances) resetPaging()

        currentParams = params

        _currentPagination = getPagination(params) ?: createPagination(params).also { p ->
            paginationMap[params] = p
        }

        collectPagination(currentPagination)
        onPaginationChanged(currentPagination, params)
        return true
    }

    /**
     * Return true if new provided params not equals to current params
     */
    fun updateAndSetParams(update: (Params) -> Params): Boolean {
        val newParams = update(requireParams())
        return setParams(newParams)
    }

    override fun loadNextPage(onComplete: () -> Unit): Boolean {
        return currentPagination.loadNextPage(onComplete)
    }

    override fun loadInitialPage(onComplete: () -> Unit): Boolean {
        return currentPagination.loadInitialPage(onComplete)
    }

    override fun loadCurrentPage(onComplete: () -> Unit): Boolean {
        return currentPagination.loadCurrentPage(onComplete)
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
        _currentPagination = null
        currentParams = null
        paginationMap.clear()
        notifyPagingReset()
    }

    private fun cancelJob() {
        collectJob?.cancel()
        collectJob = null
    }

    override fun addOnPagingResetCallback(block: () -> Unit) {
        onPagingResetCallbacks.add(block)
    }

    override fun notifyPagingReset() {
        onPagingResetCallbacks.forEach { callback -> callback() }
    }

    fun addOnPaginationChangedCallback(block: (Paging, Params) -> Unit) {
        onPaginationChangedCallbacks.add(block)
        if (_currentPagination != null) block(currentPagination, requireParams())
    }

    private fun onPaginationChanged(pagination: Paging, params: Params) {
        onPaginationChangedCallbacks.forEach { callback -> callback(pagination, params) }
    }
}