package com.example.bite

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
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
import kotlinx.coroutines.withContext

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
        val userId = intent.getStringExtra("UserID")
        Log.v("IN RECIPE DETAIL", "User id: $userId")

        // Check if UserId is set in the Intent
        if (userId != null) {
            // Check internet connectivity
            if (isInternetAvailable(this)) {
                // Fetch data from Firebase
                if (recipeName != null) {
                    fetchRecipeFromFirebase(userId, recipeName)
                    Log.v("Recipe Detail Fetch:", "Taken from Firebase")
                }
            } else {
                // Fetch recipe from local database
                if (recipeName != null) {
                    fetchRecipeFromLocalDatabase(userId, recipeName)
                    Log.v("Recipe Detail Fetch:", "Taken from local database")
                }
            }
        } else {
            // Fetch recipe directly from Spoonacular API
            fetchRecipeFromSpoonacular(recipeId)
            Log.v("Recipe Detail Fetch:", "Taken from Spoonacular")
        }

        // findViewById<View>(R.id.loadingGraphic).visibility = View.VISIBLE

    }

    private fun fetchRecipeFromLocalDatabase(userId: String, recipeName: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val recipe = recipeName?.let {
                    database.customRecipeDao().getRecipeByUserIdAndName(userId, recipeName)
                }
                runOnUiThread {
                    // Update UI with fetched recipe details
                    recipe?.let {
                        // Update UI with fetched recipe details
                        findViewById<TextView>(R.id.recipeLabel).text = "Recipe"
                        findViewById<TextView>(R.id.recipeTitle).text = recipe.name
                        findViewById<TextView>(R.id.recipeDescription).text =
                            HtmlCompat.fromHtml(recipe.desc, HtmlCompat.FROM_HTML_MODE_LEGACY)
                        findViewById<TextView>(R.id.recipeAuthor).text = "By ${recipe.userId}"

                        // Use Glide to load the recipe image
                        Glide.with(this@RecipeDetailActivity).load(recipe.image)
                            .into(findViewById(R.id.recipeImage))

                        findViewById<TextView>(R.id.recipeInstructions).text = recipe.instructions

                        // TODO: IMPLEMENT GETTING INGREDIENTS
                        // Update Ingredients RecyclerView
                        //                    recipe.ingredients?.let {
                        //                        val recyclerView: RecyclerView = findViewById(R.id.ingredientsRecyclerView)
                        //                        val layoutManager = LinearLayoutManager(this@RecipeDetailActivity)
                        //                        val adapter = RecipeIngredientAdapter(it)
                        //                        recyclerView.layoutManager = layoutManager
                        //                        recyclerView.adapter = adapter
                        //                    }

                    } ?: run {
                        // Recipe not found in local database
                        runOnUiThread {
                            Alerter.create(this@RecipeDetailActivity)
                                .setTitle("Error")
                                .setText("Recipe not found in local database")
                                .setBackgroundColorRes(R.color.red)
                                .setDuration(5000)
                                .show()
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle exception
                e.printStackTrace()
                runOnUiThread {
                    // Show error message to the user
                    Alerter.create(this@RecipeDetailActivity)
                        .setTitle("Error")
                        .setText("Failed to fetch recipe details from local database: ${e.message}")
                        .setBackgroundColorRes(R.color.red)
                        .setDuration(5000)
                        .show()
                }
            }
        }
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return connectivityManager?.activeNetworkInfo?.isConnected ?: false
    }


    private fun fetchRecipeFromFirebase(userId: String, recipeName: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val syncWithFirebase = SyncWithFirebase(database.customRecipeDao())
                val recipeWithIngredients  = syncWithFirebase.getRecipeByUserIdAndName(this@RecipeDetailActivity, userId, recipeName)

                // Update UI with fetched recipe details

                recipeWithIngredients?.let { rw ->
                    val recipe = rw.recipe
                    val ingredients = rw.ingredients

                    if (recipe != null) {
                        Log.d("Recipe Detail", "Recipe found: ${recipe.name}")
                        findViewById<TextView>(R.id.recipeLabel).text = "Recipe"
                        findViewById<TextView>(R.id.recipeTitle).text = recipe.name
                        findViewById<TextView>(R.id.recipeDescription).text =
                            HtmlCompat.fromHtml(recipe.desc, HtmlCompat.FROM_HTML_MODE_LEGACY)
                        findViewById<TextView>(R.id.recipeAuthor).text = "By You"
                        findViewById<TextView>(R.id.recipeInstructions).text = recipe.instructions


                        // Use Glide to load the recipe image on the main thread
                        withContext(Dispatchers.Main) {
                            Glide.with(this@RecipeDetailActivity)
                                .load(recipe.image)
                                .into(findViewById(R.id.recipeImage))
                        }

                    }

                    // Printing Ingredients RecyclerView
                    ingredients.let {
                        val recyclerView: RecyclerView = findViewById(R.id.ingredientsRecyclerView)
                        val layoutManager = LinearLayoutManager(this@RecipeDetailActivity)
                        val adapter = CustomRecipeIngredientAdapter(it)
                        recyclerView.layoutManager = layoutManager
                        recyclerView.adapter = adapter
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
            } finally {
                // Hide loading layout
                shimmerLayout.stopShimmer()
                shimmerLayout.visibility = View.GONE
                findViewById<View>(R.id.loadingGraphic)?.visibility = View.GONE
                findViewById<View>(R.id.mainContent)?.visibility = View.VISIBLE

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