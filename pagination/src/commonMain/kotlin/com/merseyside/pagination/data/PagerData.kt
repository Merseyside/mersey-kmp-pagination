package com.merseyside.pagination.data

open class PagerData<Data, Page>(
    val data: Data,
    val nextPage: Page?,
    val prevPage: Page? = null
)