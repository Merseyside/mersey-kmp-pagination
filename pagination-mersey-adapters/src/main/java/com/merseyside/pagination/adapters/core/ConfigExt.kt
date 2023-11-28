package com.merseyside.pagination.adapters.core

import com.merseyside.adapters.core.feature.pagination.Config
import com.merseyside.adapters.core.model.VM
import com.merseyside.merseyLib.kotlin.entity.result.filterSuccessValues
import com.merseyside.pagination.OneWayPagination
import com.merseyside.pagination.Pagination
import com.merseyside.pagination.parametrized.OneWayParametrizedPagination
import com.merseyside.pagination.parametrized.ParametrizedPagination
import kotlinx.coroutines.flow.filterNotNull
import com.merseyside.pagination.parametrized.ext.onResetOrPagingChangedEvent

fun <Data, Parent, Model : VM<Parent>> Config<Parent, Model>.setPagination(
    oneWayPagination: OneWayPagination<*, List<Data>, *>
) {
    with(oneWayPagination) {
        onNextPageFlow = onPageResultFlow.filterSuccessValues().filterNotNull()
        resetObservableEvent = onResetEvent
    }
}

fun <Data, Parent, Model : VM<Parent>> Config<Parent, Model>.setPagination(
    pagination: Pagination<*, List<Data>, *>
) {
    setPagination(oneWayPagination = pagination)
    onPrevPageFlow = pagination.onPrevPageResultFlow.filterSuccessValues().filterNotNull()
}

fun <Data, Parent, Model : VM<Parent>> Config<Parent, Model>.setParametrizedPagination(
    oneWayParametrizedPagination: OneWayParametrizedPagination<*, List<Data>, *>
) {
    with(oneWayParametrizedPagination) {
        onNextPageFlow = onPageResultFlow.filterSuccessValues().filterNotNull()
        resetObservableEvent = onResetOrPagingChangedEvent()
    }
}

fun <Data, Parent, Model : VM<Parent>> Config<Parent, Model>.setParametrizedPagination(
    parametrizedPagination: ParametrizedPagination<*, List<Data>, *>
) {
    setParametrizedPagination(oneWayParametrizedPagination = parametrizedPagination)
    onPrevPageFlow =
        parametrizedPagination.onPrevPageResultFlow.filterSuccessValues().filterNotNull()
}

