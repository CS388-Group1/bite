package com.example.bite.models

data class Recipe(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val cookingTime: Int,
    val sourceName: String,
    val instructions: List<InstructionStep>?
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

data class RecipeInstructionsResponse(
    val steps: List<InstructionStep>
)


data class InstructionStep (
    val number: Int,
    val step: String
)
