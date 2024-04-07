package com.example.bite

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.bite.network.SpoonacularRepository
import kotlinx.coroutines.launch

class SearchResultsFragment : Fragment() {
    private lateinit var spoonacularRepository: SpoonacularRepository
    private lateinit var recyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter

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
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ingredients = arguments?.getString(ARG_INGREDIENTS) ?: ""
        searchRecipesByIngredients(ingredients)
    }

    private fun searchRecipesByIngredients(ingredients: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val recipes = spoonacularRepository.searchRecipesByIngredients(ingredients)
                recipeAdapter.updateRecipes(recipes)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}