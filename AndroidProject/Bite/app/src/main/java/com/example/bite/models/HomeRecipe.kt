package com.example.bite.models

data class HomeRecipe(
    val id: String,
    val name: String,
    val author: String,
    val imageUrl: String,
)

data class HomeRecipeResponse(
    val id: Int,
    val title: String,
    val creditsText: String,
    val image: String
){
    fun toHomeRecipe(): HomeRecipe{
        return HomeRecipe(
            id = id.toString(),
            name = title,
            author = creditsText,
            imageUrl = image
        )
    }
}

data class HomeRecipeListResponse(
    val id: Int,
    val title: String,
    val image: String,
    val recipes: List<HomeRecipeResponse>)