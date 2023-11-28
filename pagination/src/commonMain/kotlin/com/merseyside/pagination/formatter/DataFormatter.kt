package com.merseyside.pagination.formatter

fun interface DataFormatter<Data> {

    fun format(data: Data): Data
}