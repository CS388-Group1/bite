package com.example.bite.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.bite.models.Ingredient
import com.example.bite.models.Recipe
import com.example.bite.models.UserPreferences

@Dao
interface UserPreferencesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPreferences(userPreferences: UserPreferences)

    @Query("SELECT * FROM user_preferences LIMIT 1")
    suspend fun getUserPreferences(): UserPreferences?

    @Update
    suspend fun updateUserPreferences(userPreferences: UserPreferences)
}
