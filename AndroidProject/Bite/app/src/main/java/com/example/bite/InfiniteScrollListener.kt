package com.example.bite

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class InfiniteScrollListener(
    private val layoutManager: LinearLayoutManager,
    private var loading: () -> Boolean,
    private val setLoading: (Boolean) -> Unit,
    private val pageSize: Int, // Added pageSize to the constructor
    private val loadMore: () -> Unit
) : RecyclerView.OnScrollListener() {
    private var previousTotal = 0
    private val visibleThreshold = 5

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (dy <= 0 || loading()) {
            return
        }

        val totalItemCount = layoutManager.itemCount
        val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

        if (loading() && (totalItemCount > previousTotal)) {
            setLoading(false)
            previousTotal = totalItemCount
        }

        if (!loading() && (lastVisibleItem + visibleThreshold) >= totalItemCount) {
            if (totalItemCount % pageSize > 0 && totalItemCount < pageSize) {
                return
            }
            loadMore()
            setLoading(true)
        }
    }
}
