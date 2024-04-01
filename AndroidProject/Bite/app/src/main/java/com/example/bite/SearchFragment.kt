package com.example.bite

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bite.network.SpoonacularRepository
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {
    private lateinit var spoonacularRepository: SpoonacularRepository
    private lateinit var recyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var ingredientAdapter: IngredientAdapter

    private enum class SearchType {
        RECIPE_BY_NAME, INGREDIENT_BY_NAME, RECIPE_BY_INGREDIENT
    }

    private var currentSearchType: SearchType? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        spoonacularRepository = SpoonacularRepository()
        recyclerView = view.findViewById(R.id.recyclerView)
        recipeAdapter = RecipeAdapter(emptyList()) { recipe ->
            // Handle recipe click here
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = recipeAdapter

        ingredientAdapter = IngredientAdapter(emptyList())

        val buttonSearchRecipeByName: Button = view.findViewById(R.id.buttonSearchRecipeByName)
        val buttonSearchIngredientByName: Button = view.findViewById(R.id.buttonSearchIngredientByName)
        val buttonSearchRecipeByIngredient: Button = view.findViewById(R.id.buttonSearchRecipeByIngredient)
        val buttonSearchByIngredient: Button = view.findViewById(R.id.buttonSearchByIngredient)


        // Search Recipe by Name
        buttonSearchRecipeByName.setOnClickListener {
            val query = "chicken"
            searchRecipeByName(query)
        }

        // Search Ingredient by Name
        buttonSearchIngredientByName.setOnClickListener {
            val query = "tomato"
            searchIngredientByName(query)
        }

        // Search Recipe by Ingredient
        buttonSearchRecipeByIngredient.setOnClickListener {
            val ingredient = "onion"
            searchRecipeByIngredient(ingredient)
        }

        buttonSearchByIngredient.setOnClickListener {
            val intent = Intent(context, SearchByIngredient::class.java)
            startActivity(intent)
        }


        return view
    }

    private fun searchRecipeByName(query: String) {
        currentSearchType = SearchType.RECIPE_BY_NAME
        lifecycleScope.launch {
            try {
                val recipes = spoonacularRepository.searchRecipeByName(query)
                recipeAdapter.updateRecipes(recipes)
                recyclerView.adapter = recipeAdapter
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchIngredientByName(query: String) {
        currentSearchType = SearchType.INGREDIENT_BY_NAME
        lifecycleScope.launch {
            try {
                val ingredients = spoonacularRepository.searchIngredientByName(query)
                ingredientAdapter.updateIngredients(ingredients.results)
                recyclerView.adapter = ingredientAdapter
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchRecipeByIngredient(ingredient: String) {
        currentSearchType = SearchType.RECIPE_BY_INGREDIENT
        lifecycleScope.launch {
            try {
                val recipes = spoonacularRepository.searchRecipesByIngredients(ingredient)
                recipeAdapter.updateRecipes(recipes)
                recyclerView.adapter = recipeAdapter
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}