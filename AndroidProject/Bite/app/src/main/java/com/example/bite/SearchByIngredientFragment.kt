package com.example.bite

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bite.models.Ingredient
import com.example.bite.network.IngredientRepository
import com.example.bite.network.SpoonacularRepository
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.launch
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator

class SearchByIngredientFragment : Fragment() {
    private lateinit var searchByRecipeFragment: SearchByRecipeFragment
    private lateinit var spoonacularRepository: SpoonacularRepository
    private lateinit var recyclerView: RecyclerView
    private lateinit var selectedRecyclerView: RecyclerView
    private lateinit var ingredientAdapter: IngredientAdapter
    private lateinit var selectedAdapter: IngredientAdapter
    private lateinit var searchView: SearchView
    private lateinit var ingredientRepository: IngredientRepository
    private lateinit var commonIngredientsTextView: TextView
    private lateinit var nestedScrollView: NestedScrollView
    private var selected: MutableList<Ingredient> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search_by_ingredient, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewCommonIngredients)
        selectedRecyclerView = view.findViewById(R.id.recyclerViewSelectedIngredients)
        commonIngredientsTextView = view.findViewById(R.id.CommonIngredients)

        ingredientAdapter = IngredientAdapter(emptyList())
        selectedAdapter = IngredientAdapter(emptyList())

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 4)
        recyclerView.adapter = ingredientAdapter
        recyclerView.itemAnimator = DefaultItemAnimator()

        nestedScrollView = view.findViewById(R.id.nestedScrollView)

        spoonacularRepository = SpoonacularRepository()

        val selectedIngredientsLayout =
            view.findViewById<LinearLayout>(R.id.selectedIngredientsLayout)
        selectedIngredientsLayout.visibility = View.GONE

        selectedRecyclerView.layoutManager = GridLayoutManager(requireContext(), 4)
        selectedRecyclerView.adapter = selectedAdapter

        ingredientAdapter.setOnClickListener(object : IngredientAdapter.OnClickListener {
            override fun onClick(position: Int, ingredient: Ingredient) {
                if (ingredient.id !in selected.map { it.id }) {
                    selected.add(ingredient)
                    selectedAdapter.updateIngredients(selected)
                    selectedAdapter.notifyDataSetChanged()
                    if (selectedIngredientsLayout.visibility == View.GONE) {
                        selectedIngredientsLayout.visibility = View.VISIBLE
                    }

                    commonIngredientsTextView.text = "Common Ingredients"
                    viewLifecycleOwner.lifecycleScope.launch {
                        val selectedIngredientIds = selected.map { it.id }
                        val commonIngredients =
                            ingredientRepository.getCommonIngredients().filter { ingredient ->
                                ingredient.id !in selectedIngredientIds
                            }
                        ingredientAdapter.updateIngredients(commonIngredients)
                    }

                    nestedScrollView.smoothScrollTo(0, 0)
                }
            }
        })

        ingredientRepository = IngredientRepository(
            AppDatabase.getInstance(requireContext()).ingredientDao(),
            requireContext()
        )

        viewLifecycleOwner.lifecycleScope.launch {
            val selectedIngredientIds = selected.map { it.id }
            val commonIngredients =
                ingredientRepository.getCommonIngredients().filter { ingredient ->
                    ingredient.id !in selectedIngredientIds
                }
            ingredientAdapter.updateIngredients(commonIngredients)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchView = requireActivity().findViewById(R.id.SearchInput)
        val exitButton = requireActivity().findViewById<ImageButton>(R.id.exit)
        val submitSearchButton =
            requireActivity().findViewById<ExtendedFloatingActionButton>(R.id.SubmitSearchButton)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchIngredientByName(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return true
            }
        })

        submitSearchButton.setOnClickListener {
            val query = searchView.query.toString()
            searchIngredientByName(query)
            hideKeyboard()
        }

        exitButton.setOnClickListener {
            // Handle exit button click
        }
    }

    private fun hideKeyboard() {
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
    }

    private fun searchIngredientByName(query: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val ingredientListResponse = spoonacularRepository.searchIngredientByName(query)
                val selectedIngredientIds = selected.map { it.id }
                val filteredIngredients =
                    ingredientListResponse.results.filter { ingredientResponse ->
                        ingredientResponse.id !in selectedIngredientIds
                    }.map { ingredientResponse ->
                        val imageUrl =
                            "https://spoonacular.com/cdn/ingredients_100x100/${ingredientResponse.image}"
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
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as SearchActivity).updateTitle("Search ingredient")
    }
}
