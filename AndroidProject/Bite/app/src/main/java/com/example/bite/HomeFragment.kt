package com.example.bite

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bite.adapters.HomeRecipeAdapter
import com.example.bite.models.Recipe
import com.example.bite.network.SpoonacularRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private lateinit var spoonacularRepository: SpoonacularRepository
    private lateinit var recipesRv: RecyclerView
    private lateinit var homeRecipeAdapter: HomeRecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        spoonacularRepository = SpoonacularRepository()
        recipesRv = view.findViewById(R.id.recipeRecyclerView)
        recipesRv.layoutManager = LinearLayoutManager(requireContext())
        homeRecipeAdapter = HomeRecipeAdapter(emptyList()) { recipe ->
            Toast.makeText(requireContext(), "Clicked on ${recipe.name}", Toast.LENGTH_SHORT).show()
        }
        recipesRv.adapter = homeRecipeAdapter

        // Fetch recipes asynchronously
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val recipes = spoonacularRepository.getTrendingRecipes()
                homeRecipeAdapter.updateRecipes(recipes)
            } catch (e: Exception) {
                Log.e("recpies", "Failed to fetch recipes: ${e.message}")
                Toast.makeText(requireContext(), "Failed to fetch recipes: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        return view
    }
}