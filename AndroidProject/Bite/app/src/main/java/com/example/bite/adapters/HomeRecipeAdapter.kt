package com.example.bite.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bite.R
import com.example.bite.models.Recipe

class HomeRecipeAdapter(private var recipes: List<Recipe>, private val onRecipeClicked: (Recipe) -> Unit) :
    RecyclerView.Adapter<HomeRecipeAdapter.HomeRecipeViewHolder>() {

    inner class HomeRecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recipeTitle: TextView = itemView.findViewById(R.id.recipeTitleTextView)
        val recipeAuthor: TextView = itemView.findViewById(R.id.authorTextView)
        val recipeImage: ImageView = itemView.findViewById(R.id.recipeImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeRecipeViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val recipeView = inflater.inflate(R.layout.item_home_recipe, parent, false)
        return HomeRecipeViewHolder(recipeView)
    }

    override fun getItemCount(): Int {
        return recipes.size
    }

    override fun onBindViewHolder(holder: HomeRecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        with(holder) {
            recipeTitle.text = recipe.name
            recipeAuthor.text = recipe.author
            Glide.with(recipeImage.context).load(recipe.imageUrl).into(recipeImage)
            itemView.setOnClickListener { onRecipeClicked(recipe) }
        }
    }
    fun updateRecipes(newRecipes: List<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }
}
