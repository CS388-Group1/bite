package com.example.bite

import android.content.Context
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class RecipeAdapter(private var recipes: List<Recipe>, private val onRecipeClicked: (Recipe) -> Unit) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageViewRecipe)
        val nameView: TextView = itemView.findViewById(R.id.textViewRecipeName)
        val descriptionView: TextView = itemView.findViewById(R.id.textViewDescription)
        val buttonFavorite: ImageButton = itemView.findViewById(R.id.favoriteButton)

        fun updateFavorite(favorite: Boolean, id: String){
            GlobalScope.launch {
                itemView.context.let {
                    AppDatabase.getInstance(it.applicationContext).recipeDao()
                }.let { RecipeLocalData(it, itemView.context.applicationContext) }
                    .updateRecipe(favorite, id)
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
            descriptionView.text = recipe.summary
            //get database
            buttonFavorite.setOnClickListener{
                //update favorite
                recipe.isFavorite = true
                updateFavorite(recipe.isFavorite,recipe.id)
                //cant find method to execute query within adapter

            }

            Glide.with(imageView.context).load(recipe.image).into(imageView)

            itemView.setOnClickListener { onRecipeClicked(recipe) }
        }
    }

    override fun getItemCount() = recipes.size

    fun updateRecipes(newRecipes: List<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }
}
