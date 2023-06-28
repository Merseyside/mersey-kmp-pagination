package com.merseyside.pagination.adapters.compose

import androidx.recyclerview.widget.RecyclerView
import com.merseyside.adapters.compose.dsl.context.ComposeContext
import com.merseyside.adapters.compose.view.base.SCV
import com.merseyside.adapters.compose.view.list.paging.ComposingPagingList
import com.merseyside.adapters.compose.view.list.simple.ListConfig
import com.merseyside.adapters.core.base.IBaseAdapter
import com.merseyside.adapters.core.base.callback.OnAttachToRecyclerViewListener
import com.merseyside.merseyLib.kotlin.entity.result.Result
import com.merseyside.pagination.TwoWayPaginationHandler
import com.merseyside.pagination.contract.PaginationContract
import com.merseyside.pagination.contract.TwoWayPaginationContract

context(ComposeContext)
fun <Data> ComposingPagingList(
    id: String,
    pagination: TwoWayPaginationContract<Data>,
    pagingConfig: ListConfig.() -> Unit = {},
    viewProvider: (Result<Data>) -> List<SCV>
): ComposingPagingList<Result<Data>> {
    return ComposingPagingList(id, pagingConfig) {
        onNextPage = pagination.onPageResultFlow
        onPrevPage = pagination.onPrevPageResultFlow
        this.viewProvider = viewProvider
        pagination.onResetEvent.observe {
            resetPaging()
        }
    }
}

context(ComposeContext)
fun <Data> ComposingPagingList(
    id: String,
    pagination: PaginationContract<Data>,
    pagingConfig: ListConfig.() -> Unit = {},
    viewProvider: (Result<Data>) -> List<SCV>
): ComposingPagingList<Result<Data>> {
    return ComposingPagingList(id, pagingConfig) {
        onNextPage = pagination.onPageResultFlow
        this.viewProvider = viewProvider
        pagination.onResetEvent.observe {
            resetPaging()
        }
    }
}

context(ComposeContext)
fun <Paging : TwoWayPaginationContract<Data>, Data> ComposingPagingList(
    id: String,
    paginationHandler: TwoWayPaginationHandler<Paging, Data>,
    pagingConfig: ListConfig.() -> Unit = {},
    viewProvider: (Result<Data>) -> List<SCV>
): ComposingPagingList<Result<Data>> {
    val paginationContract = paginationHandler.pagination

    val handlerConfig: ListConfig.() -> Unit = {
        pagingConfig()
        val listener = object : OnAttachToRecyclerViewListener {
            override fun onAttached(recyclerView: RecyclerView, adapter: IBaseAdapter<*, *>) {
                paginationHandler.setRecyclerView(recyclerView)
            }
            override fun onDetached(recyclerView: RecyclerView, adapter: IBaseAdapter<*, *>) {
                paginationHandler.setRecyclerView(null)
            }
        }

        addOnAttachToRecyclerViewListener(listener)
    }

    return ComposingPagingList(id, handlerConfig) {
        onNextPage = paginationContract.onPageResultFlow
        onPrevPage = paginationContract.onPrevPageResultFlow
        this.viewProvider = viewProvider
        paginationContract.onResetEvent.observe {
            resetPaging()
        }
    }
}