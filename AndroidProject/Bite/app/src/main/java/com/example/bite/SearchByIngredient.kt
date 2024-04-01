package com.example.bite

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bite.models.IngredientResponse
import com.example.bite.network.SpoonacularRepository
import kotlinx.coroutines.launch
class SearchByIngredient : AppCompatActivity() {
    private lateinit var spoonacularRepository: SpoonacularRepository
    private lateinit var recyclerView: RecyclerView
    private lateinit var selectedRecyclerView: RecyclerView
    private lateinit var ingredientAdapter: IngredientAdapter
    private lateinit var selectedAdapter: IngredientAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ingredient_search)

        // TODO: Get IDs
        val searchView = findViewById<SearchView>(R.id.SearchInput)
        val exitButton = findViewById<ImageButton>(R.id.exit)
        recyclerView = findViewById(R.id.recyclerViewCommonIngredients)
        selectedRecyclerView = findViewById(R.id.recyclerViewSelectedIngredients)

        ingredientAdapter = IngredientAdapter(emptyList())
        selectedAdapter = IngredientAdapter(emptyList())

        recyclerView.layoutManager = GridLayoutManager(this, 4)
        recyclerView.adapter = ingredientAdapter

        spoonacularRepository = SpoonacularRepository()

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
            }
        })

        // TODO: Use Button To Exit Detail
        exitButton.setOnClickListener {
            finish()
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