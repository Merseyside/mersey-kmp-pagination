package com.merseyside.pagination.formatter

import com.merseyside.pagination.BasePagination
import com.merseyside.pagination.PagerData

fun <Data : List<Item>, Item> BasePagination<out PagerData<Data, *>, Data, *>.setFilter(
    predicate: (Item) -> Boolean
): DataFormatter<Data> {
    return addDataFormatter { data ->
        data.filter(predicate) as Data
    }
}