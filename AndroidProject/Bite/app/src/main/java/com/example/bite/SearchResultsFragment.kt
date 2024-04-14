package com.example.bite

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bite.network.SpoonacularRepository
import kotlinx.coroutines.launch

class SearchResultsFragment : Fragment() {
    private lateinit var spoonacularRepository: SpoonacularRepository
    private lateinit var recyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter
    private var loading = false
    private var currentOffset = 0
    private val pageSize = 10
    private var currentIngredients = ""

    companion object {
        private const val ARG_INGREDIENTS = "ingredients"

        fun newInstance(ingredients: String): SearchResultsFragment {
            val fragment = SearchResultsFragment()
            val args = Bundle()
            args.putString(ARG_INGREDIENTS, ingredients)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recipe_list, container, false)
        recyclerView = view.findViewById(R.id.recipeRecyclerView)
        recipeAdapter = RecipeAdapter(emptyList()) { recipe ->
            val intent = Intent(requireContext(), RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE_ID", recipe.id)
            startActivity(intent)
        }
        recyclerView.adapter = recipeAdapter
        spoonacularRepository = SpoonacularRepository()
        recyclerView.layoutManager = GridLayoutManager(context, 1)

        recyclerView.addOnScrollListener(InfiniteScrollListener(
            layoutManager = recyclerView.layoutManager as GridLayoutManager,
            loading = { loading },
            setLoading = { isLoading ->
                loading = isLoading
            }
        ) {
            if (currentIngredients.isNotEmpty()) {
                searchRecipesByIngredients(currentIngredients, currentOffset)
            }
        })

        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getString(ARG_INGREDIENTS)?.let {
            currentIngredients = it
            searchRecipesByIngredients(it, currentOffset)
        }
    }

    private fun searchRecipesByIngredients(ingredients: String, offset: Int) {
        if (loading) return
        loading = true
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val recipes = SpoonacularRepository().searchRecipesByIngredients(ingredients, pageSize, offset)
                if (recipes.isNotEmpty()) {
                    if (offset == 0) {
                        recipeAdapter.updateRecipes(recipes)
                    } else {
                        recipeAdapter.addRecipes(recipes)
                    }
                    currentOffset += recipes.size // Ensure to update offset only when data is fetched
                } else {
                    // Handle case when no more data is available
                    Toast.makeText(requireContext(), "No more recipes found", Toast.LENGTH_SHORT).show()
                }
                loading = false
            } catch (e: Exception) {
                loading = false
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}