package com.example.bite

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bite.models.IngredientResponse
import com.google.android.material.imageview.ShapeableImageView

class IngredientAdapter(private var ingredients: List<IngredientResponse>) :
    RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder>() {
    private var onClickListener: OnClickListener? = null
    inner class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val imageView: ShapeableImageView = itemView.findViewById(R.id.imageViewIngredient)
        val nameView: TextView = itemView.findViewById(R.id.textViewIngredientName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_ingredient, parent, false)
        return IngredientViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val ingredient = ingredients[position]

        val ingredientName = ingredient.name.split(" ").joinToString(" ") { it.capitalize() }
        holder.nameView.text = ingredientName
        holder.nameView.textAlignment = View.TEXT_ALIGNMENT_CENTER

        val imageUrl = "https://spoonacular.com/cdn/ingredients_100x100/${ingredient.image}"
        Glide.with(holder.imageView.context)
            .load(imageUrl)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.error_image)
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            if (onClickListener != null) {
                onClickListener!!.onClick(position, ingredient)
            }
        }
    }

    override fun getItemCount() = ingredients.size

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, ingredient: IngredientResponse)
    }


    fun updateIngredients(newIngredients: List<IngredientResponse>) {
        ingredients = newIngredients
        notifyDataSetChanged()
    }
}