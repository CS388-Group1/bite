package com.example.bite.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bite.R
import com.example.bite.models.Recipe

class DiscoverAdapter(private val items: MutableList<Recipe> = mutableListOf(), private val listener: OnRecipeClickListener) : RecyclerView.Adapter<DiscoverAdapter.DiscoverViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscoverViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.discover_card, parent, false)
        return DiscoverViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: DiscoverViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    fun addItems(newItems: List<Recipe>) {
        Log.d("DiscoverAdapter", "Adding ${newItems.size} new items")
        items.addAll(newItems)
        notifyDataSetChanged()
    }
    interface OnRecipeClickListener {
        fun onRecipeClicked(recipeId: String)
    }

    class DiscoverViewHolder(view: View, private val listener: OnRecipeClickListener) : RecyclerView.ViewHolder(view) {

        private val imageView: ImageView = view.findViewById(R.id.imageView)
        private val recipeNameTextView: TextView = view.findViewById(R.id.discoverRecipeName)
        private val recipeTimeTextView: TextView = view.findViewById(R.id.discoverRecipeTime)

        fun bind(recipe: Recipe) {
            Glide.with(itemView.context).load(recipe.image).into(imageView)
            recipeNameTextView.text = recipe.title

            val timeFormatted = "${recipe.cookingTime} min"
            recipeTimeTextView.text = timeFormatted

            itemView.setOnClickListener {
                listener.onRecipeClicked(recipe.id)
            }

        }
    }
}
