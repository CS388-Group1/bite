package com.example.bite

import android.app.Application

class RecipeDataApp: Application() {
    val db by lazy { AppDatabase.getInstance(this) }
}