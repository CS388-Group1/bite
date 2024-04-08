package com.example.bite.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey val id: String,
    val title: String,
    val summary: String,
    val image: String,
    val cookingTime: Int,
    val sourceName: String,
    var instructions: String? = null,
    var isFavorite: Boolean = false,
)

data class RecipeResponse(
    val id: Int,
    val title: String,
    val image: String,
    val readyInMinutes: Int

){
    fun toRecipe(): Recipe {
        return Recipe(
            id = this.id.toString(),
            title = this.title,
            summary = "",
            image = this.image,
            cookingTime = this.readyInMinutes,
            sourceName = "Spoonacular",
            instructions = null,
            isFavorite = false
        )
    }
}

data class RecipeListResponse(
    val recipes: List<RecipeResponse>
)

// For GetRecipeInformation Response
data class DetailedRecipeResponse(
    val id: Int,
    val title: String,
    val image: String,
    val sourceName: String?,
    val summary: String?,
    val readyInMinutes: Int,
) {
    fun toRecipe(): Recipe {
        return Recipe(
            id = id.toString(),
            title = title,
            summary = summary ?: "",
            image = image,
            cookingTime = readyInMinutes ?: 0,
            sourceName = sourceName ?: "Null",
            instructions = null
        )
    }
}


data class InstructionsResponse(
    val instructions: List<Instruction>?
) {
    fun toJsonString(): String {
        return this.toString()
    }
}
data class Instruction(
    val name: String?,
    val steps: List<Step>?
)
data class Step(
    val equipment: List<Equipment>?,
    val ingredients: List<Ingr>?,
    val number: Int?,
    val step: String?,
    val length: Length?
)

data class Equipment(
    val id: Int?,
    val image: String?,
    val name: String?,
    val temperature: Temperature?
)

data class Ingr(
    val id: Int?,
    val image: String?,
    val name: String?
)

data class Length(
    val number: Int?,
    val unit: String?
)

data class Temperature(
    val number: Double?,
    val unit: String?
)

data class RecipeInstruction(
    val name: String?,
    val steps: List<Step>?
)

data class InstructionStep(
    val number: Int,
    val step: String
)