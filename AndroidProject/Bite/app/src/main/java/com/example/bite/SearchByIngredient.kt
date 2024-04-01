package com.example.bite

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bite.models.IngredientResponse
import com.example.bite.network.SpoonacularRepository
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.launch

class SearchByIngredient : AppCompatActivity() {
    private lateinit var spoonacularRepository: SpoonacularRepository
    private lateinit var recyclerView: RecyclerView
    private lateinit var selectedRecyclerView: RecyclerView
    private lateinit var ingredientAdapter: IngredientAdapter
    private lateinit var selectedAdapter: IngredientAdapter
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ingredient_search)

        // TODO: Get IDs
        searchView = findViewById(R.id.SearchInput)
        val exitButton = findViewById<ImageButton>(R.id.exit)
        recyclerView = findViewById(R.id.recyclerViewCommonIngredients)
        selectedRecyclerView = findViewById(R.id.recyclerViewSelectedIngredients)
        val submitSearchButton = findViewById<ExtendedFloatingActionButton>(R.id.SubmitSearchButton)

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

        var selected: List<IngredientResponse> = emptyList()
        selectedRecyclerView.layoutManager = GridLayoutManager(this, 4)
        selectedRecyclerView.adapter = selectedAdapter

        ingredientAdapter.setOnClickListener(object : IngredientAdapter.OnClickListener {
            override fun onClick(position: Int, ingredient: IngredientResponse) {
                selected = selected + ingredient
                selectedAdapter.updateIngredients(selected)
                if (selectedIngredientsLayout.visibility == View.GONE) {
                    selectedIngredientsLayout.visibility = View.VISIBLE
                }
            }
        })

        // TODO: Use Button To Exit Detail
        exitButton.setOnClickListener {
            finish()
        }

        submitSearchButton.setOnClickListener {
            val query = searchView.query.toString()
            searchIngredientByName(query)
        }
    }

    private fun searchIngredientByName(query: String) {
        lifecycleScope.launch {
            try {
                val ingredients = spoonacularRepository.searchIngredientByName(query)
                ingredientAdapter.updateIngredients(ingredients.results)
            } catch (e: Exception) {
                Toast.makeText(this@SearchByIngredient, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}