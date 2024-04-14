package com.example.bite

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private val favRecipes = mutableListOf<Recipe>()
    private var loading = false
    private var currentOffset = 0
    private val pageSize = 10
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)
        setupRecyclerView(view)
        retrieveFavorite(currentOffset, pageSize)
        return view
    }
    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.favoritesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = RecipeAdapter(mutableListOf()) { recipe ->
        }
        recyclerView.adapter = adapter
        recyclerView.addOnScrollListener(InfiniteScrollListener(
            layoutManager = recyclerView.layoutManager as LinearLayoutManager,
            loading = { loading },
            setLoading = { isLoading ->
                loading = isLoading
            }
        ) {
            retrieveFavorite(currentOffset, pageSize)
        })
    }

    private fun retrieveFavorite(offset: Int, limit: Int) {
        if (loading) return
        loading = true
        lifecycleScope.launch {
            val favoriteRepository = RecipeLocalData(AppDatabase.getInstance(requireContext()).recipeDao(), requireContext())
            val recipes = favoriteRepository.getFavoriteRecipes(limit, offset)
            if (recipes.isNotEmpty()) {
                adapter.addRecipes(recipes)
                currentOffset += recipes.size
            } else {
//                Toast.makeText(context, "No more favorites found", Toast.LENGTH_SHORT).show()
            }
            loading = false
        }
    }
}