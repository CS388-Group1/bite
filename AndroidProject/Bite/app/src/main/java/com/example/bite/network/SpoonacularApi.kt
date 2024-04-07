package com.example.bite.network
import okhttp3.ResponseBody
import retrofit2.Response
import com.example.bite.BuildConfig
import com.example.bite.models.HomeRecipe
import com.example.bite.models.HomeRecipeListResponse
import com.example.bite.models.IngredientListResponse
import com.example.bite.models.IngredientResponse
import com.example.bite.models.Recipe
import com.example.bite.models.DetailedRecipeResponse
import com.example.bite.models.DiscoverRecipeListResponse
import com.example.bite.models.InstructionsResponse
import com.example.bite.models.RecipeIngredientsResponse
import com.example.bite.models.RecipeInstruction
import com.example.bite.models.RecipeListResponse
import com.example.bite.models.RecipeResponse
import com.google.gson.Gson
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SpoonacularApi {
    @GET("food/ingredients/autocomplete")
    suspend fun autocompleteIngredient(
        @Query("query") query: String,
        @Query("number") number: Int = 10,
        @Query("apiKey") apiKey: String = BuildConfig.SPOONACULAR_API_KEY
    ): IngredientListResponse

    @FormUrlEncoded
    @POST("recipes/parseIngredients")
    suspend fun parseIngredients(
        @Field("ingredientList") ingredientList: String,
        @Field("servings") servings: Int = 1,
        @Field("includeNutrition") includeNutrition: Boolean = true,
        @Query("apiKey") apiKey: String = BuildConfig.SPOONACULAR_API_KEY
    ): List<IngredientResponse>

    @GET("recipes/findByIngredients")
    suspend fun searchRecipesByIngredients(
        @Query("ingredients") ingredients: String,
        @Query("number") number: Int = 10,
        @Query("limitLicense") limitLicense: Boolean = true,
        @Query("ranking") ranking: Int = 1,
        @Query("ignorePantry") ignorePantry: Boolean = true,
        @Query("apiKey") apiKey: String = BuildConfig.SPOONACULAR_API_KEY
    ): List<RecipeResponse>

    @GET("food/ingredients/search")
    suspend fun searchIngredientByName(
        @Query("query") query: String,
        @Query("number") number: Int = 10,
        @Query("apiKey") apiKey: String = BuildConfig.SPOONACULAR_API_KEY
    ): IngredientListResponse

    @GET("recipes/random")
    suspend fun getTrendingRecipes(
        @Query("number") number: Int = 10,
        @Query("tags") tags: String = "vegetarian",
        @Query("apiKey") apiKey: String = BuildConfig.SPOONACULAR_API_KEY
    ): HomeRecipeListResponse

    @GET("recipes/random")
    suspend fun getDiscoverRecipes(
        @Query("number") number: Int = 50,
        @Query("tags") tags: String = "vegetarian",
        @Query("apiKey") apiKey: String
    ): DiscoverRecipeListResponse

    @GET("recipes/complexSearch")
    suspend fun searchRecipeByName(
        @Query("query") query: String,
        @Query("number") number: Int = 10,
        @Query("apiKey") apiKey: String = BuildConfig.SPOONACULAR_API_KEY
    ): RecipeListResponse

    @GET("recipes/random")
    suspend fun getRandomRecipe(
        @Query("number") number: Int = 1,
        @Query("apiKey") apiKey: String = BuildConfig.SPOONACULAR_API_KEY
    ): HomeRecipeListResponse

    @GET("recipes/{id}/information")
    suspend fun getRecipeById(
        @Path("id") id: String,
        @Query("apiKey") apiKey: String = BuildConfig.SPOONACULAR_API_KEY
    ): DetailedRecipeResponse

    @GET("recipes/{id}/summary")
    suspend fun getRecipeSummary(
        @Path("id") id: String,
        @Query("apiKey") apiKey: String = BuildConfig.SPOONACULAR_API_KEY
    ): DetailedRecipeResponse

    @GET("recipes/{id}/ingredientWidget.json")
    suspend fun getRecipeIngredients(
        @Path("id") id: String,
        @Query("apiKey") apiKey: String = BuildConfig.SPOONACULAR_API_KEY
    ): RecipeIngredientsResponse

    @GET("recipes/{id}/analyzedInstructions")
    suspend fun getRecipeInstructions(
        @Path("id") id: String,
        @Query("apiKey") apiKey: String = BuildConfig.SPOONACULAR_API_KEY
    ):Response<ResponseBody>

}