package com.example.bite

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.bite.daos.IngredientDao
import com.example.bite.daos.RecipeDao
import com.example.bite.daos.UserPreferencesDao
import com.example.bite.models.Ingredient
import com.example.bite.models.Recipe
import com.example.bite.models.UserPreferences

// AppDatabase.kt
@Database(
    entities = [Ingredient::class, Recipe::class, UserPreferences::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeDao(): RecipeDao
    abstract fun userPreferencesDao(): UserPreferencesDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "bite_database"
            ).build()
        }
    }
}