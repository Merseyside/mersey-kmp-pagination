package com.merseyside.pagination

class PositionPagerData<Data>(
    data: Data,
    currentPage: Int
) : PagerData<Data, Int>(data, currentPage + 1, currentPage - 1)