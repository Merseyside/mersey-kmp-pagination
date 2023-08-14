package com.merseyside.pagination

import com.merseyside.pagination.data.PagerData

class PositionPagerData<Data>(
    data: Data,
    currentPage: Int
) : PagerData<Data, Int>(data, currentPage + 1, currentPage - 1)