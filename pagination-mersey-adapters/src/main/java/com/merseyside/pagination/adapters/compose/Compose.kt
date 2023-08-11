package com.merseyside.pagination.adapters.compose

import androidx.recyclerview.widget.RecyclerView
import com.merseyside.adapters.compose.dsl.context.ComposeContext
import com.merseyside.adapters.compose.view.base.SCV
import com.merseyside.adapters.compose.view.list.paging.ComposingPagingList
import com.merseyside.adapters.compose.view.list.simple.ComposingListStyle
import com.merseyside.adapters.compose.view.list.simple.ListConfig
import com.merseyside.adapters.core.base.IBaseAdapter
import com.merseyside.adapters.core.base.callback.OnAttachToRecyclerViewListener
import com.merseyside.merseyLib.kotlin.entity.result.Result
import com.merseyside.pagination.PaginationHandler
import com.merseyside.pagination.TwoWayPaginationHandler
import com.merseyside.pagination.contract.PaginationContract
import com.merseyside.pagination.contract.TwoWayPaginationContract
import com.merseyside.pagination.parametrized.ParametrizedPagination

context(ComposeContext)
fun <Data> ComposingPagingList(
    id: String,
    pagination: TwoWayPaginationContract<Data>,
    style: ComposingListStyle.() -> Unit = {},
    pagingConfig: ListConfig.() -> Unit = {},
    viewProvider: (Result<Data>) -> List<SCV>?
): ComposingPagingList<Result<Data>> {
    return ComposingPagingList(id, pagingConfig, style) {
        onNextPage = pagination.onPageResultFlow
        onPrevPage = pagination.onPrevPageResultFlow
        this.viewProvider = viewProvider as (Result<Data>) -> List<SCV>
        pagination.onResetEvent.observe { resetPaging() }

        if (pagination is ParametrizedPagination<*, *, *>) {
            pagination.setKeepInstances(false)
            pagination.onPagingChangedEvent.observe { resetPaging() }
        }
    }
}

context(ComposeContext)
fun <Data> ComposingPagingList(
    id: String,
    pagination: PaginationContract<Data>,
    style: ComposingListStyle.() -> Unit = {},
    pagingConfig: ListConfig.() -> Unit = {},
    viewProvider: (Result<Data>) -> List<SCV>?
): ComposingPagingList<Result<Data>> {
    return ComposingPagingList(id, pagingConfig, style) {
        onNextPage = pagination.onPageResultFlow
        this.viewProvider = viewProvider as (Result<Data>) -> List<SCV>
        pagination.onResetEvent.observe { resetPaging() }

        if (pagination is ParametrizedPagination<*, *, *>) {
            pagination.setKeepInstances(false)
            pagination.onPagingChangedEvent.observe { resetPaging() }
        }
    }
}

context(ComposeContext)
fun <Paging : TwoWayPaginationContract<Data>, Data> ComposingPagingList(
    id: String,
    paginationHandler: TwoWayPaginationHandler<Paging, Data>,
    style: ComposingListStyle.() -> Unit = {},
    pagingConfig: ListConfig.() -> Unit = {},
    viewProvider: (Result<Data>) -> List<SCV>?
): ComposingPagingList<Result<Data>> {
    return ComposingPagingList(
        id,
        paginationHandler.pagination,
        style,
        wrapWithHandlerConfig(paginationHandler, pagingConfig),
        viewProvider
    )
}

context(ComposeContext)
fun <Paging : PaginationContract<Data>, Data> ComposingPagingList(
    id: String,
    paginationHandler: PaginationHandler<Paging, Data>,
    style: ComposingListStyle.() -> Unit = {},
    pagingConfig: ListConfig.() -> Unit = {},
    viewProvider: (Result<Data>) -> List<SCV>?
): ComposingPagingList<Result<Data>> {

    return ComposingPagingList(
        id,
        paginationHandler.pagination,
        style,
        wrapWithHandlerConfig(paginationHandler, pagingConfig),
        viewProvider
    )
}

context(ComposeContext)
private fun wrapWithHandlerConfig(
    paginationHandler: PaginationHandler<*, *>,
    pagingConfig: ListConfig.() -> Unit = {}
): ListConfig.() -> Unit = {
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