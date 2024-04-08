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
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)
        setupRecyclerView(view)
        retrieveFavorite()
        return view
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.favoritesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = RecipeAdapter(favRecipes) { recipe ->
            val intent = Intent(context, RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE_ID", recipe.id)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
    }

    private fun retrieveFavorite() {
        lifecycleScope.launch {
            val favoriteRepository = activity?.let {
                AppDatabase.getInstance(it.applicationContext).recipeDao()
            }?.let {
                RecipeLocalData(it, requireActivity().applicationContext)
            }
            val dataList = favoriteRepository?.getFavoriteRecipes()
            if (dataList != null) {
                favRecipes.clear()
                favRecipes.addAll(dataList)
                adapter.updateRecipes(favRecipes)
            }
        }
    }
}