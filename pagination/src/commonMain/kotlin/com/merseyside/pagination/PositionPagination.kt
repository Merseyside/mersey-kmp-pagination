package com.merseyside.pagination

import kotlinx.coroutines.CoroutineScope

abstract class PositionPagination<Data>(
    parentScope: CoroutineScope,
    initPage: Int,
    pageSize: Int
) : TwoWayPagination<PositionPagerData<Data>, Data, Int>(parentScope, initPage, pageSize)