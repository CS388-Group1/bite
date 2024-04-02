package com.example.bite

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.bite.models.InstructionStep
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

                // Update UI with fetched recipe details
                recipe?.let {
                    findViewById<TextView>(R.id.recipeLabel).text = "Recipe" // Set recipe label
                    findViewById<TextView>(R.id.recipeTitle).text = recipe.name // Set recipe title
                    findViewById<TextView>(R.id.recipeDescription).text =
                        HtmlCompat.fromHtml(recipe.description, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    findViewById<TextView>(R.id.recipeAuthor).text = "By " + recipe.sourceName
                    // Set other recipe details like author, description, and image
                    // Use Glide or any other image loading library to load the image
                    Glide.with(this@RecipeDetailActivity).load(recipe.imageUrl)
                        .into(findViewById(R.id.recipeImage))


                    findViewById<TextView>(R.id.recipeInstructions).text =
                        buildInstructionsString(recipe.instructions)
                }

            } finally {
                // Hide loading layout
                findViewById<View>(R.id.loadingGraphic).visibility = View.GONE
                findViewById<View>(R.id.mainContent).visibility = View.VISIBLE
            }
        }
    }
    private fun buildInstructionsString(instructionSteps: List<InstructionStep>?): String {
        val instructionsStringBuilder = StringBuilder()
        instructionSteps?.forEach { step ->
            instructionsStringBuilder.append("${step.number}. ${step.step}\n")
        }
        return instructionsStringBuilder.toString()
    }


}
