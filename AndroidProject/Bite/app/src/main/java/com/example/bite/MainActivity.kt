package com.example.bite

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.bite.network.IngredientRepository
import com.example.bite.network.SpoonacularRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: RecipeAdapter
    private val repository = SpoonacularRepository()
    private lateinit var fabCenterPlus: FloatingActionButton
    private lateinit var fabScanFood: FloatingActionButton
    private lateinit var fabCreateRecipe: FloatingActionButton
    private var areFabsVisible = false

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var homeFragment: HomeFragment
    private lateinit var discoverFragment: DiscoverFragment
    private lateinit var favoritesFragment: FavoritesFragment

    private val ingredientRepository: IngredientRepository by lazy {
        val database = AppDatabase.getInstance(applicationContext)
        val ingredientDao = database.ingredientDao()
        IngredientRepository(ingredientDao, applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fabCenterPlus = findViewById(R.id.fab_center_plus)
        fabScanFood = findViewById(R.id.fab_scan_food)
        fabCreateRecipe = findViewById(R.id.fab_create_recipe)

        fabCenterPlus.setOnClickListener {
            if (!areFabsVisible) {
                fabScanFood.show()
                fabCreateRecipe.show()
                areFabsVisible = true
            } else {
                fabScanFood.hide()
                fabCreateRecipe.hide()
                areFabsVisible = false
            }
        }

        homeFragment = HomeFragment()
        discoverFragment = DiscoverFragment()
        favoritesFragment = FavoritesFragment()

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.background = null
        loadFragment(HomeFragment())

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    loadFragment(homeFragment)
                    true
                }
                R.id.navigation_search -> {
                    val intent = Intent(this, SearchActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_discover -> {
                    loadFragment(discoverFragment)
                    true
                }
                R.id.navigation_favorites -> {
                    loadFragment(favoritesFragment)
                    true
                }
                else -> false
            }
        }

        // Handle clicks for FABs
        fabScanFood.setOnClickListener {
            // Handle "Scan Food" action
        }

        fabCreateRecipe.setOnClickListener {
            // Handle "Create Recipe" action
        }

        lifecycleScope.launch {
            ingredientRepository.loadCommonIngredients()
        }
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

    private fun searchRecipes(query: String) {
        lifecycleScope.launch {
            // Load recipes and update the adapter
            val recipes = repository.searchRecipesByIngredients(query)
            adapter.updateRecipes(recipes)
        }
    }
}