package com.example.bite

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bite.daos.CustomRecipeDao
import com.example.bite.models.CustomRecipeViewModel
import com.example.bite.models.Recipe
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
                    val customRecipes = customRecipeDao.getCustomRecipesWithIngredientsByUserId(userId)
                    val recipeList = customRecipes.map { customRecipeWithIngredients ->
                        Recipe(
                            id = customRecipeWithIngredients.recipe.recipeId.toString(), // Set the ID as a string
                            userId = userId,
                            title = customRecipeWithIngredients.recipe.name,
                            image = customRecipeWithIngredients.recipe.image,
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
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MyRecipesActivity, "Error fetching custom recipes: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }



    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}