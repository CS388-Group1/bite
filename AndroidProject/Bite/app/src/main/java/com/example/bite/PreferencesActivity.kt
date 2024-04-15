package com.example.bite

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PreferencesActivity : AppCompatActivity() {
    private lateinit var backToHomeButton: ImageView
    private lateinit var logOutButton: TextView
    private lateinit var userEmailLabel: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var fStore: FirebaseFirestore
    private lateinit var dietPrefsButton: TextView
    private lateinit var myRecipesButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences)

        userEmailLabel = findViewById(R.id.userEmailTextView)
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
        dietPrefsButton = findViewById(R.id.dietaryPreferencesButton)
        myRecipesButton = findViewById(R.id.myRecipesButton)

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

        dietPrefsButton.setOnClickListener {
            startActivity(Intent(this@PreferencesActivity, DietPrefsActivity::class.java))
        }

        myRecipesButton.setOnClickListener {
            startActivity(Intent(this@PreferencesActivity, MyRecipesActivity::class.java))
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}