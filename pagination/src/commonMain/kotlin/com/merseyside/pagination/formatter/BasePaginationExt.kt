package com.merseyside.pagination.formatter

import com.merseyside.pagination.BasePagination
import com.merseyside.pagination.data.PagerData

fun <Data : List<Item>, Item> BasePagination<out PagerData<Data, *>, Data, *>.setFilter(
    predicate: (Item) -> Boolean
): DataFormatter<Data> {
    return addDataFormatter { data ->
        @Suppress("UNCHECKED_CAST")
        data.filter(predicate) as Data
    }
}