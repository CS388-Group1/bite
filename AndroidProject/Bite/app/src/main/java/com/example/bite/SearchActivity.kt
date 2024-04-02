package com.example.bite

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bite.network.SpoonacularRepository
import kotlinx.coroutines.launch

class SearchActivity : ComponentActivity() {
    private lateinit var spoonacularRepository: SpoonacularRepository
    private lateinit var recyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var ingredientAdapter: IngredientAdapter

    private enum class SearchType {
        RECIPE_BY_NAME,
        INGREDIENT_BY_NAME,
        RECIPE_BY_INGREDIENT
    }

    private var currentSearchType: SearchType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        spoonacularRepository = SpoonacularRepository()
        recyclerView = findViewById(R.id.recyclerView)

        recipeAdapter = RecipeAdapter(emptyList()) { recipe ->
            // Handle recipe click here
            val intent = Intent(this@SearchActivity, RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE_ID", recipe.id) // Pass the selected recipe to RecipeDetailActivity
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = recipeAdapter

        ingredientAdapter = IngredientAdapter(emptyList())

        val buttonSearchRecipeByName: Button = findViewById(R.id.buttonSearchRecipeByName)
        val buttonSearchIngredientByName: Button = findViewById(R.id.buttonSearchIngredientByName)
        val buttonSearchRecipeByIngredient: Button = findViewById(R.id.buttonSearchRecipeByIngredient)

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
    }

    private fun searchRecipeByName(query: String) {
        currentSearchType = SearchType.RECIPE_BY_NAME
        lifecycleScope.launch {
            try {
                val recipes = spoonacularRepository.searchRecipeByName(query)
                recipeAdapter.updateRecipes(recipes)
                recyclerView.adapter = recipeAdapter
            } catch (e: Exception) {
                Toast.makeText(this@SearchActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@SearchActivity, "Error: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
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
                Toast.makeText(this@SearchActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}