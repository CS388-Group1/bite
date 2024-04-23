package com.example.bite

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.bite.models.Recipe
import com.example.bite.models.RecipeLocalData
import com.facebook.shimmer.ShimmerFrameLayout
import com.tapadoo.alerter.Alerter
import kotlinx.coroutines.launch


class FavoritesFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecipeAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var loading = false
    private var currentOffset = 0
    private val pageSize = 10

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        Log.d("FavoritesFragment", "onCreateView called")
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)
        val container = view.findViewById(R.id.shimmer_layout_favorite) as ShimmerFrameLayout;
        container.startShimmer()
        setupRecyclerView(view)

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            refreshFavorites()
        }

        // Reset the current offset every time the view is created
        currentOffset = 0

//        val backButton = view.findViewById<ImageButton>(R.id.exit)
//        backButton.setOnClickListener {
//            requireActivity().onBackPressed()
//        }

        retrieveFavorite(currentOffset, pageSize)
        return view
    }

    private fun setupRecyclerView(view: View) {
        Log.d("FavoritesFragment", "Setting up RecyclerView")
        recyclerView = view.findViewById(R.id.favoritesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = RecipeAdapter(mutableListOf()) { recipe ->
            val intent = Intent(requireActivity(), RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE_ID", recipe.id.toString())
            if (recipe?.sourceName == "") {
                intent.putExtra("UserID", recipe.userId.toString())
                intent.putExtra("RecipeName", recipe.title.toString())
                Log.v("Custom Recipe Clicked", "user id: ${recipe.userId.toString()}")
            }
            startActivity(intent)
        }


        recyclerView.adapter = adapter

        recyclerView.adapter = adapter
        adapter.onFavoriteClicked = { recipe ->
            if(adapter.onFavoriteClick(recipe)){
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
                refreshFavorites()
            }

        }
        recyclerView.addOnScrollListener(
            InfiniteScrollListener(
                layoutManager = recyclerView.layoutManager as LinearLayoutManager,
                loading = { loading },
                setLoading = { isLoading ->
                    loading = isLoading
                },
                pageSize = pageSize,
                loadMore = {
                    retrieveFavorite(currentOffset, pageSize)
                })
        )
    }

    private fun retrieveFavorite(offset: Int, limit: Int) {
        if (loading) {
            Log.d("FavoritesFragment", "Already loading, exiting retrieveFavorite")
            return
        }
        Log.d("FavoritesFragment", "Loading favorites, offset: $offset, limit: $limit")
        loading = true
        lifecycleScope.launch {
            val favoriteRepository = RecipeLocalData(
                AppDatabase.getInstance(requireContext()).recipeDao(),
                requireContext()
            )
            val recipes = favoriteRepository.getFavoriteRecipes(limit, offset)
            Log.d("FavoritesFragment", "Recipes fetched: ${recipes.size}")
            if (recipes.isNotEmpty()) {
                adapter.addRecipes(recipes)
                currentOffset += recipes.size
                recyclerView.visibility = View.VISIBLE
                view?.findViewById<TextView>(R.id.noFavoritesTextView)?.visibility = View.GONE
            } else {
                if (offset == 0) {
                    recyclerView.visibility = View.GONE
                    view?.findViewById<TextView>(R.id.noFavoritesTextView)?.visibility = View.VISIBLE
                }
            }
            loading = false
            val container = view?.findViewById<ShimmerFrameLayout>(R.id.shimmer_layout_favorite)
            container?.stopShimmer()
            container?.visibility = View.GONE
        }
    }

    private fun refreshFavorites() {
        currentOffset = 0
        adapter.clearRecipes()
        retrieveFavorite(currentOffset, pageSize)
        swipeRefreshLayout.isRefreshing = false
    }

}
