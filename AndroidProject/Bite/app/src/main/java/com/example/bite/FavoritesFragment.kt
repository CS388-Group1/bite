package com.example.bite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bite.models.Recipe
import com.example.bite.network.IngredientRepository
import com.example.bite.network.SpoonacularRepository
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {
    private val favRecipes = mutableListOf<Recipe>()
    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var favoriteAdapter: RecipeAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)
        favoritesRecyclerView = view.findViewById(R.id.favorites)
        val favoriteAdapter = RecipeAdapter(view.context, favRecipes)
        favoritesRecyclerView.adapter = favoriteAdapter

        lifecycleScope.launch {
           val DataList = (activity?.application as RecipeDataApp).db.recipeDao().getFavoriteRecipes()
            favRecipes.addAll(DataList)
        }
        favRecipes.addAll(DataList)

        favoritesRecyclerView.layoutManager = LinearLayoutManager(context).also {
            val dividerItemDecoration = DividerItemDecoration(context, it.orientation)
            favoritesRecyclerView.addItemDecoration(dividerItemDecoration)
        }
    }
}