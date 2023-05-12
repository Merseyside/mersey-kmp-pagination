package com.merseyside.pagination

import com.merseyside.merseyLib.kotlin.entity.result.Result
import com.merseyside.merseyLib.kotlin.logger.Logger
import com.merseyside.pagination.contract.TwoWayPaginationContract
import com.merseyside.merseyLib.utils.core.savedState.SavedState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

abstract class TwoWayPagination<PD, Data, Page>(
    parentScope: CoroutineScope,
    initNextPage: Page,
    private val initPrevPage: Page,
    savedState: SavedState = SavedState()
) : Pagination<PD, Data, Page>(parentScope, initNextPage, savedState),
    TwoWayPaginationContract<Data> where PD : PagerData<Data, Page> {

    private val mutPrevPageResultFlow = MutableSharedFlow<Result<Data>>()
    override val onPrevPageResultFlow: Flow<Result<Data>> = mutPrevPageResultFlow

    abstract suspend fun loadPrevPage(page: Page?): PD

    private var onPrevPageResult: (Result<Data>) -> Unit = { result ->
        mutPrevPageResultFlow.tryEmit(result)
    }

    override fun loadPrevPage(onComplete: () -> Unit): Boolean {
        if (isLoading()) return false
        if (!pagesManager.isFirstPageLoaded) return false

        if (pagesManager.getPrevPage() == null) {
            Logger.logInfo(tag, "No prev page!")
        }

        loadPageInternal(pagesManager.getPrevPage(), onPrevPageResult, onComplete, ::loadPrevPage)

        return true
    }
}

typealias TwoWayPaginationData<Data> = TwoWayPagination<*, Data, *>