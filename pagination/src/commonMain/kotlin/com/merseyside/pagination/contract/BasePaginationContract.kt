package com.merseyside.pagination.contract

import com.merseyside.merseyLib.kotlin.entity.result.Result
import com.merseyside.merseyLib.kotlin.observable.ObservableField
import com.merseyside.pagination.state.PagingState

interface BasePaginationContract<Data> {

    val pageSize: Int

    fun setCurrentPosition(position: Int)

    val isFirstPageLoaded: Boolean

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

    fun loadCurrentPage(onComplete: () -> Unit = {}): Boolean

    fun resetPaging()

    fun getPagingState(): PagingState
}