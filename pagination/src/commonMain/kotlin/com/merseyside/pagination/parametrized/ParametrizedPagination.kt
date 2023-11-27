package com.merseyside.pagination.parametrized

import com.merseyside.merseyLib.utils.core.savedState.SavedState
import com.merseyside.pagination.Pagination
import com.merseyside.pagination.TwoWayPagination
import kotlinx.coroutines.CoroutineScope

abstract class ParametrizedPagination<Paging : Pagination<*, Data, *>, Data, Params : Any>(
    parentScope: CoroutineScope,
    savedState: SavedState = SavedState(),
) : TwoWayParametrizedPagination<Paging, Data, Params>(parentScope, savedState)