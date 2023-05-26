package com.merseyside.pagination.contract

interface BasePaginationContract {
    val isFirstPageLoaded: Boolean

    fun loadInitialPage(onComplete: () -> Unit = {}): Boolean

    fun loadCurrentPage(onComplete: () -> Unit = {}): Boolean

    fun resetPaging()

    fun addOnPagingResetCallback(block: () -> Unit)

    fun notifyPagingReset()
}