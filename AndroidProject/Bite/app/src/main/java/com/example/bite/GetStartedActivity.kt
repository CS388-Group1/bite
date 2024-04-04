package com.example.bite

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class GetStartedActivity : AppCompatActivity() {
    private lateinit var getStartedButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_started)

        getStartedButton = findViewById(R.id.getStartedButton)
        getStartedButton.setOnClickListener{
            startActivity(Intent(this@GetStartedActivity, LoginActivity::class.java))
        }
    }
}