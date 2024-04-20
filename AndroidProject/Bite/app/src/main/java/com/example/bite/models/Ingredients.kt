package com.example.bite.models

import androidx.room.Entity
import androidx.room.PrimaryKey

data class IngredientListResponse(
    val results: List<IngredientResponse>,
    val offset: Int,
    val number: Int,
    val totalResults: Int
)

data class IngredientResponse(
    val id: Int,
    val name: String,
    val image: String
) {
    fun toIngredient(): Ingredient {
        return Ingredient(
            id = id,
            name = name,
            image = image,
            amount = 0.0,
            unit = "",
            isCommon = false
        )
    }
}

data class RecipeIngredientsResponse(
    val ingredients: List<RecipeIngredients>
)

data class RecipeIngredients(
    val amount: AmountResponse,
    val image: String,
    val name: String
) {
    fun toIngredient(): Ingredient {
        return Ingredient(
            name = name,
            amount = amount.us.value,
            unit = amount.us.unit,
            image = image,
            id = 0
        )
    }
}

data class AmountResponse(
    val us: AmountUsResponse
)

data class AmountUsResponse(
    val unit: String,
    val value: Double
)

@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val image: String,
    val amount: Double,
    val unit: String,
    val isCommon: Boolean = false,
    val isSelected: Boolean = false
)
// Stores ingredients from recipes created by the user (Generate ids for each ingredient, not to be confused with ingredient ids
// from the Spoonacular API)
@Entity(tableName = "custom_ingredient")
data class CustomIngredient(
    @PrimaryKey(autoGenerate = true) val ingredientId: Int = 0,
    val name: String,
    val amount: Double,
    val unit: String
){
    // Default constructor
    constructor() : this(0, "", 0.0, "")
}

data class CustomCreateIngredient(
    val name: String,
    val amount: Double,
    val unit: String
)
