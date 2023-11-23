package com.merseyside.pagination.annotation

@Retention(value = AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.TYPEALIAS,
    AnnotationTarget.PROPERTY
)
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR, message = "This is an internal merseyside.pagination API " +
            "that should not be used from outside of merseyside.pagination"
)
annotation class InternalPaginationApi