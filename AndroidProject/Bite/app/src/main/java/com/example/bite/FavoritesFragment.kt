package com.example.bite

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bite.models.Recipe
import com.example.bite.models.RecipeLocalData
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
                    Toast.makeText(context, "No favorites found", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "No more favorites found", Toast.LENGTH_SHORT).show()
                }
            }
            loading = false
        }
    }
}
