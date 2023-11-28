@file:Suppress("UNCHECKED_CAST")

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
import com.merseyside.merseyLib.kotlin.observable.lifecycle.asLiveData
import com.merseyside.pagination.PaginationHandler
import com.merseyside.pagination.contract.PaginationContract
import com.merseyside.pagination.parametrized.ParametrizedPagination
import com.merseyside.pagination.parametrized.ext.onResetOrPagingChangedEvent


context(ComposeContext)
fun <Paging : PaginationContract<Data>, Data> ComposingPagingList(
    id: String,
    pagination: Paging,
    style: ComposingListStyle.() -> Unit = {},
    pagingConfig: ListConfig.() -> Unit = {},
    viewProvider: (Result<Data>) -> List<SCV>?
): ComposingPagingList<Result<Data>> {

    val paginationHandler = PaginationHandler(pagination)

    val config = wrapWithHandlerConfig(paginationHandler, pagingConfig)

    return ComposingPagingList(id, config, style) {
        onNextPage = pagination.onPageResultFlow
        onPrevPage = pagination.onPrevPageResultFlow
        this.viewProvider = viewProvider as (Result<Data>) -> List<SCV>
        pagination.onResetEvent.asLiveData().observe(lifecycleOwner) { resetPaging() }

        if (pagination is ParametrizedPagination<*, *, *>) {
            pagination.onResetOrPagingChangedEvent().asLiveData().observe(lifecycleOwner) {
                resetPaging()
            }
        }
    }
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