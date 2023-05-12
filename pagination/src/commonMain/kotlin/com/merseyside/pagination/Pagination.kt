package com.merseyside.pagination

import com.merseyside.merseyLib.kotlin.entity.result.Result
import com.merseyside.merseyLib.kotlin.logger.Logger
import com.merseyside.pagination.contract.PaginationContract
import com.merseyside.merseyLib.utils.core.savedState.SavedState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

abstract class Pagination<PD, Data, Page>(
    parentScope: CoroutineScope,
    initPage: Page,
    savedState: SavedState = SavedState()
) : BasePagination<PD, Data, Page>(parentScope, initPage, savedState),
    PaginationContract<Data> where PD : PagerData<Data, Page> {

    private val mutPageResultFlow = MutableSharedFlow<Result<Data>>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val onPageResultFlow: Flow<Result<Data>> = mutPageResultFlow

    override val onPageResultInternal: (Result<Data>) -> Unit = { result ->
        mutPageResultFlow.tryEmit(result)
    }

    var onPageResult: (Result<Data>) -> Unit = {}

    override fun loadNextPage(onComplete: () -> Unit): Boolean {
        if (isLoading()) return false
        if (!pagesManager.isFirstPageLoaded) return false
        if (pagesManager.getNextPage() == null) {
            Logger.logInfo(tag, "No next page!")
            return false
        }

        loadPageInternal(
            pagesManager.getNextPage(),
            onPageResultInternal,
            onComplete,
            ::loadPage
        )

        return true
    }


    override val tag: String = "Pagination"
}

typealias PaginationData<Data> = Pagination<*, Data, *>