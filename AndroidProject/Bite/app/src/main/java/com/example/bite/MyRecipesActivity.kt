package com.example.bite

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bite.models.CustomRecipeViewModel
import com.example.bite.models.Recipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MyRecipesActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecipeAdapter
    private lateinit var backButton: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var customRecipeViewModel: CustomRecipeViewModel
    private lateinit var noRecipesTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_recipes)

        recyclerView = findViewById(R.id.myRecipesRecyclerView)
        backButton = findViewById(R.id.backButton)
        auth = FirebaseAuth.getInstance()
        customRecipeViewModel = CustomRecipeViewModel(application)
        noRecipesTextView = findViewById(R.id.noRecipesTextView)

        setupRecyclerView()
        fetchCustomRecipes()

        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RecipeAdapter(emptyList()) { recipe ->
            val intent = Intent(this, RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE_ID", recipe.id.toString())
            intent.putExtra("UserID", recipe.userId.toString())
            intent.putExtra("RecipeName", recipe.title.toString())
            Log.v("Custom Recipe Clicked", "user id: ${recipe.userId.toString()}")
            startActivity(intent)
        }
        recyclerView.adapter = adapter
    }

    private fun fetchCustomRecipes() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val customRecipeDao = AppDatabase.getInstance(this@MyRecipesActivity).customRecipeDao()

                    if (isConnectedToInternet(this@MyRecipesActivity)) {
                        // Fetch recipes from Firestore
                        val firestoreRecipes = fetchRecipesFromFirestore(userId)
                        withContext(Dispatchers.Main) {
                            updateUIWithRecipes(firestoreRecipes)
                        }
                    }else {

                        val customRecipes =
                            customRecipeDao.getCustomRecipesWithIngredientsByUserId(userId)
                        val recipeList = customRecipes.map { customRecipeWithIngredients ->
                            val imagePath =
                                if (customRecipeWithIngredients.recipe.image.isNullOrEmpty()) {
                                    "android.resource://com.example.bite/drawable/cookie_transparent_wide"
                                } else {
                                    customRecipeWithIngredients.recipe.image
                                }
                            Recipe(
                                id = customRecipeWithIngredients.recipe.recipeId.toString(), // Set the ID as a string
                                userId = userId,
                                title = customRecipeWithIngredients.recipe.name,
                                image = imagePath,
                                cookingTime = customRecipeWithIngredients.recipe.readyInMinutes,
                                summary = "",
                                sourceName = "",
                                isFavorite = false
                            )
                        }
                        withContext(Dispatchers.Main) {
                            if (recipeList.isEmpty()) {
                                noRecipesTextView.visibility = View.VISIBLE
                                recyclerView.visibility = View.GONE
                            } else {
                                noRecipesTextView.visibility = View.GONE
                                recyclerView.visibility = View.VISIBLE
                                adapter.updateRecipes(recipeList)
                            }

                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MyRecipesActivity, "Error fetching custom recipes: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    private fun isConnectedToInternet(context: Context): Boolean {
        val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager?
        return connectivityManager?.activeNetworkInfo?.isConnected ?: false
    }

    private suspend fun fetchRecipesFromFirestore(userId: String): List<Recipe> {
        val firestoreRecipes = mutableListOf<Recipe>()

        try {
            val firestore = FirebaseFirestore.getInstance()
            val querySnapshot = firestore
                .collection("users")
                .document(userId)
                .collection("createdRecipes")
                .get()
                .await()

            for (document in querySnapshot.documents) {
                val recipeId = document.id
                val name = document.getString("name") ?: ""
                val image = document.getString("image") ?: ""
                val servings = document.getLong("servings") ?: 0
                val readyInMinutes = document.getLong("readyInMinutes") ?: 0
                val instructions = document.getString("instructions") ?: ""

                // Create a Recipe object using the retrieved data
                val recipe = Recipe(
                    id = recipeId,
                    userId = userId,
                    title = name,
                    image = image,
                    cookingTime = readyInMinutes.toInt(),
                    instructions = instructions,
                    summary = "",
                    sourceName = "",
                    isFavorite = false
                )

                firestoreRecipes.add(recipe)
            }
        } catch (e: Exception) {
            // Handle any exceptions, such as Firestore exceptions
            // Log the error or handle it appropriately
        }

        return firestoreRecipes
    }
    private fun updateUIWithRecipes(recipeList: List<Recipe>) {
        if (recipeList.isEmpty()) {
            noRecipesTextView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            noRecipesTextView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter.updateRecipes(recipeList)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}