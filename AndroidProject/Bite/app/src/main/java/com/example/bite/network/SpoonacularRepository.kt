package com.example.bite.network

import com.example.bite.models.IngredientListResponse
import com.example.bite.models.Recipe
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.*


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

    suspend fun getRecipeInfo(recipeId: String): Recipe? {
        return try {
            coroutineScope {
                val recipeInfoDeferred =
                    async { api.getRecipeById(recipeId) } // First API call to get basic recipe info
                val recipeResponse = recipeInfoDeferred.await().toRecipe()

                //val recipeInstructionsDeferred = async { api.getRecipeInstructions(recipeId) } // API call to get recipe instructions
                //val recipeInstructionsResponse = recipeInstructionsDeferred.await()

                // Make additional API calls for extra information
                val summaryDeferred =
                    async { api.getRecipeSummary(recipeId) } // Example additional API call
                val description = summaryDeferred.await().summary?:"Description Unavailable"
                recipeResponse?.copy(description = description)

                //val recipe = recipeResponse.toRecipe(recipeInstructionsResponse)


                // Update the recipe with the extra information
                //recipe.copy(description = description)
            }

        } catch (e: Exception) {
            null // Handle error or return null in case of failure
        }
    }



}