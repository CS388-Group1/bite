package com.example.bite.network

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.bite.daos.IngredientDao
import com.example.bite.models.Ingredient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class IngredientRepository(private val ingredientDao: IngredientDao, private val context: Context) {
    suspend fun loadCommonIngredients() {
        if (ingredientDao.getCommonIngredients().isEmpty()) {
            val ingredients = parseIngredientsFromCSV(context)
            ingredientDao.insertIngredients(ingredients)
        }
    }

    suspend fun getCommonIngredients(): List<Ingredient> {
        return ingredientDao.getCommonIngredients()
    }

    private suspend fun parseIngredientsFromCSV(context: Context): List<Ingredient> {
        val ingredients = mutableListOf<Ingredient>()
        val assetManager = context.assets
        val inputStream = assetManager.open("top-1k-ingredients.csv")
        inputStream.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                val parts = line.split(",")
                if (parts.size == 4) {
                    val id = parts[0].toInt()
                    val name = parts[1]
                    val imageUrl = parts[2]
                    val isCommon = parts[3].toBoolean()

                    if (isCommon) {
                        val imagePath = saveImageToInternalStorage(imageUrl, context)
                        val ingredient = Ingredient(id = id, name = name, image = imagePath, amount = 0.0, unit = "", isCommon = true)
                        ingredients.add(ingredient)
                    }
                }
            }
        }
        return ingredients
    }

    private suspend fun saveImageToInternalStorage(imageUrl: String, context: Context): String {
        return withContext(Dispatchers.IO) {
            Log.d("IngredientRepository", "Downloading image: $imageUrl")
            try {
                val url = URL(imageUrl)
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val inputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(inputStream)

                val filename = "${System.currentTimeMillis()}.png"
                val file = File(context.filesDir, filename)
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
                inputStream.close()

                file.absolutePath
            } catch (e: IOException) {
                e.printStackTrace()
                ""
            }
        }
    }

    suspend fun updateIngredientSelection(id: Int, isSelected: Boolean) {
        ingredientDao.updateIngredientSelection(id, isSelected)
    }

    suspend fun getSelectedIngredients(): List<Ingredient> {
        return ingredientDao.getSelectedIngredients()
    }

}