package com.merseyside.pagination.parametrized

import com.merseyside.merseyLib.kotlin.entity.result.Result
import com.merseyside.pagination.PaginationData
import com.merseyside.pagination.contract.PaginationContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

abstract class ParametrizedPagination<Paging : PaginationData<Data>, Data, Params : Any>(
    protected val parentScope: CoroutineScope
) : PaginationContract<Data> {

    private var collectJob: Job? = null
    private val paginationMap: MutableMap<Params, Paging> = mutableMapOf()

    var onPaginationChanged: (params: Params, pagination: Paging) -> Unit = { _, _ -> }

    var currentParams: Params? = null
    private var _currentPagination: Paging? = null

    private var onPagingReset: () -> Unit = {}

    val currentPagination: Paging
        get() {
            if (_currentPagination == null) throw IllegalStateException("Pagination is not initialized." +
                    "Probable current params not set.")
            else return _currentPagination!!
        }

    private val mutPageResultFlow = MutableSharedFlow<Result<Data>>()
    override val onPageResultFlow: Flow<Result<Data>> = mutPageResultFlow

    abstract fun createPagination(params: Params): Paging

    fun getPagination(params: Params): Paging? {
        return paginationMap[params]
    }

    fun setParams(params: Params) {
        currentParams = params

        _currentPagination = getPagination(params) ?: createPagination(params).also { p ->
            paginationMap[params] = p
        }

        initCreatedPagination(currentPagination)
        onPaginationChanged(params, currentPagination)
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

    open fun initCreatedPagination(pagination: Paging) {
        collectJob?.cancel()
        collectJob = pagination
            .onPageResultFlow
            .onEach { mutPageResultFlow.emit(it) }
            .launchIn(parentScope)
    }

    override fun resetPaging() {
        cancelJob()
        currentParams = null
        onPagingReset()
    }

    fun clear() {
        cancelJob()
        paginationMap.clear()
        resetPaging()
    }

    private fun cancelJob() {
        collectJob?.cancel()
        collectJob = null
    }

    override fun addOnPagingResetCallback(block: () -> Unit) {
        onPagingReset = block
    }
}