package com.example.bite.models

data class HomeRecipe(
    val id: String,
    val title: String,
    val author: String,
    val image: String,
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
            title = title,
            author = creditsText,
            image = image
        )
    }
}

data class HomeRecipeListResponse(
    val id: Int,
    val title: String,
    val image: String,
    val recipes: List<HomeRecipeResponse>)

data class DiscoverRecipeListResponse(
    val recipes: List<DiscoverRecipe>,
)

data class DiscoverRecipe(
    val id: Int,
    val title: String,
    val image: String,
    val readyInMinutes: Int

){
    fun toRecipeModel(): Recipe {
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