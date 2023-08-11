package com.merseyside.pagination.contract

import com.merseyside.merseyLib.kotlin.observable.ObservableField
import com.merseyside.pagination.formatter.DataFormatter

interface BasePaginationContract<Data> {
    val isFirstPageLoaded: Boolean

    /**
     * Event means that pagination/current parametrized pagination reset.
     * It means pages were erased and you have to start it by loading initial page.
     */
    val onResetEvent: ObservableField<Unit>

    fun loadInitialPage(onComplete: () -> Unit = {}): Boolean

    fun loadCurrentPage(onComplete: () -> Unit = {}): Boolean

    fun resetPaging()
}