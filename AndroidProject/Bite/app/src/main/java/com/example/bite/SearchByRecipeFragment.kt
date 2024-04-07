package com.example.bite

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bite.models.Recipe
import com.example.bite.network.SpoonacularRepository
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchByRecipeFragment : Fragment() {
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var searchView: SearchView
    private lateinit var searchButton: ExtendedFloatingActionButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_search_by_recipe, container, false)
        searchButton = view.findViewById(R.id.SubmitSearchButton)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recipeAdapter = RecipeAdapter(emptyList()) { recipe ->
            val intent = Intent(requireContext(), RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE_ID", recipe.id)
            startActivity(intent)
        }

        searchView = requireActivity().findViewById(R.id.SearchInput)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    navigateToSearchResults(it)
                    hideKeyboard()
                    searchView.clearFocus()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    hideSearchButton()
                } else if (searchButton.visibility != View.VISIBLE) {
                    showSearchButton()
                }
                return true
            }
        })

        searchButton.setOnClickListener {
            searchView.query.toString().let {
                if (it.isNotEmpty()) {
                    navigateToSearchResults(it)
                }
            }
            hideKeyboard()
            searchView.clearFocus()
        }
    }

    private fun navigateToSearchResults(query: String) {
        val searchResultsFragment = SearchResultsFragment.newInstance(query)
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(R.id.fragment_container, searchResultsFragment)
            .addToBackStack(null)
            .commit()
        // Removed hideSearchButton call here
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as SearchActivity).updateTitle("Search Recipes", "Search recipes")
    }

    private fun showSearchButton() {
        searchButton.apply {
            visibility = View.VISIBLE
            alpha = 0f
            translationY = height.toFloat()
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }
    }

    private fun hideSearchButton() {
        searchButton.animate()
            .alpha(0f)
            .translationY(searchButton.height.toFloat())
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                searchButton.visibility = View.GONE
            }
            .start()
    }

    private fun hideKeyboard() {
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(searchView.windowToken, 0)
    }
}