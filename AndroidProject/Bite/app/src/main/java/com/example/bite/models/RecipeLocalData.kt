package com.example.bite.models

import android.content.Context
import com.example.bite.daos.RecipeDao
import com.example.bite.models.Recipe

class RecipeLocalData(private val recipeDao: RecipeDao, private val context: Context){
    fun getAllRecipes(): List<Recipe>? {
        val recipes = recipeDao.getAllRecipes().value
        if(recipes !== null){
            return recipes
        }
        return null;
    }

    fun getFavoriteRecipes(): List<Recipe>? {
        val favorites = recipeDao.getFavoriteRecipes().value
        if(favorites !== null){
            return favorites
        }
        return null;
    }

    fun updateRecipe(favorite: Boolean, id: String) {
         recipeDao.updateRecipe(favorite,id)
    }

}