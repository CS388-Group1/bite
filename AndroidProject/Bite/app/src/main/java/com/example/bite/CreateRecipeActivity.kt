package com.example.bite

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
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
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bite.adapters.CustomIngredientAdapter
import com.example.bite.models.CustomCreateIngredient
import com.example.bite.models.CustomCreateRecipe
import com.example.bite.models.CustomRecipeViewModel
import com.example.bite.network.SyncWithFirebase
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


class CreateRecipeActivity : AppCompatActivity() {

    private lateinit var addImage: ImageView
    private var imageFilePath: String? = null
    private val REQUEST_IMAGE_PICK = 101
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
    private lateinit var customRecipeViewModel: CustomRecipeViewModel
    private lateinit var backToHome: ImageView
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_recipe)

        customRecipeViewModel = ViewModelProvider(this).get(CustomRecipeViewModel::class.java)

        addImage = findViewById(R.id.addImage)
        servesTextView = findViewById(R.id.addServes)
        cookTimeTextView = findViewById(R.id.addCookTime)
        servingsView = findViewById(R.id.servings)
        minutesView = findViewById(R.id.minutes)
        recipeName = findViewById(R.id.addRecipeName)
        recipeDesc = findViewById(R.id.addRecipeDesc)
        recipeInstructions = findViewById(R.id.addInstructions)
        saveRecipeButton = findViewById(R.id.saveRecipeButton)
        backToHome = findViewById(R.id.backToHomeButton)

        backToHome.setOnClickListener{
            val intent = Intent(this@CreateRecipeActivity, MainActivity::class.java)
            startActivity(intent)
        }

        addImage.setOnClickListener {
            showImageSourceChooser()
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

                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val customCreateRecipe = CustomCreateRecipe(
                    userId,
                    recipeNameText,
                    imageFilePath ?: "",
                    recipeDescText,
                    servings,
                    readyInMinutes,
                    recipeInstructionsText,
                    ingredientList
                )


                // Log Recipe Details
                Log.d("CreateRecipeActivity", "CustomCreateRecipe: $customCreateRecipe")
                Toast.makeText(this, "Recipe Successfully Created.", Toast.LENGTH_SHORT).show()
                //Save to Room
                customRecipeViewModel.insertCustomCreateRecipe(customCreateRecipe)

                // Delay to allow Room to save data
                Thread.sleep(2000)

                //Sync to Firebase
                val networkAvailable = isInternetAvailable(this)
                if (networkAvailable) {
                    Log.v("NetworkConn---->", "Available")
                    database = AppDatabase.getInstance(this)
                    val customRecipeDao = database.customRecipeDao()
                    val syncWithFirebase = SyncWithFirebase(customRecipeDao)
                    val auth : FirebaseAuth = FirebaseAuth.getInstance()
                    val userId = auth.currentUser?.uid
                    Log.v("userId---->", userId.toString())
                    if (userId != null) {
                        syncWithFirebase.syncRecipesWithFirestore(userId)
                    }

                } else {
                    Log.e("NetworkConn----->", "Network Unavailable.")
                }

                val intent = Intent(this@CreateRecipeActivity, MainActivity::class.java)
                startActivity(intent)

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

    private fun showImageSourceChooser() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Image Source")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> requestCameraPermission()
                1 -> requestGalleryPermission()
            }
            dialog.dismiss()
        }
        builder.show()
    }

    private fun requestGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(READ_MEDIA_IMAGES),
                    REQUEST_IMAGE_PICK
                )
            } else {
                openGallery()
            }
        }else
        {
            if (ContextCompat.checkSelfPermission(
                    this,
                    READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(READ_EXTERNAL_STORAGE),
                    REQUEST_IMAGE_PICK
                )
            } else {
                openGallery()
            }
        }
    }
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_IMAGE_CAPTURE)
        } else {
            dispatchTakePictureIntent()
        }
    }

    private fun dispatchTakePictureIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent()
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_IMAGE_PICK -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap?
                    imageBitmap?.let {
                        val imagePath = saveImageToFile(it)
                        addImage.setBackgroundResource(R.drawable.transparent_background)
                        addImage.setImageBitmap(it)
                        imagePath?.let { convertImage(it) }
                    }
                }
                REQUEST_IMAGE_PICK -> {
                    val selectedImageUri = data?.data
                    selectedImageUri?.let {
                        val imagePath = getImagePathFromUri(selectedImageUri)
                        imagePath?.let{
                            val bitmap = BitmapFactory.decodeFile(imagePath)
                            addImage.setBackgroundResource(R.drawable.transparent_background)
                            addImage.setImageBitmap(bitmap)
                            convertImage(it)
                        }
                    }
                }
            }
        }
    }

    private fun getImagePathFromUri(uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            it.moveToFirst()
            val columnIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            return it.getString(columnIndex)
        }
        return null
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

    private fun convertImage(imgPath: String) {
        Thread {
            val base64String = encodeFileToBase64(imgPath)
            base64String?.let {
                runOnUiThread {
                    imageFilePath = base64String
                }
            }
        }.start()
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun encodeFileToBase64(filePath: String): String? {
        return try {
            val file = File(filePath)
            val fileInputStream = FileInputStream(file)
            val bytes = fileInputStream.readBytes()
            fileInputStream.close()
            val base64String = Base64.encode(bytes, 0, bytes.size)
            "data:image/jpeg;base64,$base64String"
        } catch (e: Exception) {
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
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return connectivityManager?.activeNetworkInfo?.isConnected ?: false
    }

}
