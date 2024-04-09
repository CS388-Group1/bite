package com.example.bite

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PreferencesActivity : AppCompatActivity() {

        private lateinit var backToHomeButton: ImageView
        private lateinit var logOutButton: TextView
        private lateinit var userEmailLabel: TextView
        private lateinit var auth: FirebaseAuth
        private lateinit var fStore: FirebaseFirestore
        private lateinit var dietPrefsButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences)


        auth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()
            val userId = auth.currentUser?.uid
            var userEmail: String? = null
            userId?.let { uid ->
                val docRef = fStore.collection("users").document(uid)
                docRef.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        // Handle error
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        userEmail = snapshot.getString("email")
                        userEmailLabel.text = "(Logged in as: " + userEmail + ")"
                    }
                }
            }


        backToHomeButton = findViewById(R.id.backToHomeButton)
        logOutButton = findViewById(R.id.logOutButton)


        backToHomeButton.setOnClickListener {
            val intent = Intent(this@PreferencesActivity, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)

        }

            logOutButton.setOnClickListener {
                auth.signOut()
                val intent = Intent(this@PreferencesActivity, LoginActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }

        //TODO: Dietary Preference and My Recipes routing
        dietPrefsButton = findViewById<TextView>(R.id.dietaryPreferencesButton)
        dietPrefsButton.setOnClickListener{
            startActivity(Intent(this@PreferencesActivity, DietPrefsActivity::class.java))
        }
    }

        override fun onBackPressed() {
            super.onBackPressed()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

