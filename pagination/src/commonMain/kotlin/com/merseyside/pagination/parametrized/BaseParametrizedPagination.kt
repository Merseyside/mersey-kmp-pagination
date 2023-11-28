package com.merseyside.pagination.parametrized

import com.merseyside.merseyLib.kotlin.entity.result.Result
import com.merseyside.merseyLib.kotlin.logger.ILogger
import com.merseyside.merseyLib.kotlin.logger.Logger
import com.merseyside.merseyLib.kotlin.observable.*
import com.merseyside.merseyLib.kotlin.utils.safeLet
import com.merseyside.merseyLib.utils.core.savedState.SavedState
import com.merseyside.pagination.BasePagination
import com.merseyside.pagination.CompleteAction
import com.merseyside.pagination.annotation.InternalPaginationApi
import com.merseyside.pagination.contract.BasePaginationContract
import com.merseyside.pagination.pagesManager.PaginationPagesManager
import com.merseyside.pagination.state.PagingState
import kotlinx.coroutines.CoroutineScope

@OptIn(InternalPaginationApi::class)
abstract class BaseParametrizedPagination<Paging : BasePagination<*, Data, *>, Data, Params : Any>(
    protected val parentScope: CoroutineScope,
    private val savedState: SavedState
) : BasePaginationContract<Data>, ILogger {

    private var keepInstances: Boolean = false

    override val pageSize: Int
        get() = currentPagination.pageSize

    private var savingStateBehaviour = Behaviour.SOFT_RESET

    private val paginationMap: MutableMap<Params, Paging> = mutableMapOf()

    private val onPagingResetObservableEvent = SingleObservableEvent()
    override val onResetEvent: EventObservableField = onPagingResetObservableEvent

    private val onPagingChangedMutableEvent = SingleObservableField<Params>()
    val onPagingChangedEvent: ObservableField<Params> = onPagingChangedMutableEvent

    private var _currentParams: Params? = null
    val currentParams: Params
        get() = _currentParams ?: throw NullPointerException("Params not set!")

    private var _currentPagination: Paging? = null
    val currentPagination: Paging
        get() = _currentPagination ?: throw NullPointerException("Pagination not set!")

    private val mutOnStateChangedEvent =
        MutableObservableField<Result<Data>>(Result.NotInitialized())
    override val onStateChangedEvent: ObservableField<Result<Data>> = mutOnStateChangedEvent
    private var mutableStateDisposable: Disposable<Result<Data>>? = null

    private var onSavePagingPositionCallback: PaginationPagesManager.OnSavePagingPositionCallback? =
        null

    override val isInitialPageLoaded: Boolean
        get() = currentPagination.isInitialPageLoaded


    private var onReady: (startingPosition: Int) -> Unit = {}

    abstract fun createPagination(
        parentScope: CoroutineScope,
        params: Params,
        savedState: SavedState
    ): Paging

    abstract fun collectPagination(pagination: Paging)

    abstract fun getDefaultParams(): Params?

    override fun notifyWhenReady(onReady: (startingPosition: Int) -> Unit) {
        this.onReady = onReady
        if (isInitialized()) {
            onReady(currentPagination.getSavedPosition())
        } else {
            setDefaultParamsIfNotNull()
        }
    }

    override fun removeNotifyWhenReady() {
        onReady = {}
    }

    fun isInitialized(): Boolean {
        return _currentParams != null
    }

    fun getPaginationOrNull(params: Params): Paging? {
        return paginationMap[params]
    }

    internal fun getCurrentOrDefaultParams(): Params? {
        return if (isInitialized()) currentParams
        else getDefaultParams()
    }

    fun setSavingStateBehaviour(behaviour: Behaviour) {
        check(keepInstances) {
            "Pagination instances are not keeping by parametrized pagination." +
                    "Please call setKeepInstances and pass true"
        }

        if (behaviour != savingStateBehaviour) {
            savingStateBehaviour = behaviour
        } else Logger.logInfo(tag, "Setting the same savingStateBehaviour. Skipped.")
    }

    fun setParams(params: Params): Boolean {
        if (_currentParams == params) {
            Logger.logInfo(tag, "Trying to set the same params.")
            return false
        } else {
            cancelLoading()
        }

        if (!keepInstances) {
            _currentPagination?.reset()
            _currentPagination = null
        }

        Logger.logInfo(tag, "Set new params")
        _currentParams = params
        val prevPaging = _currentPagination
        _currentPagination = getPagination(params)
        onPaginationChange(prevPaging, currentPagination)
        onPaginationChanged(params)

        return true
    }

    private fun getPagination(params: Params): Paging {
        return if (keepInstances) {
            getPaginationOrNull(params) ?: createPagination(
                parentScope,
                params,
                savedState
            ).also { paging ->
                onPaginationCreated(paging, params)
            }
        } else {
            createPagination(parentScope, params, savedState)
        }
    }

    protected open fun onPaginationCreated(paging: Paging, params: Params) {
        paginationMap[params] = paging
    }

    protected open fun onPaginationChange(prevPaging: Paging?, paging: Paging) {
        safeLet(prevPaging) { prev ->
            prev.saveState()
            when (savingStateBehaviour) {
                Behaviour.SOFT_RESET -> prev.softReset()
                Behaviour.KEEP_STATE -> {} /* do nothing */
            }
            prev.setOnSavePagingPositionCallback(null)
        }

        with(paging) {
            safeLet(onSavePagingPositionCallback) { callback ->
                setOnSavePagingPositionCallback(callback)
            }

            mutableStateDisposable?.dispose()
            observeMutableState(this)

            cancelLoading()
            collectPagination(this)
        }
    }

    private fun onPaginationChanged(params: Params) {
        onPagingChangedMutableEvent.value = params
        onReady(currentPagination.getSavedPosition())
    }

    override fun loadCurrentPage(onComplete: CompleteAction): Boolean = setPaginationIfNeed {
        currentPagination.loadCurrentPage(onComplete)
    }

    open fun observeMutableState(pagination: Paging) {
        mutableStateDisposable = pagination.onStateChangedEvent.observe { state ->
            mutOnStateChangedEvent.value = state
        }
    }

    override fun softReset() {
        _currentPagination?.softReset()
    }

    override fun reset() {
        cancelLoading()

        _currentPagination?.reset()
        _currentPagination = null
        _currentParams = null

        paginationMap.forEach { (_, paging) -> paging.reset() }
        paginationMap.clear()

        onPaginationReset()
    }

    override fun saveState() {
        currentPagination.saveState()
    }

    override fun setOnSavePagingPositionCallback(callback: PaginationPagesManager.OnSavePagingPositionCallback?) {
        onSavePagingPositionCallback = callback
        _currentPagination?.setOnSavePagingPositionCallback(callback)
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
                reset()
            }

            this.keepInstances = keepInstances
        }
    }

    /**
     * @return true if default params not null
     */
    private fun setDefaultParamsIfNotNull(): Boolean {
        return getDefaultParams()?.let { params ->
            setParams(params)
            true
        } ?: false
    }

    private fun onPaginationReset() {
        onPagingResetObservableEvent.call()
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

    enum class Behaviour {
        SOFT_RESET, KEEP_STATE
    }

    override val tag: String
        get() = this::class.simpleName ?: "UnknownParametrizedPaging"
}