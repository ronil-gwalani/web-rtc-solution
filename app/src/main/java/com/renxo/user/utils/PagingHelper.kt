package com.renxo.user.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow

private const val LIMIT = "limit"
private const val OFFSET = "offset"
private const val REQUEST_ID = "requestID"
private const val HAS_MORE = "hasMore"
private const val NEXT_OFFSET = "nextOffset"
private const val TOTAL_FETCHED = "totalFetched"
private const val TOTAL_RECORDS = "totalRecords"

data class PagingHelper(
    var hasMore: Boolean = false,
    var limit: Int = 100,
    var nextOffset: Int = 0,
    var totalFetched: Int = 0,
    var totalRecords: Int = 0,
    var isLoading: Boolean = false,
    var requestID: String? = null,
) {

    internal lateinit var listState: LazyListState
    internal var loadMoreItems: (() -> Unit)? = null
    fun onLoadMore(callback: () -> Unit) {
        loadMoreItems = callback
    }

    fun setPagingParams(map: HashMap<String, String?>) {
        with(map) {
            get(NEXT_OFFSET)?.toIntOrNull()?.let {
                nextOffset = it
            }
            get(TOTAL_RECORDS)?.toIntOrNull()?.let {
                totalRecords = it
            }
            get(TOTAL_FETCHED)?.toIntOrNull()?.let {
                totalFetched = it
            }
            get(NEXT_OFFSET)?.toIntOrNull()?.let { nextOffset ->
                get(OFFSET)?.toIntOrNull()?.let { offset ->
                    limit = nextOffset - offset
                }
            }
            get(HAS_MORE)?.toBooleanStrictOrNull()?.let {
                hasMore = it
            }
            get(REQUEST_ID)?.let {
                requestID = it
            }
        }
        isLoading = false

    }

    fun getPagingParams(map: HashMap<String, Any?>) {
        with(map) {
            put(LIMIT, limit)
            put(OFFSET, nextOffset)
            requestID?.let {
                put(REQUEST_ID, it)
            }
        }
        isLoading = true

    }

    fun reset() {
        hasMore = false
        limit = 100
        nextOffset = 0
        totalFetched = 0
        totalRecords = 0
        isLoading = false
        requestID = null
    }

}


@Composable
fun rememberPagingHelper(): PagingHelper {
    val state = remember { PagingHelper() }
    state.listState = rememberLazyListState()
    LaunchedEffect(state) {
        snapshotFlow { state.listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
//                Log.e(
//                    "TestingTesting",
//                    "${state.listState.layoutInfo.totalItemsCount}-${(lastVisibleIndex ?: 0)}< 10   ${state.hasMore}   ${state.totalFetched}   !=   ${state.totalRecords}   ${!state.isLoading}"
//                )
                if (state.listState.layoutInfo.totalItemsCount - (lastVisibleIndex
                        ?: 0) < 10 && state.hasMore &&
                    state.totalFetched != state.totalRecords &&
                    !state.isLoading
                ) {
                    state.loadMoreItems?.invoke()
                }

            }
    }

    return state
}