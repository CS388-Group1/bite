package com.example.bite

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class InfiniteScrollListener(
    private val layoutManager: LinearLayoutManager,
    private var loading: () -> Boolean,
    private val setLoading: (Boolean) -> Unit,
    private val loadMore: () -> Unit
) : RecyclerView.OnScrollListener() {
    private var previousTotal = 0
    private val visibleThreshold = 5

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (dy <= 0 || loading()) {
            // Log.d("InfiniteScroll", "Early exit from onScrolled, dy: $dy, loading: ${loading()}")
            return
        }

        val totalItemCount = layoutManager.itemCount
        val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
        // Log.d("InfiniteScroll", "Scrolled, totalItemCount: $totalItemCount, lastVisibleItem: $lastVisibleItem, previousTotal: $previousTotal")

        if (loading() && (totalItemCount > previousTotal)) {
            setLoading(false)
            previousTotal = totalItemCount
            // Log.d("InfiniteScroll", "Load complete, totalItemCount: $totalItemCount")
        }

        if (!loading() && (lastVisibleItem + visibleThreshold) >= totalItemCount) {
            loadMore()
            setLoading(true)
            // Log.d("InfiniteScroll", "Loading more items")
        }
    }
}
