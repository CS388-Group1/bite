package com.example.bite

import com.example.bite.models.InstructionStepConverter
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.bite.daos.IngredientDao
import com.example.bite.daos.RecipeDao
import com.example.bite.daos.UserPreferencesDao
import com.example.bite.models.Ingredient
import com.example.bite.models.Recipe
import com.example.bite.models.UserPreferences

// AppDatabase.kt
@Database(entities = [Ingredient::class, Recipe::class, UserPreferences::class], version = 2, exportSchema = false)
@TypeConverters(InstructionStepConverter::class)
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
            val MIGRATION_1_2 = object : Migration(1, 2) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    // Perform the necessary schema changes here
                    // For example, adding a new column to the ingredients table
                    database.execSQL("ALTER TABLE ingredients ADD COLUMN isSelected INTEGER NOT NULL DEFAULT 0")
                }
            }
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "bite_database"
            )    .addMigrations(MIGRATION_1_2)
                .build()
        }
    }


}