package com.example.bite

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bite.models.Ingredient
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

        findViewById<View>(R.id.loadingGraphic).visibility = View.VISIBLE

        // Fetch recipe from repository based on ID
        lifecycleScope.launch {
            try {
                val recipe: Recipe? = recipeId?.let { spoonacularRepository.getRecipeInfo(it) }

                val ingredientsList: List<Ingredient>? = recipeId?.let { spoonacularRepository.getIngredients(it) }

                // Update UI with fetched recipe details
                recipe?.let {
                    findViewById<TextView>(R.id.recipeLabel).text = "Recipe" // Set recipe label
                    findViewById<TextView>(R.id.recipeTitle).text = recipe.name // Set recipe title
                    findViewById<TextView>(R.id.recipeDescription).text =
                        HtmlCompat.fromHtml(recipe.description, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    findViewById<TextView>(R.id.recipeAuthor).text = "By " + recipe.sourceName
                    Glide.with(this@RecipeDetailActivity).load(recipe.imageUrl)
                        .into(findViewById(R.id.recipeImage))
                    findViewById<TextView>(R.id.recipeInstructions).text = recipe.instructions

                    // Update Ingredients RecyclerView
                    ingredientsList?.let {
                        val recyclerView: RecyclerView = findViewById(R.id.ingredientsRecyclerView)
                        val layoutManager = LinearLayoutManager(this@RecipeDetailActivity)
                        val adapter = RecipeIngredientAdapter(ingredientsList)
                        recyclerView.layoutManager = layoutManager
                        recyclerView.adapter = adapter
                    }

                }
            } finally {
                // Hide loading layout
                findViewById<View>(R.id.loadingGraphic).visibility = View.GONE
                findViewById<View>(R.id.mainContent).visibility = View.VISIBLE
            }
        }
    }
//    private fun buildInstructionsString(instructionSteps: List<InstructionStep>?): String {
//        val instructionsStringBuilder = StringBuilder()
//        instructionSteps?.forEach { step ->
//            instructionsStringBuilder.append("${step.number}. ${step.step}\n")
//        }
//        return instructionsStringBuilder.toString()
//    }


}
