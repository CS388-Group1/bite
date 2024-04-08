package com.example.bite.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.bite.models.CustomRecipe
import com.example.bite.models.RecipeWithIngredients

@Dao
interface CustomRecipeDao {
    @Insert
    suspend fun insertCustomRecipe(customRecipe: CustomRecipe): Long

//    @Transaction
//    @Query("SELECT * FROM custom_recipe WHERE recipeId = :recipeId")
//    suspend fun getRecipeWithIngredients(recipeId: Int): RecipeWithIngredients
}