package com.example.bite

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bite.models.Recipe
import com.example.bite.models.RecipeLocalData
import kotlinx.coroutines.CoroutineStart
import com.tapadoo.alerter.Alerter
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


class RecipeAdapter(private var recipes: List<Recipe>, var onRecipeClicked: (Recipe) -> Unit) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

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

    @OptIn(ExperimentalEncodingApi::class)    
    var onFavoriteClicked: ((Recipe) -> Unit)? = null
    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        with(holder) {
            nameView.text = recipe.title
            cookingTimeView.text = recipe.cookingTime.takeIf { it > 0 }?.let { "$it min" } ?: ""

            // Check if the image is a base64 string or a URL
            if (recipe.image.startsWith("data:image/")) {
                // Decode the base64 image and set it to the ImageView
                Log.d("RecipeAdapter", "Loading image from base64: ${recipe.image}")
                val imageData = recipe.image.substringAfter(",")
                val imageBytes = android.util.Base64.decode(imageData, android.util.Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                imageView.setImageBitmap(decodedImage)
            } else {
                // Load the image from the URL using Glide
                Log.d("RecipeAdapter", "Loading image from URL: ${recipe.image}")
                Glide.with(imageView.context).load(recipe.image).into(imageView)
            }

            // Check if the recipe is in favorites
            val recipeLocalData = RecipeLocalData(itemView.context.let {
                AppDatabase.getInstance(it.applicationContext).recipeDao()
            }, itemView.context.applicationContext)

            GlobalScope.launch {
                val isFavorite = recipeLocalData.isRowIsExist(recipe.id)
                recipe.isFavorite = isFavorite

                MainScope().launch {
                    buttonFavorite.isSelected = isFavorite
                }
            }

            buttonFavorite.setOnClickListener {
                recipe.isFavorite = !recipe.isFavorite
                updateFavorite(recipe, recipe.isFavorite, recipe.id)
                buttonFavorite.isSelected = recipe.isFavorite
                onFavoriteClick(recipe)
                onFavoriteClicked?.invoke(recipe)
            }


            buttonFavorite.isSelected = recipe.isFavorite
            buttonFavorite.setOnClickListener {
                recipe.isFavorite = !recipe.isFavorite
                updateFavorite(recipe, recipe.isFavorite, recipe.id)
                buttonFavorite.isSelected = recipe.isFavorite
                onFavoriteClick(recipe)
                onFavoriteClicked?.invoke(recipe)
            }

            itemView.setOnClickListener {
                onRecipeClicked(recipe)
            }
        }
    }
    fun onFavoriteClick(recipe: Recipe): Boolean {
        return recipe.isFavorite
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
