package com.merseyside.pagination.pagesManager

import com.merseyside.merseyLib.utils.core.savedState.SavedState
import com.merseyside.merseyLib.utils.core.savedState.delegate.value
import com.merseyside.merseyLib.utils.core.savedState.delegate.valueOrNull

class PaginationPagesManager<Page>(
    private val initPage: Page,
    private val savedState: SavedState
) {

    var isFirstPageLoaded = false

    var currentPage: Page? by savedState.value(initPage)

    private var _prevPage: Page? by savedState.valueOrNull()
    private var _nextPage: Page? by savedState.valueOrNull()

    fun getPrevPage(): Page? {
        if (!isFirstPageLoaded) throw IllegalStateException("First page not loaded!")
        return _prevPage
    }

    fun getNextPage(): Page? {
        if (!isFirstPageLoaded) throw IllegalStateException("First page not loaded!")
        return _nextPage
    }

    fun onPageLoaded(loadedPage: Page?, nextPage: Page?, prevPage: Page? = null) {
        currentPage = loadedPage

        _prevPage = prevPage
        _nextPage = nextPage
    }

    fun reset() {
        savedState.clear()

        isFirstPageLoaded = false
        currentPage = initPage
        _prevPage = null
        _nextPage = null
    }
}