package com.merseyside.pagination.data

fun <Data, Page, R> PagerData<Data, Page>.map(map: (Data) -> R): PagerData<R, Page> {
    return PagerData(map(data), nextPage, prevPage)
}