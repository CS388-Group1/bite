package com.example.bite.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bite.AppDatabase
import com.example.bite.daos.CustomRecipeDao
import kotlinx.coroutines.launch

class CustomRecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val customRecipeDao: CustomRecipeDao = AppDatabase.getInstance(application).customRecipeDao()

    // Example function to insert a CustomCreateRecipe
    fun insertCustomCreateRecipe(customCreateRecipe: CustomCreateRecipe) {
        viewModelScope.launch {
            customRecipeDao.insertCustomCreateRecipe(customCreateRecipe)
        }
    }
}