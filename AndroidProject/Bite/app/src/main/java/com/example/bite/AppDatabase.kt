package com.example.bite

import com.example.bite.models.InstructionStepConverter
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.bite.daos.CustomIngredientDao
import com.example.bite.daos.CustomRecipeDao
import com.example.bite.daos.IngredientDao
import com.example.bite.daos.RecipeDao
import com.example.bite.daos.UserPreferencesDao
import com.example.bite.models.CustomIngredient
import com.example.bite.models.CustomRecipe
import com.example.bite.models.Ingredient
import com.example.bite.models.Recipe
import com.example.bite.models.RecipeIngredientCrossRef
import com.example.bite.models.UserPreferences

// AppDatabase.kt
@Database(entities = [Ingredient::class, Recipe::class, UserPreferences::class, CustomRecipe::class, CustomIngredient::class, RecipeIngredientCrossRef::class], version = 6, exportSchema = false)
@TypeConverters(InstructionStepConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeDao(): RecipeDao
    abstract fun userPreferencesDao(): UserPreferencesDao
    abstract fun customRecipeDao(): CustomRecipeDao
    abstract fun customIngredientDao(): CustomIngredientDao

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
            val MIGRATION_2_3: Migration = object : Migration(2, 3) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    // Create the CustomRecipe table
                    database.execSQL("""
            CREATE TABLE IF NOT EXISTS custom_recipe (
                recipeId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                image TEXT NOT NULL,
                servings INTEGER NOT NULL,
                readyInMinutes INTEGER NOT NULL,
                instructions TEXT NOT NULL
            )
        """.trimIndent())

                    // Create the CustomIngredient table
                    database.execSQL("""
            CREATE TABLE IF NOT EXISTS custom_ingredient (
                ingredientId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                amount REAL NOT NULL,
                unit TEXT NOT NULL
            )
        """.trimIndent())

                    // Create the RecipeIngredientCrossRef table
                    database.execSQL("""
            CREATE TABLE IF NOT EXISTS recipe_ingredient_cross_ref (
                recipeId INTEGER NOT NULL,
                ingredientId INTEGER NOT NULL,
                PRIMARY KEY(recipeId, ingredientId),
                FOREIGN KEY(recipeId) REFERENCES custom_recipe(recipeId) ON DELETE CASCADE,
                FOREIGN KEY(ingredientId) REFERENCES custom_ingredient(ingredientId) ON DELETE CASCADE
            )
        """.trimIndent())
                }
            }
            val MIGRATION_3_4 = object : Migration(3, 4) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    // Perform the necessary schema changes here
                    // For example, adding a new column to the ingredients table
                    database.execSQL("ALTER TABLE custom_recipe ADD COLUMN desc TEXT NOT NULL")
                }
            }
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "bite_database"
            )    .addMigrations(MIGRATION_1_2)
                .addMigrations(MIGRATION_2_3)
                .addMigrations(MIGRATION_3_4)
                .addMigrations(MIGRATION_4_5)
                .addMigrations(MIGRATION_5_6)
                .build()
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the userId column to the custom_recipe table
                database.execSQL("ALTER TABLE custom_recipe ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Perform any necessary schema changes here for the upgrade from version 5 to version 6
                // For example, alter tables, add columns, etc.
                database.execSQL("ALTER TABLE custom_recipes ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
            }
        }
    }


}