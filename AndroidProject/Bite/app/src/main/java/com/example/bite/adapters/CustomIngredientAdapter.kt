package com.example.bite.adapters
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bite.R
import com.example.bite.models.CustomCreateIngredient

class CustomIngredientAdapter(private val ingredientList: MutableList<CustomCreateIngredient>) :
    RecyclerView.Adapter<CustomIngredientAdapter.CustomIngredientViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomIngredientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_create_recipe_ingredient, parent, false)
        return CustomIngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomIngredientViewHolder, position: Int) {
        val ingredient = ingredientList[position]
        holder.bind(ingredient)
    }

    override fun getItemCount(): Int {
        return ingredientList.size
    }

    inner class CustomIngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ingredientTextView: TextView = itemView.findViewById(R.id.ingredientTextView)
        private val portionTextView: TextView = itemView.findViewById(R.id.portionTextView)
        private val minusButton: ImageView = itemView.findViewById(R.id.minusButton)

        fun bind(ingredient: CustomCreateIngredient) {
            ingredientTextView.text = ingredient.name
            portionTextView.text = "${ingredient.amount} ${ingredient.unit}"

            minusButton.setOnClickListener {
                // Handle click to remove the ingredient from the list
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    ingredientList.removeAt(position)
                    notifyItemRemoved(position)
                }
            }
        }
    }
}