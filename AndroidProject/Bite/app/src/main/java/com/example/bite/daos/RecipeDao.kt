package com.example.bite.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.bite.models.Ingredient
import com.example.bite.models.Recipe

@Dao
interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe)

    @Query("SELECT * FROM recipes")
    fun getAllRecipes(): LiveData<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE isFavorite = 1")
    suspend fun getFavoriteRecipes(): List<Recipe>

    @Query("UPDATE recipes SET isFavorite=:favorite WHERE id = :id")
    fun updateRecipe(favorite: Boolean, id: String)

    @Query("SELECT EXISTS(SELECT * FROM recipes WHERE id = :id)")
    fun isRowIsExist(id : String) : Boolean
}