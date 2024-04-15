package com.example.bite

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bite.models.Ingredient
import com.example.bite.models.Recipe
import com.example.bite.network.SpoonacularRepository
import com.example.bite.network.SyncWithFirebase
import com.tapadoo.alerter.Alerter
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecipeDetailActivity : AppCompatActivity() {
    private lateinit var spoonacularRepository: SpoonacularRepository
    private lateinit var favoriteButton: ImageButton
    private lateinit var recipe: Recipe
    private lateinit var shimmerLayout: ShimmerFrameLayout
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        spoonacularRepository = SpoonacularRepository()
        database = AppDatabase.getInstance(this)

        shimmerLayout = findViewById<ShimmerFrameLayout>(R.id.shimmer_layout)
        shimmerLayout.startShimmer()

        // Retrieve recipe ID from Intent
        val recipeId = intent.getStringExtra("RECIPE_ID")
        val recipeName = intent.getStringExtra("RecipeName")
        val userId = intent.getStringExtra("USER_ID")

        // Check if UserId is set in the Intent
        if (!userId.isNullOrBlank()) {
            // Check internet connectivity
            if (isInternetAvailable(this)) {
                // Fetch data from Firebase
                fetchRecipeFromFirebase(userId, recipeName)
            } else {
                // Fetch recipe from local database
                fetchRecipeFromLocalDatabase(recipeId)
            }
        } else {
            // Fetch recipe directly from Spoonacular API
            fetchRecipeFromSpoonacular(recipeId)
        }

        // findViewById<View>(R.id.loadingGraphic).visibility = View.VISIBLE

    }

    private fun fetchRecipeFromFirebase(userId: String, recipeName: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val syncWithFirebase = SyncWithFirebase(database.customRecipeDao())
                val recipe = syncWithFirebase.getRecipeByUserIdAndName(userId, recipeName)

                // Update UI with fetched recipe details
                findViewById<TextView>(R.id.recipeLabel).text = "Recipe"
                findViewById<TextView>(R.id.recipeTitle).text = recipe.title
                findViewById<TextView>(R.id.recipeDescription).text = HtmlCompat.fromHtml(recipe.summary, HtmlCompat.FROM_HTML_MODE_LEGACY)
                findViewById<TextView>(R.id.recipeAuthor).text = "By ${recipe.sourceName}"

                // Use Glide to load the recipe image
                Glide.with(this@RecipeDetailActivity).load(recipe.image)
                    .into(findViewById(R.id.recipeImage))

                findViewById<TextView>(R.id.recipeInstructions).text = recipe.instructions

                // Update Ingredients RecyclerView
                recipe.ingredients?.let {
                    val recyclerView: RecyclerView = findViewById(R.id.ingredientsRecyclerView)
                    val layoutManager = LinearLayoutManager(this@RecipeDetailActivity)
                    val adapter = RecipeIngredientAdapter(it)
                    recyclerView.layoutManager = layoutManager
                    recyclerView.adapter = adapter
                }

                // Handle favorite button click
                favoriteButton = findViewById(R.id.favoriteButton)
                favoriteButton.isSelected = recipe.isFavorite
                favoriteButton.setOnClickListener {
                    recipe.isFavorite = !recipe.isFavorite
                    updateFavorite(recipe, recipe.isFavorite, recipe.id)
                    favoriteButton.isSelected = recipe.isFavorite
                    if (recipe.isFavorite) {
                        Alerter.create(this@RecipeDetailActivity)
                            .setTitle("Bite Favorites")
                            .setText("Added to Favorites")
                            .setBackgroundColorRes(R.color.green)
                            .setDuration(5000)
                            .show()
                    } else {
                        Alerter.create(this@RecipeDetailActivity)
                            .setTitle("Bite Favorites")
                            .setText("Removed from Favorites")
                            .setBackgroundColorRes(R.color.green)
                            .setDuration(5000)
                            .show()
                    }
                }
            } catch (e: Exception) {
                // Handle exception
                e.printStackTrace()
                runOnUiThread {
                    // Show error message to the user
                    Alerter.create(this@RecipeDetailActivity)
                        .setTitle("Error")
                        .setText("Failed to fetch recipe details: ${e.message}")
                        .setBackgroundColorRes(R.color.red)
                        .setDuration(5000)
                        .show()
                }
            }
        }

    private fun fetchRecipeFromSpoonacular(recipeId: String?){
        // Fetch recipe from repository based on ID
        lifecycleScope.launch {
            try {
                recipe = recipeId?.let { spoonacularRepository.getRecipeInfo(it) }!!
                val ingredientsList: List<Ingredient>? = recipeId.let { spoonacularRepository.getIngredients(it) }

                // Update UI with fetched recipe details
                findViewById<TextView>(R.id.recipeLabel).text = "Recipe"
                findViewById<TextView>(R.id.recipeTitle).text = recipe.title
                findViewById<TextView>(R.id.recipeDescription).text = HtmlCompat.fromHtml(recipe.summary, HtmlCompat.FROM_HTML_MODE_LEGACY)
                findViewById<TextView>(R.id.recipeAuthor).text = "By ${recipe.sourceName}"

                // Use Glide to load the recipe image
                Glide.with(this@RecipeDetailActivity).load(recipe.image)
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

                favoriteButton = findViewById(R.id.favoriteButton)
                favoriteButton.isSelected = recipe.isFavorite
                favoriteButton.setOnClickListener {
                    recipe.isFavorite = !recipe.isFavorite
                    updateFavorite(recipe, recipe.isFavorite, recipe.id)
                    favoriteButton.isSelected = recipe.isFavorite
                    if (recipe.isFavorite) {
                        Alerter.create(this@RecipeDetailActivity)
                            .setTitle("Bite Favorites")
                            .setText("Added to Favorites")
                            .setBackgroundColorRes(R.color.green)
                            .setDuration(5000)
                            .show()
                    } else {
                        Alerter.create(this@RecipeDetailActivity)
                            .setTitle("Bite Favorites")
                            .setText("Removed from Favorites")
                            .setBackgroundColorRes(R.color.green)
                            .setDuration(5000)
                            .show()
                    }
                }
            } finally {
                // Hide loading layout
                shimmerLayout.stopShimmer()
                shimmerLayout.visibility = View.GONE
                findViewById<View>(R.id.loadingGraphic)?.visibility = View.GONE
                findViewById<View>(R.id.mainContent)?.visibility = View.VISIBLE

            }
        }
    }

    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return connectivityManager?.activeNetworkInfo?.isConnected ?: false
    }

    private fun updateFavorite(recipe: Recipe, favorite: Boolean, id: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val exists = AppDatabase.getInstance(applicationContext).recipeDao().isRowIsExist(id)
            if (exists) {
                AppDatabase.getInstance(applicationContext).recipeDao().updateRecipe(favorite, id)
            } else {
                AppDatabase.getInstance(applicationContext).recipeDao().insertRecipe(recipe)
            }
        }
    }
}