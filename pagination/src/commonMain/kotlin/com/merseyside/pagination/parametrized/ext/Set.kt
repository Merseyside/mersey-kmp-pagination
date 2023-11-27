package com.merseyside.pagination.parametrized.ext

import com.merseyside.pagination.parametrized.ParametrizedPagination

inline fun <Params : Any> ParametrizedPagination<*, *, Params>.setParams(
    params: Params,
    doBefore: () -> Unit
): Boolean {
    doBefore()
    return setParams(params)
}