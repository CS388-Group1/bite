package com.example.bite

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bite.adapters.CustomIngredientAdapter
import com.example.bite.models.CustomCreateIngredient
import com.example.bite.models.CustomCreateRecipe
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateRecipeActivity : AppCompatActivity() {

    private lateinit var addImage: ImageView
    private var imageFilePath: String? = null
    private val REQUEST_IMAGE_CAPTURE = 100
    private val IMAGE_FILE_NAME_PREFIX = "recipe_image_"
    private val ingredientList = mutableListOf<CustomCreateIngredient>()
    private lateinit var customIngredientAdapter: CustomIngredientAdapter
    private lateinit var servesTextView: TextView
    private lateinit var cookTimeTextView: TextView
    private lateinit var servingsView: TextView
    private lateinit var minutesView: TextView
    private lateinit var recipeName: EditText
    private lateinit var recipeDesc: EditText
    private lateinit var recipeInstructions: EditText
    private lateinit var saveRecipeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_recipe)

        addImage = findViewById(R.id.addImage)
        servesTextView = findViewById(R.id.addServes)
        cookTimeTextView = findViewById(R.id.addCookTime)
        servingsView = findViewById(R.id.servings)
        minutesView = findViewById(R.id.minutes)
        recipeName = findViewById(R.id.addRecipeName)
        recipeDesc = findViewById(R.id.addRecipeDesc)
        recipeInstructions = findViewById(R.id.addInstructions)
        saveRecipeButton = findViewById(R.id.saveRecipeButton)

        addImage.setOnClickListener {
            dispatchTakePictureIntent()
        }

        val addIngredientsRv = findViewById<RecyclerView>(R.id.addIngredientsRv)
        customIngredientAdapter = CustomIngredientAdapter(ingredientList)
        addIngredientsRv.layoutManager = LinearLayoutManager(this)
        addIngredientsRv.adapter = customIngredientAdapter

        val addButton = findViewById<ImageView>(R.id.addButton)
        val ingredientInput = findViewById<EditText>(R.id.addIngredientEditText)
        val portionInput = findViewById<EditText>(R.id.addPortionEditText)

        addButton.setOnClickListener {
            val ingredient = ingredientInput.text.toString().trim()
            val portion = portionInput.text.toString().trim()

            if (ingredient.isNotEmpty() && portion.isNotEmpty()) {
                val (number, unit) = extractNumberAndUnit(portion)
                val newIngredient = CustomCreateIngredient(ingredient, number, unit)
                ingredientList.add(newIngredient)
                customIngredientAdapter.notifyDataSetChanged()
                portionInput.text = null
                ingredientInput.text = null
            } else {
                Toast.makeText(
                    this,
                    "Please enter both ingredient name and portion (quantity with unit).",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        servesTextView.setOnClickListener { showPopup(servesTextView) }
        cookTimeTextView.setOnClickListener { showPopup(cookTimeTextView) }

        saveRecipeButton.setOnClickListener {
            val recipeNameText = recipeName.text.toString().trim()
            val recipeDescText = recipeDesc.text.toString().trim()
            val recipeInstructionsText = recipeInstructions.text.toString().trim()
            val servingsText = servingsView.text.toString().trim()
            val minutesText = minutesView.text.toString().trim()

            if (recipeNameText.isNotEmpty() && recipeDescText.isNotEmpty() &&
                recipeInstructionsText.isNotEmpty() && servingsText.isNotEmpty() &&
                minutesText.isNotEmpty() && ingredientList.isNotEmpty()) {

                val servings = servingsText.toIntOrNull() ?: 0
                val readyInMinutes = minutesText.replace(" min", "").toIntOrNull() ?: 0

                val customCreateRecipe = CustomCreateRecipe(
                    recipeNameText,
                    imageFilePath ?: "", // Use imageFilePath if available, otherwise empty string
                    recipeDescText,
                    servings,
                    readyInMinutes,
                    recipeInstructionsText,
                    ingredientList
                )

                // Now you can use the customCreateRecipe object as needed (e.g., save to database)
                // For demonstration, you can log the recipe details
                Log.d("CreateRecipeActivity", "CustomCreateRecipe: $customCreateRecipe")

            } else {
                Toast.makeText(
                    this,
                    "Please fill out all required fields to save the recipe.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun extractNumberAndUnit(input: String): Pair<Double, String> {
        val trimmedInput = input.trim()
        val lastSpaceIndex = trimmedInput.lastIndexOf(' ')
        return if (lastSpaceIndex != -1 && lastSpaceIndex < trimmedInput.length - 1) {
            val numberString = trimmedInput.substring(0, lastSpaceIndex).trim()
            val unit = trimmedInput.substring(lastSpaceIndex + 1).trim()
            val number = numberString.toDoubleOrNull() ?: 0.0
            Pair(number, unit)
        } else {
            val number = trimmedInput.toDoubleOrNull() ?: 0.0
            Pair(number, "units")
        }
    }

    private fun dispatchTakePictureIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap?
            imageBitmap?.let {
                imageFilePath = saveImageToFile(it)
                addImage.setBackgroundResource(R.drawable.transparent_background)
                addImage.setImageBitmap(it)
            }
        }
    }

    private fun saveImageToFile(bitmap: Bitmap): String? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "$IMAGE_FILE_NAME_PREFIX$timeStamp.jpg"
            val storageDir = getExternalFilesDir(null)
            val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)

            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            imageFile.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun showPopup(anchorView: View) {
        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_adjust_values, null)
        val popupWindow = PopupWindow(
            popupView,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )

        val editServes = popupView.findViewById<EditText>(R.id.editServes)
        val editCookTime = popupView.findViewById<EditText>(R.id.editCookTime)
        val saveButton = popupView.findViewById<Button>(R.id.saveButton)

        if (anchorView == servesTextView) {
            editServes.setText(servingsView.text)
            editCookTime.setText(minutesView.text.toString().replace(" min", ""))
        } else if (anchorView == cookTimeTextView) {
            editServes.setText(servingsView.text)
            editCookTime.setText(minutesView.text.toString().replace(" min", ""))
        }

        saveButton.setOnClickListener {
            servingsView.text = editServes.text
            minutesView.text = "${editCookTime.text} min"
            popupWindow.dismiss()
        }

        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0)
    }
}