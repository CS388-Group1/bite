package com.example.bite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bite.models.Recipe

class RecipeAdapter(private var recipes: List<Recipe>, private val onRecipeClicked: (Recipe) -> Unit) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageViewRecipe)
        val nameView: TextView = itemView.findViewById(R.id.textViewRecipeName)
        val descriptionView: TextView = itemView.findViewById(R.id.textViewDescription)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        with(holder) {
            nameView.text = recipe.name
            descriptionView.text = recipe.description

            Glide.with(imageView.context).load(recipe.imageUrl).into(imageView)

            itemView.setOnClickListener { onRecipeClicked(recipe) }
        }
    }

    override fun getItemCount() = recipes.size

    fun updateRecipes(newRecipes: List<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }
}
