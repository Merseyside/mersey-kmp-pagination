package com.merseyside.pagination.pagesManager

import com.merseyside.merseyLib.kotlin.logger.Logger
import com.merseyside.merseyLib.kotlin.logger.log
import com.merseyside.merseyLib.utils.core.savedState.SavedState
import com.merseyside.merseyLib.utils.core.savedState.delegate.value

class PaginationPagesManager<Page>(
    private val initPage: Page?,
    private val pageSize: Int,
    private val savedState: SavedState
) {

    private val loadedPageList: MutableList<Page?> = mutableListOf()

    var isInitialPageLoaded = false
        private set

    var startingPage: Page? by savedState.value(defValue = initPage)
    var savedStartingPosition: Int by savedState.value(defValue = 0)

    private var _nextPage: Page? = null
    private var _prevPage: Page? = null

    private var onSavePagingPositionCallback: OnSavePagingPositionCallback? = null

    fun getNextPage(): Page? {
        requireInitialPageLoaded()
        return _nextPage
    }

    fun getPrevPage(): Page? {
        requireInitialPageLoaded()
        return _prevPage
    }

    fun onPageLoaded(
        loadedPage: Page?,
        isEmptyData: Boolean,
        nextPage: Page?,
        prevPage: Page? = null
    ) {
        if (!isInitialPageLoaded) {
            isInitialPageLoaded = true
            if (startingPage != null) _prevPage = prevPage
            _nextPage = nextPage
            loadedPageList.add(loadedPage)

        } else if (loadedPage == _nextPage) {
            onNextPageLoaded(loadedPage, isEmptyData, nextPage)
        } else {
            onPrevPageLoaded(loadedPage, isEmptyData, prevPage)
        }
    }

    private fun onNextPageLoaded(loadedPage: Page?, isEmptyData: Boolean, nextPage: Page?) {
        _nextPage = nextPage
        if (!isEmptyData) loadedPageList.add(loadedPage)
    }

    private fun onPrevPageLoaded(loadedPage: Page?, isEmptyData: Boolean, prevPage: Page?) {
        _prevPage = prevPage
        if (!isEmptyData) loadedPageList.add(0, loadedPage)
    }

    fun softReset() {
        isInitialPageLoaded = false
        savedStartingPosition %= pageSize
        loadedPageList.clear()
        _prevPage = null
        _nextPage = null
        setOnSavePagingPositionCallback(null)
    }

    fun reset() {
        startingPage = initPage
        savedStartingPosition = 0
        savedState.clear()
        softReset()
    }

    fun setOnSavePagingPositionCallback(callback: OnSavePagingPositionCallback?) {
        if (callback != null) {
            savedState.setOnPreSaveStateCallback { onSaveState() }
        } else {
            savedState.setOnPreSaveStateCallback(null)
        }

        onSavePagingPositionCallback = callback
    }

    private fun onSaveState() {
        "onSaveState".log()
        if (isInitialPageLoaded) {
            val itemPosition = onSavePagingPositionCallback?.getCurrentPagingItemPosition() ?: 0
            setCurrentPosition(itemPosition)
        }
    }

    private fun setCurrentPosition(position: Int) {
        if (position <= loadedPageList.size * pageSize) {
            val pageIndex = position / pageSize
            savedStartingPosition = position
            startingPage = loadedPageList[pageIndex]
        } else {
            Logger.logInfo("Invalid position. Skipped.")
        }
    }

    private fun requireInitialPageLoaded() {
        check(isInitialPageLoaded) {
            "Initial page not loaded!"
        }
    }

    fun interface OnSavePagingPositionCallback {
        fun getCurrentPagingItemPosition(): Int
    }
}