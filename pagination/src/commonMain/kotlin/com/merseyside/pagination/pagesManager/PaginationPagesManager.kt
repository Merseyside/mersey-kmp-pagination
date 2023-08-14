package com.merseyside.pagination.pagesManager

import com.merseyside.merseyLib.utils.core.savedState.SavedState
import com.merseyside.merseyLib.utils.core.savedState.delegate.value

class PaginationPagesManager<Page>(
    private val initPage: Page?,
    private val pageSize: Int,
    private val savedState: SavedState
) {

    private val loadedPageList: MutableList<Page?> = mutableListOf()

    var isFirstPageLoaded = false

    var currentPage: Page? by savedState.value(initPage)

    private var _prevPage: Page? = null
    private var _nextPage: Page? = null

    fun getNextPage(): Page? {
        if (!isFirstPageLoaded) throw IllegalStateException("First page not loaded!")
        return _nextPage
    }

    fun getPrevPage(): Page? {
        if (!isFirstPageLoaded) throw IllegalStateException("First page not loaded!")
        return _prevPage
    }

    fun onPageLoaded(loadedPage: Page?, nextPage: Page?, prevPage: Page? = null) {
        if (!isFirstPageLoaded) {
            isFirstPageLoaded = true
            if (currentPage != null) _prevPage = prevPage
            _nextPage = nextPage
            loadedPageList.add(loadedPage)
        } else if (loadedPage == _nextPage) {
            onNextPageLoaded(loadedPage, nextPage)
        } else {
            onPrevPageLoaded(loadedPage, prevPage)
        }
    }

    private fun onNextPageLoaded(loadedPage: Page?, nextPage: Page?) {
        _nextPage = nextPage
        loadedPageList.add(loadedPage)
    }

    private fun onPrevPageLoaded(loadedPage: Page?, prevPage: Page?) {
        _prevPage = prevPage
        loadedPageList.add(0, loadedPage)
    }

    fun reset() {
        savedState.clear()

        isFirstPageLoaded = false
        loadedPageList.clear()
        currentPage = initPage
        _prevPage = null
        _nextPage = null
    }

    internal fun setCurrentPosition(position: Int) {
        val pageIndex = position / pageSize
        currentPage = loadedPageList[pageIndex]
    }
}