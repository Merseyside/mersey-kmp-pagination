package com.merseyside.pagination.contract

interface BasePaginationContract {
    fun loadInitialPage(onComplete: () -> Unit = {}): Boolean

    fun loadCurrentPage(onComplete: () -> Unit = {}): Boolean

    fun resetPaging()

    fun addOnPagingResetCallback(block: () -> Unit)
}