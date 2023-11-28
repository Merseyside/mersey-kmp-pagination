package com.merseyside.pagination.parametrized.ext

import com.merseyside.pagination.parametrized.ParametrizedPagination

/**
 * Compare updated params and current params. If equals @return null and updatedParams otherwise
 */
fun <Params : Any> ParametrizedPagination<*, *, Params>.updateParams(
    update: (Params) -> Params
): Params? {
    val params = getCurrentOrDefaultParams()

    check(params != null) {
        "Nothing to update! Pass params or override getDefaultParams() correctly."
    }

    val updatedParams = update(params)
    return if (updatedParams != params) updatedParams
    else null
}

/**
 * Return true if new provided params are not equal to current params
 */
fun <Params : Any> ParametrizedPagination<*, *, Params>.updateAndSetParams(
    update: (Params) -> Params
): Boolean {
    val updatedParams = updateParams(update)

    return if (updatedParams != null) {
        setParams(updatedParams)
    } else false
}