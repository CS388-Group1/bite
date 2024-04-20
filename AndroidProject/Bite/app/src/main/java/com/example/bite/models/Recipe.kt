package com.example.bite.models

import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.google.firebase.firestore.DocumentSnapshot


@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey val id: String,
    val userId: String?,
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
    @PrimaryKey(autoGenerate = true) var recipeId: Int = 0,
    @ColumnInfo(name = "name") var name: String = "",
    @ColumnInfo(name = "image") var image: String = "",
    @ColumnInfo(name = "servings") var servings: Int = 0,
    @ColumnInfo(name = "readyInMinutes") var readyInMinutes: Int = 0,
    @ColumnInfo(name = "instructions") var instructions: String = "",
    @ColumnInfo(name = "desc") var desc: String = "",
    @ColumnInfo(name = "userId") var userId: String = ""
) {
    companion object {
        fun fromSnapshot(snapshot: DocumentSnapshot): CustomRecipe {
            Log.d("Checking From Snapshot", "This is printing from the From Snapshot method.")
            val recipeId = snapshot.getLong("recipeId")?.toInt() ?: 0
            val name = snapshot.getString("name") ?: ""
            val image = snapshot.getString("image") ?: ""
            val servings = snapshot.getLong("servings")?.toInt() ?: 0
            val readyInMinutes = snapshot.getLong("readyInMinutes")?.toInt() ?: 0
            val instructions = snapshot.getString("instructions") ?: ""
            val desc = snapshot.getString("description") ?: "not found"
            val userId = snapshot.getString("userId") ?: ""

            Log.d("CustomRecipe", "RecipeId: $recipeId, Name: $name, Desc: $desc")

            return CustomRecipe(recipeId, name, image, servings, readyInMinutes, instructions, desc, userId)
        }
    }
}
// Data class used to hold recipes created by user
data class CustomCreateRecipe(
    val userId: String,
    val name: String,
    val image: String,
    val desc: String,
    val servings: Int,
    val readyInMinutes: Int,
    val instructions: String,
    val ingredients: List<CustomCreateIngredient>
)
// Referential entity that maps recipes to their ingredientsin
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
    val readyInMinutes: Int

){
    fun toRecipe(): Recipe {
        return Recipe(
            id = this.id.toString(),
            userId = null,
            title = this.title,
            summary = "",
            image = this.image,
            cookingTime = this.readyInMinutes,
            sourceName = "Spoonacular",
            instructions = null,
            isFavorite = false
        )
    }
}

data class RecipeListResponse(
    val recipes: List<RecipeResponse>
)

// For GetRecipeInformation Response
data class DetailedRecipeResponse(
    val id: Int,
    val title: String,
    val image: String,
    val sourceName: String?,
    val summary: String?,
    val readyInMinutes: Int,
) {
    fun toRecipe(): Recipe {
        return Recipe(
            id = id.toString(),
            userId = null,
            title = title,
            summary = summary ?: "",
            image = image,
            cookingTime = readyInMinutes ?: 0,
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