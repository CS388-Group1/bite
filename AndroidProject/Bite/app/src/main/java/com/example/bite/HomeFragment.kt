package com.example.bite

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bite.adapters.HomeRecipeAdapter
import com.example.bite.models.Recipe
import com.example.bite.network.SpoonacularRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private lateinit var spoonacularRepository: SpoonacularRepository
    private lateinit var recipesRv: RecyclerView
    private lateinit var homeRecipeAdapter: HomeRecipeAdapter
    private lateinit var rotdImageView: ImageView
    private lateinit var rotdTitleTextView: TextView
    private lateinit var seeAllButton: Button
    private lateinit var preferencesButton: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        spoonacularRepository = SpoonacularRepository()
        recipesRv = view.findViewById(R.id.recipeRecyclerView)
        recipesRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        homeRecipeAdapter = HomeRecipeAdapter(emptyList()) { recipe ->
            Toast.makeText(requireContext(), "Clicked on ${recipe.title}", Toast.LENGTH_SHORT).show()
        }
        recipesRv.adapter = homeRecipeAdapter

        rotdImageView = view.findViewById(R.id.rotdImageView)
        rotdTitleTextView = view.findViewById(R.id.rotdTitleTextView)

        seeAllButton = view.findViewById(R.id.seeAllButton)
        preferencesButton = view.findViewById(R.id.preferencesButton)

        preferencesButton.setOnClickListener{
            val intent = Intent(requireContext(), PreferencesActivity::class.java)
            requireContext().startActivity(intent)
        }

        // Fetch random recipe asynchronously
        fetchRandomRecipe()

        // Fetch trending recipes asynchronously
        fetchTrendingRecipes()

        return view
    }

    private fun fetchRandomRecipe() {
        lifecycleScope.launch {
            try {
                val recipe = spoonacularRepository.getRandomRecipe()
                Glide.with(this@HomeFragment).load(recipe[0].image).centerCrop().into(rotdImageView)
                // Assuming you want to set the recipe's name to the TextView
                rotdTitleTextView.text = recipe[0].title
            } catch (e: Exception) {
                Log.e("HomeFragment", "Failed to fetch random recipe: ${e.message}")
                Toast.makeText(requireContext(), "Failed to fetch random recipe: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun fetchTrendingRecipes() {
        lifecycleScope.launch {
            try {
                val recipes = spoonacularRepository.getTrendingRecipes()
                homeRecipeAdapter.updateRecipes(recipes)
            } catch (e: Exception) {
                Log.e("HomeFragment", "Failed to fetch trending recipes: ${e.message}")
                Toast.makeText(requireContext(), "Failed to fetch trending recipes: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}