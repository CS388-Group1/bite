package com.example.bite

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bite.models.Recipe
import com.example.bite.network.SpoonacularRepository
import kotlinx.coroutines.launch

class RecipeDetailActivity : AppCompatActivity() {
    private lateinit var spoonacularRepository: SpoonacularRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        spoonacularRepository = SpoonacularRepository()

        // Retrieve recipe ID from Intent
        val recipeId = intent.getStringExtra("RECIPE_ID")

        // Fetch recipe from repository based on ID
        lifecycleScope.launch {
            val recipe: Recipe? = recipeId?.let { spoonacularRepository.getRecipeById(it) }

            // Update UI with fetched recipe details
            recipe?.let {
                findViewById<TextView>(R.id.recipeLabel).text = "Recipe" // Set recipe label
                findViewById<TextView>(R.id.recipeTitle).text = recipe.name // Set recipe title
                findViewById<TextView>(R.id.recipeDescription).text = recipe.description
                // Set other recipe details like author, description, and image
                // Use Glide or any other image loading library to load the image
                Glide.with(this@RecipeDetailActivity).load(recipe.imageUrl).into(findViewById(R.id.recipeImage))
            }
        }
    }

    private fun getDefaultRecipe(): Recipe? {
        // Create a default recipe object with placeholder values
        return Recipe(
            id = "default",
            name = "Default Recipe",
            description = "This is a default recipe.",
            imageUrl = "https://example.com/default_image.jpg",
            cookingTime = 0,
        )
    }
}
