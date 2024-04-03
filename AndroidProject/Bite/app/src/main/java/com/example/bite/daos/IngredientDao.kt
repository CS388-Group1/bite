package com.example.bite.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bite.models.Ingredient

@Dao
interface IngredientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<Ingredient>)

    @Query("SELECT * FROM ingredients")
    fun getAllIngredients(): LiveData<List<Ingredient>>

    @Query("SELECT * FROM ingredients WHERE name LIKE :query")
    suspend fun searchIngredientsByName(query: String): List<Ingredient>

    @Query("SELECT * FROM ingredients WHERE isCommon = 1")
    suspend fun getCommonIngredients(): List<Ingredient>

}