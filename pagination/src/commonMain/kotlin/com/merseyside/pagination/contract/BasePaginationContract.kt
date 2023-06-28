package com.merseyside.pagination.contract

import com.merseyside.merseyLib.kotlin.observable.ObservableField

interface BasePaginationContract {
    val isFirstPageLoaded: Boolean

    val onResetEvent: ObservableField<Unit>

    fun loadInitialPage(onComplete: () -> Unit = {}): Boolean

    fun loadCurrentPage(onComplete: () -> Unit = {}): Boolean

    fun resetPaging()
}