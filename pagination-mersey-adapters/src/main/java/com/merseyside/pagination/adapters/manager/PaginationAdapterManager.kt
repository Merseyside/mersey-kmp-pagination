package com.merseyside.pagination.adapters.manager

import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.merseyside.adapters.AdapterManager
import com.merseyside.adapters.core.base.BaseAdapter
import com.merseyside.merseyLib.kotlin.observable.Disposable
import com.merseyside.pagination.OneWayPagination
import com.merseyside.pagination.PaginationHandler
import com.merseyside.pagination.parametrized.BaseParametrizedPagination
import com.merseyside.pagination.parametrized.OneWayParametrizedPagination
import com.merseyside.pagination.positionSaver.PaginationPositionSaver

abstract class PaginationAdapterManager<Key, Adapter, Paging, Data, Params : Any>(
    private val parametrizedPagination: OneWayParametrizedPagination<Paging, Data, Params>
) : AdapterManager<Key, Adapter>()
        where Adapter : BaseAdapter<*, *>,
              Paging : OneWayPagination<*, Data, *> {

    private val paginationHandler = PaginationHandler(parametrizedPagination)

    private val pagination: Paging
        get() = parametrizedPagination.currentPagination

    /**
     * Will be called if pagination hasn't been initialized.
     * Use it in order to set starting pagination.
     */
    var initPagination: () -> Unit = {}

    private var onPaginationChangedDisposable: Disposable<Params>? = null
    private var onPaginationResetDisposable: Disposable<Unit>? = null

    final override fun createAdapter(key: Key, lifecycleOwner: LifecycleOwner): Adapter {
        return createPagingAdapter(key, pagination, lifecycleOwner)
    }

    abstract fun createPagingAdapter(
        key: Key,
        pagination: Paging,
        lifecycleOwner: LifecycleOwner
    ): Adapter

    protected open fun onPaginationChanged(params: Params) {
        setAdapterByKey(getKeyByParams(params))
    }

    abstract fun getKeyByParams(params: Params): Key

    abstract fun createPositionSaver(): PaginationPositionSaver<Adapter>?

    override fun onCreate(lifecycleOwner: LifecycleOwner) {
        with(parametrizedPagination) {
            setKeepInstances(true)
            setSavingStateBehaviour(BaseParametrizedPagination.Behaviour.KEEP_STATE)

            onPaginationChangedDisposable = onPagingChangedEvent.observe { params ->
                onPaginationChanged(params)
            }

            onPaginationResetDisposable = onResetEvent.observe {
                this@PaginationAdapterManager.reset()
            }
        }
    }

    override fun onRecyclerAttached(
        recyclerView: RecyclerView,
        recyclerLifecycleOwner: LifecycleOwner
    ) {
        if (!parametrizedPagination.isInitialized()) {
            initPagination()
        } else {
            check(recyclerView.adapter == null) {
                "Adapter has been already set!"
            }

            val key = getKeyByParams(parametrizedPagination.currentParams)
            setAdapterByKey(key)
        }

        paginationHandler.setPositionSaver(createPositionSaver())
        paginationHandler.setRecyclerView(recyclerView)
    }

    override fun onRecyclerDetached(
        recyclerView: RecyclerView,
        recyclerLifecycleOwner: LifecycleOwner
    ) {
        paginationHandler.setPositionSaver(null)
        paginationHandler.setRecyclerView(null)
    }

    override fun onDestroy(lifecycleOwner: LifecycleOwner) {
        parametrizedPagination.softReset()
        onPaginationChangedDisposable?.dispose()
        onPaginationResetDisposable?.dispose()
    }
}