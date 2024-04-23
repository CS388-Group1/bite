package com.example.bite

import androidx.recyclerview.widget.RecyclerView
import com.example.bite.models.Ingredient
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.bite.models.CustomIngredient
import java.text.DecimalFormat

class CustomRecipeIngredientAdapter(private val ingredients: List<CustomIngredient>) : RecyclerView.Adapter<CustomRecipeIngredientAdapter.ViewHolder>() {

    private val decimalFormat = DecimalFormat("#.##")

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.ingredientImg)
        private val nameTextView: TextView = itemView.findViewById(R.id.ingredientName)
        private val amountTextView: TextView = itemView.findViewById(R.id.ingredientAmount)

        fun bind(ingredient: CustomIngredient) {
            Glide.with(itemView.context)
                .load(R.drawable.cookie_transparent)
                .into(imageView)

            nameTextView.text = ingredient.name.split(" ").joinToString(" ") { it.capitalize() }

            val formattedAmount = decimalFormat.format(ingredient.amount)
            amountTextView.text = "$formattedAmount ${ingredient.unit}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_ingredient, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ingredient = ingredients[position]
        holder.bind(ingredient)
    }

    override fun getItemCount(): Int {
        return ingredients.size
    }
}