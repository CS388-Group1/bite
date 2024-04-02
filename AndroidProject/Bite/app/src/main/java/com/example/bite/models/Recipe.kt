package com.example.bite.models

import com.google.gson.Gson

data class Recipe(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val cookingTime: Int,
    val sourceName: String,
    var instructions: String?
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
            name = title,
            description = "Description Unavailable",
            imageUrl = image,
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
data class DetailedRecipeResponse (
    val id: Int,
    val title: String,
    val image: String,
    val sourceName: String?,
    val summary: String?
){
    fun toRecipe(): Recipe {
        return Recipe(
            id = id.toString(),
            name = title,
            description = summary?: "Description Unavailable",
            imageUrl = image,
            cookingTime = 0,
            sourceName = sourceName?: "Null",
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



