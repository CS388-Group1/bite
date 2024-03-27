package com.example.bite.models

data class Ingredient(
    val id: Int,
    val name: String,
    val image: String,
    val amount: Double,
    val unit: String,
    val original: String
)

data class IngredientListResponse(
    val results: List<IngredientResponse>,
    val offset: Int,
    val number: Int,
    val totalResults: Int
)

data class IngredientResponse(
    val id: Int,
    val name: String,
    val image: String
)