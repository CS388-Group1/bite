package com.example.bite.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey val id: String,
    val title: String,
    val summary: String,
    val image: String,
    val cookingTime: Int,
    val sourceName: String,
    var instructions: String? = null,
    var isFavorite: Boolean = false,
)

// Stores recipes created by the user in local database
@Entity(tableName = "custom_recipe")
data class CustomRecipe(
    @PrimaryKey(autoGenerate = true) val recipeId: Int = 0,
    val name: String,
    val image: String,
    val desc: String,
    val servings: Int,
    val readyInMinutes: Int,
    val instructions: String
)
// Data class used to hold recipes created by user
data class CustomCreateRecipe(
    val name: String,
    val image: String,
    val desc: String,
    val servings: Int,
    val readyInMinutes: Int,
    val instructions: String,
    val ingredients: List<CustomCreateIngredient>
)
// Referential entity that maps recipes to their ingredients
@Entity(
    tableName = "recipe_ingredient_cross_ref",
    primaryKeys = ["recipeId", "ingredientId"],
    foreignKeys = [
        ForeignKey(entity = CustomRecipe::class,
            parentColumns = arrayOf("recipeId"),
            childColumns = arrayOf("recipeId"),
            onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = CustomIngredient::class,
            parentColumns = arrayOf("ingredientId"),
            childColumns = arrayOf("ingredientId"),
            onDelete = ForeignKey.CASCADE)
    ]
)
data class RecipeIngredientCrossRef(
    val recipeId: Int,
    val ingredientId: Int
)

data class RecipeWithIngredients(
    @Embedded val customRecipe: CustomRecipe,
    @Relation(
        parentColumn = "recipeId",
        entityColumn = "ingredientId",
        associateBy = Junction(RecipeIngredientCrossRef::class)
    )
    val customIngredients: List<CustomIngredient>
)

data class IngredientWithRecipes(
    @Embedded val customIngredient: CustomIngredient,
    @Relation(
        parentColumn = "ingredientId",
        entityColumn = "recipeId",
        associateBy = Junction(RecipeIngredientCrossRef::class)
    )
    val customRecipes: List<CustomRecipe>
)

data class RecipeResponse(
    val id: Int,
    val title: String,
    val image: String,
    val missedIngredientCount: Int,
    val missedIngredients: List<Ingredient>,
    val usedIngredientCount: Int,
    val usedIngredients: List<Ingredient>
) {
    fun toRecipe(): Recipe {
        return Recipe(
            id = id.toString(),
            title = title,
            summary = "Description Unavailable",
            image = image,
            cookingTime = 0,
            sourceName = "Null",
            instructions = null
        )
    }
}

data class RecipeListResponse(
    val id: Int,
    val title: String,
    val results: List<RecipeResponse>
)

// For GetRecipeInformation Response
data class DetailedRecipeResponse(
    val id: Int,
    val title: String,
    val image: String,
    val sourceName: String?,
    val summary: String?
) {
    fun toRecipe(): Recipe {
        return Recipe(
            id = id.toString(),
            title = title,
            summary = summary ?: "Description Unavailable",
            image = image,
            cookingTime = 0,
            sourceName = sourceName ?: "Null",
            instructions = null
        )
    }
}


data class InstructionsResponse(
    val instructions: List<Instruction>?
) {
    fun toJsonString(): String {
        return this.toString()
    }
}
data class Instruction(
    val name: String?,
    val steps: List<Step>?
)
data class Step(
    val equipment: List<Equipment>?,
    val ingredients: List<Ingr>?,
    val number: Int?,
    val step: String?,
    val length: Length?
)

data class Equipment(
    val id: Int?,
    val image: String?,
    val name: String?,
    val temperature: Temperature?
)

data class Ingr(
    val id: Int?,
    val image: String?,
    val name: String?
)

data class Length(
    val number: Int?,
    val unit: String?
)

data class Temperature(
    val number: Double?,
    val unit: String?
)

data class RecipeInstruction(
    val name: String?,
    val steps: List<Step>?
)

data class InstructionStep(
    val number: Int,
    val step: String
)

