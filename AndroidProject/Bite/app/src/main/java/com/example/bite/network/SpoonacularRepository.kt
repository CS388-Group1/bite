package com.example.bite.network

import android.graphics.Bitmap
import android.util.Log
import com.example.bite.BuildConfig
import com.example.bite.models.Ingredient
import com.example.bite.models.IngredientListResponse
import com.example.bite.models.Recipe
import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

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
        return try {
            val response = api.searchRecipeByName(query)
            Log.d("searchRecipeByName", "API Call made with query: $query")
            val jsonResponse = Gson().toJson(response)
            Log.d("searchRecipeByName", "Raw JSON response: $jsonResponse")

            if (response.results != null) {
                response.results.map { it.toRecipe() }
            } else {
                Log.d("searchRecipeByName", "Received null recipes list")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("searchRecipeByName", "Exception during API call: ${e.localizedMessage}", e)
            emptyList()
        }
    }


    suspend fun getTrendingRecipes(): List<Recipe> {
        val response = api.getTrendingRecipes()
        return response.recipes.map { it.toRecipe()}
    }

    suspend fun getRandomRecipe(recipeId: String): Recipe {
        val response = api.getRecipeById(recipeId)
        Log.v("RRecipe--->", response.toString())
        return response.toRecipe()
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
        val imageBytes = outputStream.toByteArray()

        // Encode the image as base64
        val base64Image = android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT)

        val apiUrl = "https://api.openai.com/v1/chat/completions"
        val requestBody = JSONObject()
        requestBody.put("model", "gpt-4-turbo")
        val messages = JSONArray()
        val message = JSONObject()
        message.put("role", "user")
        val content = JSONArray()
        content.put(JSONObject().put("type", "text").put("text", "What food item is in this image? (Please provide the name of the food item only avoid using sentences and adding any other information)"))
        content.put(JSONObject().put("type", "image_url").put("image_url", JSONObject().put("url", "data:image/jpeg;base64,$base64Image").put("detail", "high")))
        message.put("content", content)
        messages.put(message)
        requestBody.put("messages", messages)
        requestBody.put("max_tokens", 300)

        val request = Request.Builder()
            .url(apiUrl)
            .addHeader("Authorization", "Bearer ${BuildConfig.CHATGPT_API_KEY}")
            .post(RequestBody.create("application/json".toMediaTypeOrNull(), requestBody.toString()))
            .build()

        // Log the message
        Log.d("UploadImage", "Request body: ${requestBody.toString()}")

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("UploadImage", "Network error while uploading image", e)
                callback.onFailure("Failed to upload image: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                // Log the response
                Log.d("UploadImage", "Response: $responseBody")

                responseBody?.let { body ->
                    if (response.isSuccessful) {
                        Log.d("UploadImage", "Response success: $body")
                        callback.onSuccess(body)
                    } else {
                        Log.e("UploadImage", "Error classifying image: ${response.message}")
                        callback.onFailure("Error classifying image: ${response.message}")
                    }
                } ?: run {
                    Log.e("UploadImage", "Empty response body")
                    callback.onFailure("Empty response")
                }
            }
        })
    }

    suspend fun getDiscoverRecipes(pageSize: Int = 10): List<Recipe> {
        val response = api.getDiscoverRecipes(number = pageSize, tags = "vegetarian", apiKey = BuildConfig.SPOONACULAR_API_KEY)
        return response.recipes.map { it.toRecipe() }
    }


}