package com.example.bite.network

import android.util.Log
import com.example.bite.BuildConfig
import com.example.bite.models.Ingredient
import com.example.bite.models.IngredientListResponse
import com.example.bite.models.Recipe
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
        val recipeIds = response.map { it.id.toString() }

        val recipes = recipeIds.map { recipeId ->
            getRecipeInfo(recipeId)
        }

        return recipes.filterNotNull()
    }

    suspend fun searchIngredientByName(query: String): IngredientListResponse {
        return api.searchIngredientByName(query)
    }

    suspend fun searchRecipeByName(query: String): List<Recipe> {
        val response = api.searchRecipeByName(query)
        return response.recipes.map { it.toRecipe() }
    }

    suspend fun getTrendingRecipes(): List<Recipe> {
        val response = api.getTrendingRecipes()
        return response.recipes.map { it.toRecipe()}
    }

    suspend fun getRandomRecipe(): List<Recipe> {
        val response = api.getRandomRecipe()
        return response.recipes.map { it.toRecipe() }
    }
    suspend fun getIngredients(recipeId: String): List<Ingredient>? {
        try {
            val recipeIngredientsResponse = api.getRecipeIngredients(recipeId)
            return recipeIngredientsResponse.ingredients.map { it.toIngredient() }
        } catch (e: Exception) {
            // Handle exceptions
            Log.e("getIngredients", "Exception: ${e.message}", e)
        }
        return null
    }

    suspend fun getRecipeInfo(recipeId: String): Recipe? {
        return try {
            coroutineScope {
                val recipeInfoDeferred = async { api.getRecipeById(recipeId) } // First API call to get basic recipe info
                var recipe = recipeInfoDeferred.await().toRecipe()


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
    suspend fun getDiscoverRecipes(pageSize: Int = 10): List<Recipe> {
        val response = api.getDiscoverRecipes(number = pageSize, tags = "vegetarian", apiKey = BuildConfig.SPOONACULAR_API_KEY)
        return response.recipes.map { it.toRecipeModel() }
    }


}