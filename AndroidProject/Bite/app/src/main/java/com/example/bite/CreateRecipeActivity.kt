package com.example.bite

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_recipe)

        addImage = findViewById(R.id.addImage)

        addImage.setOnClickListener {
            dispatchTakePictureIntent()
        }
    }

    private fun dispatchTakePictureIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            // Retrieve the captured image (Bitmap) from the intent
            val imageBitmap = data?.extras?.get("data") as Bitmap
            // Save the captured image to a file and get the file path
            imageFilePath = saveImageToFile(imageBitmap)
            // Display the captured image in the ImageView
            addImage.setImageBitmap(imageBitmap)
        }
    }

    private fun saveImageToFile(bitmap: Bitmap): String? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "$IMAGE_FILE_NAME_PREFIX$timeStamp.jpg"
            val storageDir = getExternalFilesDir(null) // Use getFilesDir() for internal storage
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
}