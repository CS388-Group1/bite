package com.example.bite.models

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.bite.daos.RecipeDao

class RecipeLocalData(private val recipeDao: RecipeDao, private val context: Context){

    suspend fun insertRecipe(recipe: Recipe){
        recipeDao.insertRecipe(recipe)
    }
    fun getAllRecipes(): List<Recipe>? {
        val recipes = recipeDao.getAllRecipes().value
        if(recipes !== null){
            return recipes
        }
        return null;
    }

    suspend fun getFavoriteRecipes(limit: Int, offset: Int): List<Recipe> {
        return recipeDao.getFavoriteRecipes(limit, offset)
    }

    fun updateRecipe(favorite: Boolean, id: String) {
         recipeDao.updateRecipe(favorite,id)
    }

    fun isRowIsExist(id : String) : Boolean{
        return recipeDao.isRowIsExist(id)
    }

}