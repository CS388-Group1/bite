package com.example.bite

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bite.models.Recipe
import com.example.bite.models.RecipeLocalData
import com.facebook.shimmer.ShimmerFrameLayout
import com.tapadoo.alerter.Alerter
import kotlinx.coroutines.launch


class FavoritesFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecipeAdapter
    private var loading = false
    private var currentOffset = 0
    private val pageSize = 10

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("FavoritesFragment", "onCreateView called")
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)
        val container = view.findViewById(R.id.shimmer_layout_favorite) as ShimmerFrameLayout;
        container.startShimmer()
        setupRecyclerView(view)


        // Reset the current offset every time the view is created
        currentOffset = 0

        val backButton = view.findViewById<ImageButton>(R.id.exit)
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        retrieveFavorite(currentOffset, pageSize)
        return view
    }

    private fun setupRecyclerView(view: View) {
        Log.d("FavoritesFragment", "Setting up RecyclerView")
        recyclerView = view.findViewById(R.id.favoritesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = RecipeAdapter(mutableListOf()) { recipe ->
            // Handle recipe click
        }
        recyclerView.adapter = adapter
        adapter.onFavoriteClicked = { recipe ->
            if(adapter.onFavoriteClick(recipe)){
                activity?.let {
                    Alerter.create(it)
                        .setTitle("Bite Favorites")
                        .setText("Item added to Favorites")
                        .setBackgroundColorRes(R.color.green)
                        .setDuration(10000)
                        .show()
                }
            }else{
                activity?.let {
                    Alerter.create(it)
                        .setTitle("Bite Favorites")
                        .setText("Item removed from Favorites")
                        .setBackgroundColorRes(R.color.green)
                        .setDuration(10000)
                        .show()
                }
            }

        }
        recyclerView.addOnScrollListener(
            InfiniteScrollListener(
                layoutManager = recyclerView.layoutManager as LinearLayoutManager,
                loading = { loading },
                setLoading = { isLoading ->
                    loading = isLoading
                },
                pageSize = pageSize,
                loadMore = {
                    retrieveFavorite(currentOffset, pageSize)
                })
        )
    }

    private fun retrieveFavorite(offset: Int, limit: Int) {
        if (loading) {
            Log.d("FavoritesFragment", "Already loading, exiting retrieveFavorite")
            return
        }
        Log.d("FavoritesFragment", "Loading favorites, offset: $offset, limit: $limit")
        loading = true
        lifecycleScope.launch {
            val favoriteRepository = RecipeLocalData(
                AppDatabase.getInstance(requireContext()).recipeDao(),
                requireContext()
            )
            val recipes = favoriteRepository.getFavoriteRecipes(limit, offset)
            Log.d("FavoritesFragment", "Recipes fetched: ${recipes.size}")
            if (recipes.isNotEmpty()) {
                adapter.addRecipes(recipes)
                currentOffset += recipes.size
            } else {
                if (offset == 0) {
                    activity?.let {
                        Alerter.create(it)
                            .setTitle("Bite Favorites")
                            .setText("No favorites found")
                            .setDuration(10000)
                            .show()
                    }
                } else {
                    activity?.let {
                        Alerter.create(it)
                            .setTitle("Bite Favorites")
                            .setText("No favorites more found")
                            .setDuration(10000)
                            .show()
                    }
                }
            }
            loading = false
            val container = view?.findViewById(R.id.shimmer_layout_favorite) as ShimmerFrameLayout;
            container.stopShimmer()
            container.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}
