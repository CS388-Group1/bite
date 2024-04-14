package com.example.bite

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.bite.network.SpoonacularRepository
import org.json.JSONObject

class ScanRecipeActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
    }

    private lateinit var repository: SpoonacularRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = SpoonacularRepository()
        requestCameraPermission()
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_IMAGE_CAPTURE)
        } else {
            dispatchImageIntent()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchImageIntent()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let {
                val imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, it)
                uploadImage(imageBitmap)
            } ?: run {
                Toast.makeText(this, "Failed to select image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun dispatchImageIntent() {
        val options = arrayOf("Take a picture", "Choose from library")
        AlertDialog.Builder(this)
            .setTitle("Select Image Source")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        takePictureLauncher.launch(takePictureIntent)
                    }
                    1 -> {
                        val selectImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        selectImageLauncher.launch(selectImageIntent)
                    }
                }
            }
            .show()
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                uploadImage(it)
            } ?: run {
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureLauncher.launch(takePictureIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                uploadImage(it)
            } ?: run {
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImage(imageBitmap: Bitmap) {
        repository.uploadImage(imageBitmap, object : SpoonacularRepository.UploadCallback {
            override fun onSuccess(result: String) {
                runOnUiThread {
                    val category = parseCategoryFromResult(result)
                    Intent(this@ScanRecipeActivity, SearchActivity::class.java).apply {
                        putExtra("search_query", category)
                        startActivity(this)
                    }
                    Toast.makeText(this@ScanRecipeActivity, "Category: $category", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(error: String) {
                runOnUiThread {
                    Toast.makeText(this@ScanRecipeActivity, error, Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun parseCategoryFromResult(result: String): String {
        try {
            // It returns Image classified successfully: { thing in here } So we have to take the substr
            val jsonPart = result.substringAfter(":")
            val jsonObject = JSONObject(jsonPart)
            return jsonObject.getString("category") // Extracting the category field ex. {"category":"brownies"}
        } catch (e: Exception) {
            Log.e("ScanRecipeActivity", "Error parsing JSON result", e)
            return "Unknown" // If it errors, search unknown. Can also fallback to something like burger.
        }
    }
}