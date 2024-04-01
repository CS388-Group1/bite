package com.example.bite.network


import android.util.Log
import com.example.bite.models.IngredientListResponse
import com.example.bite.models.Recipe
import com.example.bite.models.RecipeResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SpoonacularRepository {
    private val api = Retrofit.Builder()
        .baseUrl("https://api.spoonacular.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SpoonacularApi::class.java)

    suspend fun autocompleteIngredient(query: String) = api.autocompleteIngredient(query)

    suspend fun parseIngredients(ingredientList: String) = api.parseIngredients(ingredientList)

    suspend fun searchRecipesByIngredients(ingredients: String): List<Recipe> {
        val response = api.searchRecipesByIngredients(ingredients)
        return response.map { it.toRecipe() }
    }

    suspend fun searchIngredientByName(query: String): IngredientListResponse {
        return api.searchIngredientByName(query)
    }

    suspend fun searchRecipeByName(query: String): List<Recipe> {
        val response = api.searchRecipeByName(query)
        return response.results.map { it.toRecipe() }
    }

    suspend fun getTrendingRecipes(): RecipeResponse {
        val response = api.getTrendingRecipes()
        Log.v("response", response.toString())
        return response.toRecipe()
    }



}