package com.merseyside.pagination.adapters.manager

import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.merseyside.adapters.AdapterManager
import com.merseyside.adapters.core.base.BaseAdapter
import com.merseyside.merseyLib.kotlin.observable.Disposable
import com.merseyside.pagination.BasePagination
import com.merseyside.pagination.PaginationHandler
import com.merseyside.pagination.parametrized.ParametrizedPagination
import com.merseyside.pagination.positionSaver.PaginationPositionSaver

abstract class PaginationAdapterManager<Key, Adapter, PM, Data, Params : Any>(
    parametrizedPagination: PM
) : AdapterManager<Key, Adapter>()
        where Adapter : BaseAdapter<*, *>,
              PM : ParametrizedPagination<*, Data, Params> {

    private val paginationHandler = PaginationHandler(parametrizedPagination)

    val pagination: PM
        get() = paginationHandler.pagination

    /**
     * Will be called if pagination hasn't been initialized.
     * Use it in order to set starting pagination.
     */
    var initPagination: (PM) -> Unit = {}

    private var onClearedDisposable: Disposable<Unit>? = null
    private var onPaginationChangedDisposable: Disposable<Params>? = null

    final override fun createAdapter(key: Key, lifecycleOwner: LifecycleOwner): Adapter {
        return createPagingAdapter(key, pagination, lifecycleOwner)
    }

    abstract fun createPagingAdapter(
        key: Key,
        pagination: PM,
        lifecycleOwner: LifecycleOwner
    ): Adapter

    abstract fun createPositionSaver(): PaginationPositionSaver<Adapter>?

    override fun onCreate(lifecycleOwner: LifecycleOwner) {
        with(pagination) {
            pagination.setKeepInstances(true)
            pagination.setSavingStateBehaviour(BasePagination.Behaviour.KEEP_STATE)

            onClearedDisposable = onClearedEvent.observe {
                this@PaginationAdapterManager.clear()
            }

            onPaginationChangedDisposable = onPagingChangedEvent.observe { params ->
                onPaginationChanged(params)
            }
        }
    }

    override fun onRecyclerAttached(
        recyclerView: RecyclerView,
        recyclerLifecycleOwner: LifecycleOwner
    ) {
        paginationHandler.setPositionSaver(createPositionSaver())
        paginationHandler.setRecyclerView(recyclerView)

        if (!pagination.isInitialized()) {
            initPagination(pagination)
        }
    }

    override fun onRecyclerDetached(
        recyclerView: RecyclerView,
        recyclerLifecycleOwner: LifecycleOwner
    ) {
        paginationHandler.setPositionSaver(null)
        paginationHandler.setRecyclerView(null)
    }

    override fun onDestroy(lifecycleOwner: LifecycleOwner) {
        onClearedDisposable?.dispose()
        onPaginationChangedDisposable?.dispose()
    }

    protected abstract fun onPaginationChanged(params: Params)

    protected fun restartCurrentPaging() {
        paginationHandler.restartPagination()
    }
}