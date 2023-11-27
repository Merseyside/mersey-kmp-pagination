package com.merseyside.pagination.parametrized.ext

import com.merseyside.merseyLib.kotlin.observable.EventObservableField
import com.merseyside.merseyLib.kotlin.observable.ext.mergeSingleEvent
import com.merseyside.merseyLib.kotlin.observable.ext.toEventObservableField
import com.merseyside.pagination.parametrized.BaseParametrizedPagination
import com.merseyside.pagination.parametrized.ParametrizedPagination

fun BaseParametrizedPagination<*, *, *>.onResetOrPagingChangedEvent(): EventObservableField {
    return mergeSingleEvent(listOf(onResetEvent, onPagingChangedEvent.toEventObservableField()))
}