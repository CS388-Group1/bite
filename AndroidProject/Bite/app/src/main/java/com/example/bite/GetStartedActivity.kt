package com.example.bite

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class GetStartedActivity : AppCompatActivity() {
    private lateinit var getStartedButton: Button
    val SHARED_PREFS: String = "sharedPrefs"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_started)

        autoLogin()

        getStartedButton = findViewById(R.id.getStartedButton)
        getStartedButton.setOnClickListener{
            startActivity(Intent(this@GetStartedActivity, LoginActivity::class.java))
        }
    }

    private fun autoLogin() {
        val sharedPreferences : SharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        val check: String? = sharedPreferences.getString("name", "")
        // if logged in, bypass auth
        if(check.equals("true")){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}