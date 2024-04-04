package com.example.bite

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class PreferencesActivity : AppCompatActivity() {
        private lateinit var backToHomeButton: ImageView
        private lateinit var logOutButton: TextView
        private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences)
        auth = FirebaseAuth.getInstance()

        backToHomeButton = findViewById(R.id.backToHomeButton)
        logOutButton = findViewById(R.id.logOutButton)

            backToHomeButton.setOnClickListener{
                startActivity(Intent(this@PreferencesActivity, MainActivity::class.java))
                }
            logOutButton.setOnClickListener {
                auth.signOut()
                startActivity(Intent(this@PreferencesActivity, LoginActivity::class.java))
            }
        //TODO: Dietary Preference and My Recipes routing
            }

        }
