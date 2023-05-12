package com.merseyside.pagination

import kotlinx.coroutines.CoroutineScope

abstract class PositionPagination<Data>(parentScope: CoroutineScope, initPage: Int) :
    Pagination<PositionPagerData<Data>, Data, Int>(parentScope, initPage)