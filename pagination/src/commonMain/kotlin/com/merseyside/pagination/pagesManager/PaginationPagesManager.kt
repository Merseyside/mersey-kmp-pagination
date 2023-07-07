package com.merseyside.pagination.pagesManager

import com.merseyside.merseyLib.utils.core.savedState.SavedState
import com.merseyside.merseyLib.utils.core.savedState.delegate.value
import com.merseyside.merseyLib.utils.core.savedState.delegate.valueOrNull

class PaginationPagesManager<Page>(initPage: Page, private val savedState: SavedState) {

    var isFirstPageLoaded = false

    var initPage: Page? by savedState.value(initPage)

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
        if (!isFirstPageLoaded) {
            isFirstPageLoaded = true
            if (initPage != null) _prevPage = prevPage
            _nextPage = nextPage
        } else if (loadedPage == _nextPage){
            _nextPage = nextPage
        } else {
            _prevPage = prevPage
        }
    }

    fun reset() {
        savedState.clear()

        isFirstPageLoaded = false
        initPage = null
        _prevPage = null
        _nextPage = null
    }
}