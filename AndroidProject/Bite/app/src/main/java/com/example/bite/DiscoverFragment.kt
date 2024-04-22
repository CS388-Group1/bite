package com.example.bite

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bite.models.Recipe
import com.example.bite.network.SpoonacularRepository
import com.facebook.shimmer.ShimmerFrameLayout
import com.tapadoo.alerter.Alerter
import kotlinx.coroutines.launch

class DiscoverFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecipeAdapter
    private val spoonacularRepository = SpoonacularRepository()

    private var isLoading = false
    private var currentPage = 0
    private val pageSize = 10
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_discover, container, false)
        val container = view.findViewById(R.id.shimmer_layout_discover) as ShimmerFrameLayout;
        container.startShimmer()
        setupRecyclerView(view)
        setupScrollListener()
        fetchRecipesPaginated()

//        val backButton = view.findViewById<ImageButton>(R.id.exit)
//        backButton.setOnClickListener {
//            requireActivity().onBackPressed()
//        }

        return view
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.discoverRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = RecipeAdapter(mutableListOf()) { recipe ->
            val intent = Intent(context, RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE_ID", recipe.id)
            startActivity(intent)
        }
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
            }
        }
    }

    private fun setupScrollListener() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                if (!isLoading && totalItemCount <= (lastVisibleItemPosition + 3)) {
//                    Log.d("DiscoverFragment", "Loading more items")
                    fetchRecipesPaginated()
                }

            }

        })
    }
    private fun fetchRecipesPaginated() {
        if (isLoading) return
        isLoading = true
        lifecycleScope.launch {
            try {
                val newRecipes = spoonacularRepository.getDiscoverRecipes(pageSize)
                adapter.updateRecipes(newRecipes)
                currentPage++
            } catch (e: Exception) {
                activity?.let {
                    Alerter.create(it)
                        .setTitle("Bite: Error")
                        .setText("Failed to fetch recipes: ${e.message}")
                        .setBackgroundColorRes(com.example.bite.R.color.red)
                        .setDuration(5000)
                        .show()
                }
            } finally {
                isLoading = false
                val container = view?.findViewById(R.id.shimmer_layout_discover) as ShimmerFrameLayout;
                container.stopShimmer()
                container.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }
    }
}