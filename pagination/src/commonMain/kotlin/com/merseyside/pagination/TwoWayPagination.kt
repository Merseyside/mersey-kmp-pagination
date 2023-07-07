package com.merseyside.pagination

import com.merseyside.merseyLib.kotlin.entity.result.Result
import com.merseyside.merseyLib.kotlin.logger.Logger
import com.merseyside.merseyLib.kotlin.utils.safeLet
import com.merseyside.pagination.contract.TwoWayPaginationContract
import com.merseyside.merseyLib.utils.core.savedState.SavedState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

abstract class TwoWayPagination<PD, Data, Page>(
    parentScope: CoroutineScope,
    initPage: Page,
    savedState: SavedState = SavedState()
) : Pagination<PD, Data, Page>(parentScope, initPage, savedState),
    TwoWayPaginationContract<Data> where PD : PagerData<Data, Page> {

    private val mutPrevPageResultFlow = MutableSharedFlow<Result<Data>>()
    override val onPrevPageResultFlow: Flow<Result<Data>> = mutPrevPageResultFlow

    abstract suspend fun loadPrevPage(page: Page?): PD

    private var onPrevPageResult: suspend (Result<Data>) -> Unit = { result ->
        mutPrevPageResultFlow.emit(result)
    }

    override fun loadPrevPage(onComplete: () -> Unit): Boolean {
        if (isLoading()) return false
        return safeLet(pagesManager.getPrevPage()) { page ->
            loadPageInternal(page, onPrevPageResult, onComplete, ::loadPrevPage)
            true
        } ?: run {
            Logger.logInfo(tag, "No prev page!")
            false
        }
    }
}

typealias TwoWayPaginationData<Data> = TwoWayPagination<*, Data, *>