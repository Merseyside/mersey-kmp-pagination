package com.merseyside.pagination

import com.merseyside.merseyLib.kotlin.entity.result.Result
import com.merseyside.merseyLib.kotlin.logger.Logger
import com.merseyside.merseyLib.kotlin.utils.safeLet
import com.merseyside.pagination.contract.PaginationContract
import com.merseyside.merseyLib.utils.core.savedState.SavedState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

abstract class Pagination<PD, Data, Page>(
    parentScope: CoroutineScope,
    initPage: Page,
    override val pageSize: Int,
    savedState: SavedState = SavedState()
) : BasePagination<PD, Data, Page>(parentScope, initPage, pageSize, savedState),
    PaginationContract<Data> where PD : PagerData<Data, Page> {

    private val mutPageResultFlow = MutableSharedFlow<Result<Data>>()
    override val onPageResultFlow: Flow<Result<Data>> = mutPageResultFlow

    override val onPageResult: suspend (Result<Data>) -> Unit = { result ->
        mutPageResultFlow.emit(result)
    }

    override fun loadNextPage(onComplete: () -> Unit): Boolean {
        if (isLoading()) return false
        return safeLet(pagesManager.getNextPage()) { page ->
            loadPageInternal(page, onPageResult, onComplete, ::loadPage)
            true
        } ?: run {
            Logger.logInfo(tag, "No next page!")
            false
        }
    }


    override val tag: String = "Pagination"
}

internal typealias P<Data> = Pagination<*, Data, *>