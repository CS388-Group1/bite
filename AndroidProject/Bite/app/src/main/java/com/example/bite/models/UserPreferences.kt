package com.example.bite.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreferences(
    @PrimaryKey val id: Int = 1,
    val dietaryRestrictions: String = "",
    val allergies: String = "",
    val favoriteIngredients: String = "",
    val preferredCuisine: String = "",
    val maxReadyTime: Int = 60,
    val defaultServings: Int = 2,
    val theme: String = "light"
)