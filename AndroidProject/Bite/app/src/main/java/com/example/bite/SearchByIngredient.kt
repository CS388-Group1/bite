package com.example.bite

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bite.models.Ingredient
import com.example.bite.models.IngredientResponse
import com.example.bite.network.IngredientRepository
import com.example.bite.network.SpoonacularRepository
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.launch
import android.view.inputmethod.InputMethodManager

class SearchByIngredient : AppCompatActivity() {
    private lateinit var spoonacularRepository: SpoonacularRepository
    private lateinit var recyclerView: RecyclerView
    private lateinit var selectedRecyclerView: RecyclerView
    private lateinit var ingredientAdapter: IngredientAdapter
    private lateinit var selectedAdapter: IngredientAdapter
    private lateinit var searchView: SearchView
    private lateinit var ingredientRepository: IngredientRepository
    private lateinit var commonIngredientsTextView: TextView
    private var selected: MutableList<Ingredient> = mutableListOf()

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ingredient_search)

        // TODO: Get IDs
        searchView = findViewById(R.id.SearchInput)
        val exitButton = findViewById<ImageButton>(R.id.exit)
        recyclerView = findViewById(R.id.recyclerViewCommonIngredients)
        selectedRecyclerView = findViewById(R.id.recyclerViewSelectedIngredients)
        val submitSearchButton = findViewById<ExtendedFloatingActionButton>(R.id.SubmitSearchButton)
        commonIngredientsTextView = findViewById(R.id.CommonIngredients)

        ingredientAdapter = IngredientAdapter(emptyList())
        selectedAdapter = IngredientAdapter(emptyList())

        recyclerView.layoutManager = GridLayoutManager(this, 4)
        recyclerView.adapter = ingredientAdapter

        spoonacularRepository = SpoonacularRepository()

        val selectedIngredientsLayout = findViewById<LinearLayout>(R.id.selectedIngredientsLayout)
        selectedIngredientsLayout.visibility = View.GONE

        // TODO: Use SearchView to search ingredient list
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchIngredientByName(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                // search suggestions as the user types
                return true
            }
        })

        selectedRecyclerView.layoutManager = GridLayoutManager(this, 4)
        selectedRecyclerView.adapter = selectedAdapter

        ingredientAdapter.setOnClickListener(object : IngredientAdapter.OnClickListener {
            override fun onClick(position: Int, ingredient: Ingredient) {
                if (ingredient.id !in selected.map { it.id }) {
                    selected.add(ingredient)
                    selectedAdapter.updateIngredients(selected)
                    if (selectedIngredientsLayout.visibility == View.GONE) {
                        selectedIngredientsLayout.visibility = View.VISIBLE
                    }

                    // Change the text back to "Common Ingredients" and load common ingredients
                    commonIngredientsTextView.text = "Common Ingredients"
                    lifecycleScope.launch {
                        val selectedIngredientIds = selected.map { it.id }
                        val commonIngredients = ingredientRepository.getCommonIngredients().filter { ingredient ->
                            ingredient.id !in selectedIngredientIds
                        }
                        ingredientAdapter.updateIngredients(commonIngredients)
                    }
                }
            }
        })

        ingredientRepository = IngredientRepository(AppDatabase.getInstance(applicationContext).ingredientDao(), applicationContext)

        // TODO: Use Button To Exit Detail
        exitButton.setOnClickListener {
            finish()
        }

        submitSearchButton.setOnClickListener {
            val query = searchView.query.toString()
            searchIngredientByName(query)
            hideKeyboard() // Hide the keyboard when the submit search button is clicked
        }

        lifecycleScope.launch {
            val selectedIngredientIds = selected.map { it.id }
            val commonIngredients = ingredientRepository.getCommonIngredients().filter { ingredient ->
                ingredient.id !in selectedIngredientIds
            }
            ingredientAdapter.updateIngredients(commonIngredients)
        }
    }


    private fun searchIngredientByName(query: String) {
        lifecycleScope.launch {
            try {
                val ingredientListResponse = spoonacularRepository.searchIngredientByName(query)
                val selectedIngredientIds = selected.map { it.id }
                val filteredIngredients = ingredientListResponse.results.filter { ingredientResponse ->
                    ingredientResponse.id !in selectedIngredientIds
                }.map { ingredientResponse ->
                    val imageUrl = "https://spoonacular.com/cdn/ingredients_100x100/${ingredientResponse.image}"
                    Ingredient(
                        id = ingredientResponse.id,
                        name = ingredientResponse.name,
                        image = imageUrl,
                        amount = 0.0,
                        unit = "",
                        isCommon = false
                    )
                }
                ingredientAdapter.updateIngredients(filteredIngredients)
                commonIngredientsTextView.text = "Search Results"
                hideKeyboard()
            } catch (e: Exception) {
                Toast.makeText(this@SearchByIngredient, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}