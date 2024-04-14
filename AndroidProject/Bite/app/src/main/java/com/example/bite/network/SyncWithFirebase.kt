package com.example.bite.network

import android.util.Log
import com.example.bite.models.CustomIngredient
import com.example.bite.models.CustomRecipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.bite.daos.CustomRecipeDao as CustomRecipeDao


class SyncWithFirebase (private val customRecipeDao: CustomRecipeDao){

    private var fStore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun syncRecipesWithFirestore(userId: String) {
        Log.v("Sync ---->", "Started.")
        CoroutineScope(Dispatchers.IO).launch{
            val recipesWithIngredientsFromRoom = customRecipeDao.getAllCustomRecipesWithIngredients()

            recipesWithIngredientsFromRoom.forEach { recipeWithIngredients ->
                val recipe = recipeWithIngredients.recipe
                val ingredients = recipeWithIngredients.ingredients

                val recipeRef = fStore
                    .collection("users")
                    .document(userId)
                    .collection("createdRecipes")
                    .document(recipe.recipeId.toString())
                Log.v("Sync ----->", "dbRef created.")
                recipeRef.get().addOnSuccessListener { documentSnapshot ->
                    if (!documentSnapshot.exists()) {
                        // Recipe does not exist in Firestore, store it
                        val firestoreRecipe = mapRoomRecipeToFirestore(recipe)
                        recipeRef.set(firestoreRecipe)
                            .addOnSuccessListener {
                                // Recipe stored successfully in Firestore, now store ingredients
                                storeIngredientsInFirestore(userId, recipe.recipeId, ingredients)
                                Log.v("FirebaseSync", "Firebase recipe sync successful")
                            }
                            .addOnFailureListener { e ->
                                Log.e("FirebaseSync", "Could not sync recipe(s) with Firebase. Error: $e")
                            }
                    }
                }
            }
        }
    }


    private fun storeIngredientsInFirestore(
        userId: String,
        recipeId: Int,
        ingredients: List<CustomIngredient>
    ) {
        val ingredientsCollectionRef = fStore
            .collection("users")
            .document(userId)
            .collection("createdRecipes")
            .document(recipeId.toString())
            .collection("ingredients")

        ingredients.forEach { ingredient ->
            val ingredientId = ingredient.ingredientId.toString()
            val firestoreIngredient = mapRoomIngredientToFirestore(ingredient)

            ingredientsCollectionRef.document(ingredientId)
                .set(firestoreIngredient)
                .addOnFailureListener { e ->
                    Log.e("FirebaseSync", "Could not sync ingredient with Firebase. Error: $e")
                }
        }
    }

    private fun mapRoomRecipeToFirestore(recipe: CustomRecipe): Map<String, Any> {
        return hashMapOf<String, Any>(
            "name" to recipe.name,
            "image" to recipe.image,
            "description" to recipe.desc,
            "servings" to recipe.servings,
            "readyInMinutes" to recipe.readyInMinutes,
            "instructions" to recipe.instructions
        )
    }

    private fun mapRoomIngredientToFirestore(ingredient: CustomIngredient): Map<String, Any> {
        return hashMapOf<String, Any>(
            "name" to ingredient.name,
            "amount" to ingredient.amount,
            "unit" to ingredient.unit
        )
    }
}