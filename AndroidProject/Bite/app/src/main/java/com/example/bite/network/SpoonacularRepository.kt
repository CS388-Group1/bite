package com.example.bite.network

import android.util.Log
import com.example.bite.models.HomeRecipe
import com.example.bite.models.IngredientListResponse
import com.example.bite.models.Recipe
import com.example.bite.models.RecipeResponse
import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.*
import org.json.JSONArray

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

    suspend fun getTrendingRecipes(): List<HomeRecipe> {
        val response = api.getTrendingRecipes()
        return response.recipes.map { it.toHomeRecipe()}
    }

    suspend fun getRandomRecipe(): List<HomeRecipe> {
        val response = api.getRandomRecipe()
        return response.recipes.map { it.toHomeRecipe() }
    }

//    suspend fun getRecipeInstructions(recipeId: String): String? {
//        return try {
//            // Make API call to fetch recipe instructions
//            val jsonResponseArray = api.getRecipeInstructions(recipeId)
//
//            // Parse the JSON array manually
//            val jsonArray = JSONArray(jsonResponseArray)
//            val stringBuilder = StringBuilder()
//
//            for (i in 0 until jsonArray.length()) {
//                val jsonStepObject = jsonArray.getJSONObject(i)
//                val number = jsonStepObject.getInt("number")
//                val step = jsonStepObject.getString("step")
//                stringBuilder.append("$number. $step\n")
//            }
//
//            return stringBuilder.toString()
//        } catch (e: Exception) {
//            null // Handle error or return null in case of failure
//        }
//    }


    suspend fun getRecipeInfo(recipeId: String): Recipe? {
        return try {
            coroutineScope {
                val recipeInfoDeferred = async { api.getRecipeById(recipeId) } // First API call to get basic recipe info
                var recipe = recipeInfoDeferred.await().toRecipe()

                //val recipeInstructionsDeferred = async { api.getRecipeInstructions(recipeId) } // API call to get recipe instructions
                //val recipeInstructionsResponse = recipeInstructionsDeferred.await()

                // Make additional API calls for extra information
                    //val summaryDeferred = async { api.getRecipeSummary(recipeId) } // Example additional API call
                    //val description = summaryDeferred.await().summary?:"Description Unavailable"
                    //recipeResponse?.copy(description = description)
                    //val recipe = recipeResponse.toRecipe(recipeInstructionsResponse)

                // Make API call to fetch recipe instructions
                val response = api.getRecipeInstructions(recipeId)

                val responseBody = response.body()
                val jsonResponse = responseBody?.string() // Read response body as a string

                // Parse the JSON array manually
                val jsonArray = JSONArray(jsonResponse)
                val stringBuilder = StringBuilder()

                if (jsonArray.length() > 0) {
                    val firstJsonObject = jsonArray.getJSONObject(0)
                    if (firstJsonObject.has("steps")) {
                        val stepsArray = firstJsonObject.getJSONArray("steps")
                        // Loop through each step object in the "steps" array
                        for (i in 0 until stepsArray.length()) {
                            val stepObject = stepsArray.getJSONObject(i)

                            // Check if the step object has the "number" and "step" attributes
                            if (stepObject.has("number") && stepObject.has("step")) {
                                val number = stepObject.getInt("number")
                                val step = stepObject.getString("step")
                                stringBuilder.append("$number. $step\n")
                            }
                        }
                    }
                }

                recipe.instructions = stringBuilder.toString()

                recipe
                // Update the recipe with the extra information
                //recipe.copy(description = description)
            }

        } catch (e: Exception) {
            Log.e("RecipeInfo", "Error fetching recipe info: ${e.message}", e)
            null // Handle error or return null in case of failure
        }
    }
}