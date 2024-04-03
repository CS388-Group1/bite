package com.example.bite.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey val id: String,
    val title: String,
    val summary: String,
    val image: String,
    val cookingTime: Int,
    val sourceName: String,
    val isFavorite: Boolean = false,
    @TypeConverters(InstructionStepConverter::class)
    val instructions: List<InstructionStep>? = null
)

data class RecipeResponse(
    val id: Int,
    val title: String,
    val image: String,
    val missedIngredientCount: Int,
    val missedIngredients: List<Ingredient>,
    val usedIngredientCount: Int,
    val usedIngredients: List<Ingredient>
) {
    fun toRecipe(): Recipe {
        return Recipe(
            id = id.toString(),
            title = title,
            summary = "Description Unavailable",
            image = image,
            cookingTime = 0,
            sourceName = "Null",
            instructions = null
        )
    }
}

data class RecipeListResponse(
    val id: Int,
    val title: String,
    val results: List<RecipeResponse>
)

// For GetRecipeInformation Response
data class DetailedRecipeResponse(
    val id: Int,
    val title: String,
    val image: String,
    val sourceName: String?,
    val summary: String?
) {
    fun toRecipe(): Recipe {
        return Recipe(
            id = id.toString(),
            title = title,
            summary = summary ?: "Description Unavailable",
            image = image,
            cookingTime = 0,
            sourceName = sourceName ?: "Null",
            instructions = null
        )
    }
}

data class RecipeInstructionsResponse(
    val steps: List<InstructionStep>
)

data class InstructionStep(
    val number: Int,
    val step: String
)