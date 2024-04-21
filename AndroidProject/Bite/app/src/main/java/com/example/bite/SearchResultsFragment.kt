package com.example.bite

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bite.network.SpoonacularRepository
import com.facebook.shimmer.ShimmerFrameLayout
import com.tapadoo.alerter.Alerter
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
        private const val ARG_QUERY = "query"
        private const val ARG_IS_RECIPE_NAME = "isRecipeName"

        fun newInstance(query: String, isRecipeName: Boolean): SearchResultsFragment {
            val fragment = SearchResultsFragment()
            val args = Bundle()
            args.putString(ARG_QUERY, query)
            args.putBoolean(ARG_IS_RECIPE_NAME, isRecipeName)
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
        val container = view?.findViewById(R.id.shimmer_layout_recipe_list) as ShimmerFrameLayout;
        container.startShimmer()
        recyclerView = view.findViewById(R.id.recipeRecyclerView)
        recipeAdapter = RecipeAdapter(emptyList()) { recipe ->
            val intent = Intent(requireContext(), RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE_ID", recipe.id)
            startActivity(intent)
        }
        recyclerView.adapter = recipeAdapter
        spoonacularRepository = SpoonacularRepository()
        recyclerView.layoutManager = GridLayoutManager(context, 1)

        recipeAdapter.onFavoriteClicked = { recipe ->
            if(recipeAdapter.onFavoriteClick(recipe)){
                activity?.let {
                    Alerter.create(it)
                        .setTitle("Bite Favorites")
                        .setText("Item added to Favorites")
                        .setBackgroundColorRes(R.color.green)
                        .setDuration(5000)
                        .show()
                }
            }else{
                activity?.let {
                    Alerter.create(it)
                        .setTitle("Bite Favorites")
                        .setText("Item removed from Favorites")
                        .setBackgroundColorRes(R.color.green)
                        .setDuration(5000)
                        .show()
                }
            }
        }

        recyclerView.addOnScrollListener(InfiniteScrollListener(
            layoutManager = recyclerView.layoutManager as GridLayoutManager,
            loading = { loading },
            setLoading = { isLoading -> loading = isLoading },
            pageSize = pageSize,
            loadMore = {
                if (currentIngredients.isNotEmpty()) {
                    searchRecipesByIngredients(currentIngredients, currentOffset)
                }
            }
        ))


        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val query = arguments?.getString(ARG_QUERY)
        val isRecipeName = arguments?.getBoolean(ARG_IS_RECIPE_NAME, false) ?: false

        if (isRecipeName) {
            Log.d("SearchResultsFragment", "Searching by recipe name")
            query?.let {
                searchRecipesByName(it)
            }
        } else {
            Log.d("SearchResultsFragment", "Searching by ingredients")
            query?.let {
                currentIngredients = it
                searchRecipesByIngredients(it, currentOffset)
            }
        }
    }

    private fun searchRecipesByIngredients(ingredients: String, offset: Int) {
        if (loading) return
        loading = true
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val recipes = SpoonacularRepository().searchRecipesByIngredients(ingredients, pageSize, offset)
                val container = view?.findViewById(R.id.shimmer_layout_recipe_list) as ShimmerFrameLayout;
                container.stopShimmer()
                container.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                if (recipes.isNotEmpty()) {
                    if (offset == 0) {
                        recipeAdapter.updateRecipes(recipes)
                    } else {
                        recipeAdapter.addRecipes(recipes)
                    }
                    currentOffset += recipes.size // Ensure to update offset only when data is fetched
                } else {
                    // Handle case when no more data is available
                    activity?.let {
                        Alerter.create(it)
                            .setTitle("Bite: Error")
                            .setText("No more recipes found")
                            .setBackgroundColorRes(com.example.bite.R.color.red)
                            .setDuration(5000)
                            .show()
                    }
                }
                loading = false
            } catch (e: Exception) {
                loading = false
                activity?.let {
                    Alerter.create(it)
                        .setTitle("Bite: Error")
                        .setText("Error: ${e.message}")
                        .setBackgroundColorRes(com.example.bite.R.color.red)
                        .setDuration(5000)
                        .show()
                }
            }
        }
    }

    private fun searchRecipesByName(query: String) {
        Log.d("SearchResultsFragment", "Searching for recipes by name: $query")
        if (loading) return
        loading = true
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val recipes = SpoonacularRepository().searchRecipeByName(query)
                Log.d("SearchResultsFragment", "Found ${recipes.size} recipes")
                val container = view?.findViewById(R.id.shimmer_layout_recipe_list) as ShimmerFrameLayout
                container.stopShimmer()
                container.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                if (recipes.isNotEmpty()) {
                    recipeAdapter.updateRecipes(recipes)
                } else {
                    // Handle case when no recipes are found
                    activity?.let {
                        Alerter.create(it)
                            .setTitle("Bite: Error")
                            .setText("No recipes found")
                            .setBackgroundColorRes(com.example.bite.R.color.red)
                            .setDuration(5000)
                            .show()
                    }
                }
                loading = false
            } catch (e: Exception) {
                Log.e("SearchResultsFragment", "Error searching for recipes by name", e)
                loading = false
                activity?.let {
                    Alerter.create(it)
                        .setTitle("Bite: Error")
                        .setText("Error: ${e.message}")
                        .setBackgroundColorRes(com.example.bite.R.color.red)
                        .setDuration(5000)
                        .show()
                }
            }
        }
    }

}