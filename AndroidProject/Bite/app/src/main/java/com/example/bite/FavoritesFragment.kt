package com.example.bite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bite.models.Recipe
import com.example.bite.models.RecipeLocalData
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {
    private val favRecipes = mutableListOf<Recipe>()
    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var favoriteAdapter: RecipeAdapter
    private val favoriteRepository = activity?.let {
        AppDatabase.getInstance(it.applicationContext).recipeDao()
    }?.let { RecipeLocalData(it, requireActivity().applicationContext) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)
        favoritesRecyclerView = view.findViewById(R.id.favorites)

        lifecycleScope.launch {
           val dataList = favoriteRepository?.getFavoriteRecipes()

            Toast.makeText(requireActivity().applicationContext,dataList.toString(), Toast.LENGTH_LONG).show()

            if (dataList != null) {
                favRecipes.addAll(dataList)
                favoriteAdapter.updateRecipes(favRecipes)
                favoritesRecyclerView.adapter = favoriteAdapter
            }
        }

        favoriteAdapter = RecipeAdapter(favRecipes) { recipe ->
            val intent = android.content.Intent(
                requireContext(),
                RecipeDetailActivity::class.java
            )
            intent.putExtra("RECIPE_ID", recipe.id)
            startActivity(intent)
        }
        favoritesRecyclerView.adapter = favoriteAdapter

        favoritesRecyclerView.layoutManager = LinearLayoutManager(context).also {
            val dividerItemDecoration = DividerItemDecoration(context, it.orientation)
            favoritesRecyclerView.addItemDecoration(dividerItemDecoration)
        }

        //Allow button to remove favorites

        return view;
    }
}