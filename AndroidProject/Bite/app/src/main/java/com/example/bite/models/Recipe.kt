package com.example.bite.models

data class Recipe(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val cookingTime: Int
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
            description = "Description unavailable",
            imageUrl = image,
            cookingTime = 0
        )
    }
}

data class RecipeListResponse(
    val id: Int,
    val title: String,
    val results: List<RecipeResponse>
)
