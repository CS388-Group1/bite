package com.example.bite.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.bite.models.CustomCreateRecipe
import com.example.bite.models.CustomIngredient
import com.example.bite.models.CustomRecipe
import com.example.bite.models.RecipeIngredientCrossRef
import com.example.bite.models.RecipeWithIngredients

@Dao
interface CustomRecipeDao {

    @Insert
    suspend fun insertCustomRecipe(customRecipe: CustomRecipe): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCustomIngredient(customIngredient: CustomIngredient): Long

    @Insert
    suspend fun insertRecipeIngredientCrossRef(recipeIngredientCrossRef: RecipeIngredientCrossRef)

    @Transaction
    suspend fun insertCustomCreateRecipe(customCreateRecipe: CustomCreateRecipe) {
        // Insert the CustomRecipe
        val recipeId = insertCustomRecipe(
            CustomRecipe(
                name = customCreateRecipe.name,
                image = customCreateRecipe.image,
                desc = customCreateRecipe.desc,
                servings = customCreateRecipe.servings,
                readyInMinutes = customCreateRecipe.readyInMinutes,
                instructions = customCreateRecipe.instructions
            )
        )

        // Insert each CustomIngredient and RecipeIngredientCrossRef
        customCreateRecipe.ingredients.forEach { ingredient ->
            val ingredientId = insertCustomIngredient(
                CustomIngredient(
                    name = ingredient.name,
                    amount = ingredient.amount,
                    unit = ingredient.unit
                )
            )

            insertRecipeIngredientCrossRef(
                RecipeIngredientCrossRef(
                    recipeId = recipeId.toInt(),
                    ingredientId = ingredientId.toInt()
                )
            )
        }
    }
}
