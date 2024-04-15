package com.example.bite

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bite.models.Recipe
import com.example.bite.models.RecipeLocalData
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class RecipeAdapter(private var recipes: List<Recipe>, private val onRecipeClicked: (Recipe) -> Unit) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageViewRecipe)
        val nameView: TextView = itemView.findViewById(R.id.textViewRecipeName)
        val cookingTimeView: TextView = itemView.findViewById(R.id.textViewCookingTime)
        val buttonFavorite: ImageButton = itemView.findViewById(R.id.favoriteButton)

        @OptIn(DelicateCoroutinesApi::class)
        fun updateFavorite(recipe: Recipe, favorite: Boolean, id: String){
            GlobalScope.launch {
                val exists = (itemView.context.let {
                    AppDatabase.getInstance(it.applicationContext).recipeDao()
                }.let { RecipeLocalData(it, itemView.context.applicationContext) }
                    .isRowIsExist(id))
                if (exists) {
                    (itemView.context.let {
                        AppDatabase.getInstance(it.applicationContext).recipeDao()
                    }.let { RecipeLocalData(it, itemView.context.applicationContext) }
                        .updateRecipe(favorite, id))
                } else {
                    (itemView.context.let {
                        AppDatabase.getInstance(it.applicationContext).recipeDao()
                    }.let { RecipeLocalData(it, itemView.context.applicationContext) }
                        .insertRecipe(recipe))
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        with(holder) {
            nameView.text = recipe.title
            cookingTimeView.text = recipe.cookingTime.takeIf { it > 0 }?.let { "$it min" } ?: ""
            Glide.with(imageView.context).load(recipe.image).into(imageView)

            buttonFavorite.isSelected = recipe.isFavorite
            buttonFavorite.setOnClickListener {
                recipe.isFavorite = !recipe.isFavorite
                updateFavorite(recipe, recipe.isFavorite, recipe.id)
                buttonFavorite.isSelected = recipe.isFavorite
                if (recipe.isFavorite) {
                    Toast.makeText(itemView.context, "Item Added to Favorites", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(itemView.context, "Item Removed from Favorites", Toast.LENGTH_SHORT).show()
                }
            }

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, RecipeDetailActivity::class.java)
                intent.putExtra("RECIPE_ID", recipe.id)
                itemView.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = recipes.size
    fun addRecipes(newRecipes: List<Recipe>) {
        val oldSize = recipes.size
        recipes += newRecipes // Append new recipes to the existing list
        notifyItemRangeInserted(oldSize, newRecipes.size)
    }
    fun updateRecipes(newRecipes: List<Recipe>) {
        val oldSize = recipes.size
        recipes = recipes + newRecipes
        notifyItemRangeInserted(oldSize, newRecipes.size)
    }
}
