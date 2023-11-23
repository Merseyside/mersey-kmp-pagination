package com.merseyside.pagination.contract

import com.merseyside.merseyLib.kotlin.entity.result.Result
import com.merseyside.merseyLib.kotlin.observable.ObservableField
import com.merseyside.pagination.BasePagination
import com.merseyside.pagination.CompleteAction
import com.merseyside.pagination.annotation.InternalPaginationApi
import com.merseyside.pagination.pagesManager.PaginationPagesManager
import com.merseyside.pagination.state.PagingState

interface BasePaginationContract<Data> {

    val pageSize: Int

    val isInitialPageLoaded: Boolean

    /**
     * Event means that pagination/current parametrized pagination reset.
     * It means pages were erased and you have to start it by loading initial page.
     */
    val onResetEvent: ObservableField<Unit>

    /**
     * Calls when pagination starts to load new page
     * Emit true when loading was started, false when loading ends.
     */
    val onStateChangedEvent: ObservableField<Result<Data>>

    fun setSavingStateBehaviour(behaviour: BasePagination.Behaviour)

    @InternalPaginationApi
    fun loadCurrentPage(onComplete: CompleteAction = {}): Boolean

    fun resetPaging()

    fun getPagingState(): PagingState

    @InternalPaginationApi
    fun notifyWhenReady(onReady: (startingPosition: Int) -> Unit)

    @InternalPaginationApi
    fun removeNotifyWhenReady()

    fun setOnSavePagingPositionCallback(callback: PaginationPagesManager.OnSavePagingPositionCallback?)

    @InternalPaginationApi
    fun cancelLoading()
}