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
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bite.network.SpoonacularRepository
import com.facebook.shimmer.ShimmerFrameLayout
import com.tapadoo.alerter.Alerter
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeFragment : Fragment() {
    private lateinit var spoonacularRepository: SpoonacularRepository
    private lateinit var recipesRv: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var rotdImageView: ImageView
    private lateinit var rotdRecipe: View
    private lateinit var rotdTitleTextView: TextView
    private lateinit var seeAllButton: Button
    private lateinit var preferencesButton: ImageView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val container = view?.findViewById(R.id.shimmer_layout_home) as ShimmerFrameLayout;
        container.startShimmer()
        spoonacularRepository = SpoonacularRepository()
        recipesRv = view.findViewById(R.id.recipeRecyclerView)
        recipesRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recipeAdapter = RecipeAdapter(emptyList()) { recipe ->
            val intent = Intent(requireContext(), RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE_ID", recipe.id)
            startActivity(intent)
        }
        recipesRv.adapter = recipeAdapter
        recipesRv.visibility = View.VISIBLE
        recipeAdapter.onFavoriteClicked = { recipe ->
            if(recipeAdapter.onFavoriteClick(recipe)){
                activity?.let {
                    Alerter.create(it)
                        .setTitle("Bite Favorites")
                        .setText("Item added to Favorites")
                        .setBackgroundColorRes(R.color.green)
                        .setDuration(10000)
                        .show()
                }
            }else{
                activity?.let {
                    Alerter.create(it)
                        .setTitle("Bite Favorites")
                        .setText("Item removed from Favorites")
                        .setBackgroundColorRes(R.color.green)
                        .setDuration(10000)
                        .show()
                }
            }
        }

        val snapHelper = PagerSnapHelper() // or LinearSnapHelper()
        snapHelper.attachToRecyclerView(recipesRv)

        rotdImageView = view.findViewById(R.id.imageViewRecipe)
        rotdTitleTextView = view.findViewById(R.id.textViewRecipeName)
        val rotdCookingTimeTextView: TextView = view.findViewById(R.id.textViewCookingTime)

        view.findViewById<View>(R.id.rotdRecipe).setOnClickListener {
            val recipeId = rotdImageView.tag as? String
            val intent = Intent(requireContext(), RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE_ID", recipeId)
            startActivity(intent)
        }

        seeAllButton = view.findViewById(R.id.seeAllButton)
        preferencesButton = view.findViewById(R.id.preferencesButton)

        preferencesButton.setOnClickListener {
            val intent = Intent(requireContext(), PreferencesActivity::class.java)
            requireContext().startActivity(intent)
        }

        // Fetch random recipe asynchronously
        fetchRandomRecipe()

        rotdImageView.setOnClickListener{
            val recipeId = rotdImageView.tag as? String
            val intent = Intent(requireContext(), RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE_ID", recipeId)
            startActivity(intent)
        }
        seeAllButton.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()?.apply {
                replace(R.id.fragment_container, DiscoverFragment())
                addToBackStack(null)
                commit()
            }
        }
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
                Glide.with(this@HomeFragment).load(recipe.image).centerCrop().into(rotdImageView)
                rotdTitleTextView.text = recipe.title

                // Set the cooking time to the TextView
                val cookingTime = recipe.cookingTime
                val rotdCookingTimeTextView: TextView = view?.findViewById(R.id.textViewCookingTime) ?: return@launch
                rotdCookingTimeTextView.text = if (cookingTime > 0) {
                    "$cookingTime min"
                } else {
                    ""
                }

                rotdImageView.tag = recipe.id
            } catch (e: Exception) {
                Log.e("HomeFragment", "Failed to fetch random recipe: ${e.message}")
                activity?.let {
                    Alerter.create(it)
                        .setTitle("Bite: Error")
                        .setText("Failed to fetch random recipe: ${e.message}")
                        .setBackgroundColorRes(com.example.bite.R.color.red)
                        .setDuration(10000)
                        .show()
                }
            }
        }
    }

    private fun fetchTrendingRecipes() {
        lifecycleScope.launch {
            try {
                val recipes = spoonacularRepository.getTrendingRecipes()
                val container = view?.findViewById(R.id.shimmer_layout_home) as ShimmerFrameLayout;
                container.stopShimmer()
                container.visibility = View.GONE
                recipesRv.visibility = View.VISIBLE
                recipeAdapter.updateRecipes(recipes)
            } catch (e: Exception) {
                Log.e("HomeFragment", "Failed to fetch trending recipes: ${e.message}")
                activity?.let {
                    Alerter.create(it)
                        .setTitle("Bite: Error")
                        .setText("Failed to fetch trending recipes: ${e.message}")
                        .setBackgroundColorRes(com.example.bite.R.color.red)
                        .setDuration(10000)
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
        return (number % 100000).toString()
    }
}