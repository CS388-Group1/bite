package com.example.bite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bite.models.Recipe
import com.example.bite.network.SpoonacularRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchByRecipeFragment : Fragment() {
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var recipeRecyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private val spoonacularRepository = SpoonacularRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search_by_recipe, container, false)
        recipeRecyclerView = view.findViewById(R.id.recipeRecyclerView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recipeRecyclerView.adapter = recipeAdapter
        recipeRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        searchView = requireActivity().findViewById(R.id.SearchInput)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchRecipes(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun searchRecipes(query: String?) {
        query?.let {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val searchResults = spoonacularRepository.searchRecipeByName(query)
                    withContext(Dispatchers.Main) {
                        recipeAdapter.updateRecipes(searchResults)
                    }
                } catch (e: Exception) {
                    // Handle exception
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as SearchActivity).updateTitle("Search Recipes", "Search recipes")
    }
}