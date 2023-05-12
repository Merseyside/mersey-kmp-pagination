package com.merseyside.pagination

open class PagerData<Data, Page>(
    val data: Data,
    val nextPage: Page?,
    val prevPage: Page? = null
)