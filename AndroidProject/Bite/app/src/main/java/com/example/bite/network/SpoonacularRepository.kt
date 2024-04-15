package com.example.bite.network

import android.graphics.Bitmap
import android.util.Log
import com.example.bite.BuildConfig
import com.example.bite.models.Ingredient
import com.example.bite.models.IngredientListResponse
import com.example.bite.models.Recipe
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.io.IOException

class SpoonacularRepository {
    private val api = Retrofit.Builder()
        .baseUrl("https://api.spoonacular.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SpoonacularApi::class.java)

    suspend fun autocompleteIngredient(query: String) = api.autocompleteIngredient(query)

    suspend fun parseIngredients(ingredientList: String) = api.parseIngredients(ingredientList)

    suspend fun searchRecipesByIngredients(ingredients: String, number: Int = 10, offset: Int = 0): List<Recipe> {
        val response = api.searchRecipesByIngredients(ingredients, number, limitLicense = true, ranking = 1, ignorePantry = true, offset = offset)
//        val response = api.searchRecipesByIngredients(ingredients)
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

    // Interface for callbacks
    interface UploadCallback {
        fun onSuccess(result: String)
        fun onFailure(error: String)
    }

    // Function for uploading an image bitmap to spoonacualr
    fun uploadImage(imageBitmap: Bitmap, callback: UploadCallback) {
        // Converting the bitmap to a byte array
        val outputStream = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

        // create a multipart form body, which is used to send image data
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "image.jpg",
                RequestBody.create("image/jpeg".toMediaTypeOrNull(), outputStream.toByteArray()))
            .build()

        val apiUrl = "https://api.spoonacular.com/food/images/classify?apiKey=${BuildConfig.SPOONACULAR_API_KEY}"
        val request = Request.Builder()
            .url(apiUrl)
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("UploadImage", "Network error while uploading image", e)
                callback.onFailure("Failed to upload image: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { responseBody ->
                    Log.d("UploadImage", "Response success: $responseBody")
                    callback.onSuccess("Image classified successfully: $responseBody")
                } ?: run {
                    Log.e("UploadImage", "Empty response body")
                    callback.onFailure("Empty response")
                }
                if (!response.isSuccessful) {
                    Log.e("UploadImage", "Error classifying image: ${response.message}")
                    callback.onFailure("Error classifying image: ${response.message}")
                }
            }
        })
    }

    suspend fun getDiscoverRecipes(pageSize: Int = 10): List<Recipe> {
        val response = api.getDiscoverRecipes(number = pageSize, tags = "vegetarian", apiKey = BuildConfig.SPOONACULAR_API_KEY)
        return response.recipes.map { it.toRecipe() }
    }


}