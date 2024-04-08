package com.example.bite.daos

import androidx.room.Dao
import androidx.room.Insert
import com.example.bite.models.CustomIngredient

@Dao
interface CustomIngredientDao {
    @Insert
    suspend fun insertCustomIngredient(customIngredient: CustomIngredient): Long

}