package com.example.bite

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentContainerView
import com.example.bite.network.SpoonacularRepository
import com.facebook.shimmer.ShimmerFrameLayout
import com.tapadoo.alerter.Alerter
import org.json.JSONObject

class ScanRecipeActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
    }

    private lateinit var repository: SpoonacularRepository
    private lateinit var shimmerLayout: ShimmerFrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_recipe)
        repository = SpoonacularRepository()
        requestCameraPermission()

        showAnalyzingOverlay(true)
        shimmerLayout = findViewById(R.id.shimmer_layout)
        shimmerLayout.startShimmer()

        val exitButton = findViewById<ImageButton>(R.id.back)
        exitButton.setOnClickListener {
            finish()
        }
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
                Alerter.create(this)
                    .setTitle("Bite: Error")
                    .setText("Camera permission denied")
                    .setBackgroundColorRes(com.example.bite.R.color.red)
                    .setDuration(5000)
                    .show()
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
                Alerter.create(this)
                    .setTitle("Bite: Error")
                    .setText("Failed to select image")
                    .setBackgroundColorRes(com.example.bite.R.color.red)
                    .setDuration(5000)
                    .show()
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
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                uploadImage(it)
            } ?: run {
                Alerter.create(this)
                    .setTitle("Bite: Error")
                    .setText("Failed to capture image")
                    .setBackgroundColorRes(com.example.bite.R.color.red)
                    .setDuration(5000)
                    .show()
            }
        }
    }

    private fun uploadImage(imageBitmap: Bitmap) {
        updateProcessingText("Processing Image", "Hang tight! We’re analyzing your photo to identify the food")
        showAnalyzingOverlay(true)
        updateAppBarTitle("Analyzing...", "")
        repository.uploadImage(imageBitmap, object : SpoonacularRepository.UploadCallback {
            override fun onSuccess(result: String) {
                runOnUiThread {
                    showAnalyzingOverlay(false)
                    shimmerLayout.stopShimmer()
                    shimmerLayout.visibility = View.GONE
                    findViewById<FragmentContainerView>(R.id.fragment_container).visibility = View.VISIBLE

//                    Alerter.create(this@ScanRecipeActivity)
//                        .setTitle("Bite: Success")
//                        .setText("Image uploaded successfully")
//                        .setBackgroundColorRes(R.color.green)
//                        .setDuration(3000)
//                        .show()

                    val category = parseCategoryFromResult(result)
                    val fragment = ScanResultsFragment.newInstance(category)

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit()
                }
            }

            override fun onFailure(error: String) {
                runOnUiThread {
                    showAnalyzingOverlay(false)
                    shimmerLayout.stopShimmer()
                    shimmerLayout.visibility = View.GONE
                    Alerter.create(this@ScanRecipeActivity)
                        .setTitle("Bite: Error")
                        .setText(error)
                        .setBackgroundColorRes(com.example.bite.R.color.red)
                        .setDuration(5000)
                        .show()
                }
            }
        })
    }

    private fun parseCategoryFromResult(result: String): String {
        try {
            val jsonObject = JSONObject(result)
            val choices = jsonObject.getJSONArray("choices")
            if (choices.length() > 0) {
                val firstChoice = choices.getJSONObject(0)
                val message = firstChoice.getJSONObject("message")
                val content = message.getString("content")
                return content.trim()
            }
        } catch (e: Exception) {
            Log.e("ScanRecipeActivity", "Error parsing JSON result", e)
        }
        return "Unknown"
    }

    private fun showAnalyzingOverlay(show: Boolean) {
        val analyzingOverlay = findViewById<View>(R.id.analyzingOverlay)
        analyzingOverlay.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun updateAppBarTitle(title: String, subtitle: String) {
        findViewById<TextView>(R.id.titleScanResults).text = title
        findViewById<TextView>(R.id.subtitleScanResults).text = subtitle
    }

    fun updateProcessingText(text: String, subtitle: String) {
        findViewById<TextView>(R.id.processingText).text = text
        findViewById<TextView>(R.id.subProcessingText).text = text
    }
}