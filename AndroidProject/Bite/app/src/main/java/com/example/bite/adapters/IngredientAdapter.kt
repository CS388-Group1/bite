package com.example.bite

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bite.models.Ingredient
import com.google.android.material.imageview.ShapeableImageView

class IngredientAdapter(private var ingredients: List<Ingredient>) :
    RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder>() {
    private var onClickListener: OnClickListener? = null

    inner class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

        // Load the image using Glide
        if (ingredient.isCommon) {
            // Load local image for common ingredients
            Glide.with(holder.itemView.context)
                .load(ingredient.image)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(holder.imageView)
        } else {
            // Load image from API for searched ingredients
            Glide.with(holder.itemView.context)
                .load(ingredient.image)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(holder.imageView)
        }

        holder.itemView.setOnClickListener {
            onClickListener?.onClick(position, ingredient)
        }
    }

    override fun getItemCount() = ingredients.size

    interface OnClickListener {
        fun onClick(position: Int, ingredient: Ingredient)
    }

    fun setOnClickListener(listener: OnClickListener) {
        onClickListener = listener
    }

    fun updateIngredients(newIngredients: List<Ingredient>) {
        val diffCallback = IngredientDiffCallback(ingredients, newIngredients)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        ingredients = newIngredients
        diffResult.dispatchUpdatesTo(this)
    }
}
class IngredientDiffCallback(
    private val oldList: List<Ingredient>,
    private val newList: List<Ingredient>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}