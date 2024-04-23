package com.example.bite

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.bite.daos.UserPreferencesDao
import com.example.bite.models.UserPreferences
import com.example.bite.network.SpoonacularRepository
import com.facebook.shimmer.ShimmerFrameLayout
import com.tapadoo.alerter.Alerter
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeFragment : Fragment() {
    private lateinit var spoonacularRepository: SpoonacularRepository
    private lateinit var discoverRecyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var seeAllButton: Button
    private lateinit var preferencesButton: ImageView
    private lateinit var rotdRecyclerView: RecyclerView
    private lateinit var rotdAdapter: RecipeAdapter
    private lateinit var rotdShimmerLayout: ShimmerFrameLayout
    private lateinit var userPreferencesDao: UserPreferencesDao
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val appDatabase = AppDatabase.getInstance(requireContext())
        userPreferencesDao = appDatabase.userPreferencesDao()

        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val container = view?.findViewById(R.id.shimmer_layout_home) as ShimmerFrameLayout
        container.startShimmer()

        spoonacularRepository = SpoonacularRepository()
        discoverRecyclerView = view.findViewById(R.id.trendingRecyclerView)
        discoverRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recipeAdapter = RecipeAdapter(emptyList()) { recipe ->
            val intent = Intent(requireContext(), RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE_ID", recipe.id)
            startActivity(intent)
        }
        discoverRecyclerView.adapter = recipeAdapter
        discoverRecyclerView.visibility = View.VISIBLE
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
        rotdRecyclerView = view.findViewById(R.id.rotdRecyclerView)
        rotdRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rotdAdapter = RecipeAdapter(emptyList()) { recipe ->
            val intent = Intent(requireContext(), RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE_ID", recipe.id)
            startActivity(intent)
        }
        rotdRecyclerView.adapter = rotdAdapter

        rotdAdapter.onFavoriteClicked = { recipe ->
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

        rotdShimmerLayout = view.findViewById(R.id.shimmer_layout_rotd)
        rotdShimmerLayout.startShimmer()

        val snapHelper = PagerSnapHelper() // or LinearSnapHelper()
        snapHelper.attachToRecyclerView(discoverRecyclerView)

        seeAllButton = view.findViewById(R.id.seeAllButton)
        preferencesButton = view.findViewById(R.id.preferencesButton)

        preferencesButton.setOnClickListener {
            val intent = Intent(requireContext(), PreferencesActivity::class.java)
            requireContext().startActivity(intent)
        }

        // Fetch random recipe asynchronously
        fetchRandomRecipe()

        // Fetch trending recipes asynchronously
        fetchTrendingRecipes()

        seeAllButton.setOnClickListener {
            val discoverFragment = DiscoverFragment()
            val fragmentManager = requireActivity().supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()

            fragmentTransaction.setCustomAnimations(
                R.anim.slide_in_right, // Enter animation
                R.anim.slide_out_left, // Exit animation
                R.anim.slide_in_left, // Pop enter animation
                R.anim.slide_out_right // Pop exit animation
            )

            fragmentTransaction.replace(R.id.fragment_container, discoverFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchRandomRecipe() {
        lifecycleScope.launch {
            try {
                val recipe = spoonacularRepository.getRandomRecipe(getDailyNumber())
                rotdShimmerLayout.stopShimmer()
                rotdShimmerLayout.visibility = View.GONE
                rotdRecyclerView.visibility = View.VISIBLE
                rotdAdapter.updateRecipes(listOf(recipe))
            } catch (e: Exception) {
                Log.e("HomeFragment", "Failed to fetch random recipe: ${e.message}")
                activity?.let {
                    Alerter.create(it)
                        .setTitle("Bite: Error")
                        .setText("Failed to fetch recipe of the day: ${e.message}")
                        .setBackgroundColorRes(R.color.red)
                        .setDuration(5000)
                        .show()
                }
            }
        }
    }

    private fun fetchTrendingRecipes() {
        lifecycleScope.launch {
            try {
                val userPreferences = userPreferencesDao.getUserPreferences() ?: UserPreferences()
                val recipes = spoonacularRepository.getTrendingRecipes(userPreferences)
                val shimmerContainer = view?.findViewById(R.id.shimmer_layout_home) as ShimmerFrameLayout
                shimmerContainer.stopShimmer()
                shimmerContainer.visibility = View.GONE
//                discoverRecyclerView.visibility = View.VISIBLE
                recipeAdapter.updateRecipes(recipes)
            } catch (e: Exception) {
                Log.e("HomeFragment", "Failed to fetch trending recipes: ${e.message}")
                activity?.let {
                    Alerter.create(it)
                        .setTitle("Bite: Error")
                        .setText("Failed to fetch trending recipes: ${e.message}")
                        .setBackgroundColorRes(R.color.red)
                        .setDuration(5000)
                        .show()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDailyNumber(): String {
        val currentDate = LocalDate.now()
        val form = DateTimeFormatter.ofPattern("yyyyMMdd")
        val number = currentDate.format(form).toInt()
        return (number % 10000).toString()
    }
}