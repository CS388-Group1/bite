package com.example.bite

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import android.widget.FrameLayout
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

class SearchActivity : AppCompatActivity() {
    private lateinit var searchByIngredientFragment: SearchByIngredientFragment
    private lateinit var searchByRecipeFragment: SearchByRecipeFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val searchDropdownButton = findViewById<ImageButton>(R.id.SearchByDropdown)
        val fragmentContainer = findViewById<FrameLayout>(R.id.fragment_container)
        val exitButton = findViewById<ImageButton>(R.id.exit)

        searchByIngredientFragment = SearchByIngredientFragment()
        searchByRecipeFragment = SearchByRecipeFragment()
        loadFragment(searchByIngredientFragment)

        searchDropdownButton.setOnClickListener {
            showSearchOptionDialog()
        }

        exitButton.setOnClickListener {
            finish()
        }
    }

    private fun showSearchOptionDialog() {
        val options = arrayOf("Search By Ingredient", "Search By Recipe Name")
        AlertDialog.Builder(this)
            .setTitle("Select Search Option")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        loadFragment(searchByIngredientFragment)
                    }
                    1 -> {
                        loadFragment(searchByRecipeFragment)
                    }
                }
            }
            .show()
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        // sliding animations based on the fragment being loaded
        when (fragment) {
            is HomeFragment -> {
                transaction.setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
            }
            is DiscoverFragment -> {
                transaction.setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
            }
            is FavoritesFragment -> {
                transaction.setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
            }
        }
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun updateTitle(title: String, searchInput: String) {
        val titleTextView = findViewById<TextView>(R.id.titleSearchByIngredients)
        val searchInputSearchView = findViewById<SearchView>(R.id.SearchInput)
        titleTextView.text = title
        searchInputSearchView.queryHint = searchInput
    }
}