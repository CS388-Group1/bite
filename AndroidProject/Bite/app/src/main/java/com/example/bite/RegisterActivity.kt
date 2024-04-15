package com.example.bite

import android.R
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MotionEvent
import android.view.View.INVISIBLE
import android.view.View.OnTouchListener
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.bite.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.tapadoo.alerter.Alerter


class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var fStore: FirebaseFirestore

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()

        fun containsNumber(str: String): Boolean {
            val pattern = ".*\\d.*".toRegex()
            return pattern.matches(str)
        }

        //Sign Up Logic
        binding.signUpButton.setOnClickListener{
            val email = binding.editTextTextEmailAddressInit.text.toString()
            val password = binding.editTextTextPasswordInit.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && password.length >=8 && containsNumber(password)){
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{
                    if (it.isSuccessful) {
                        val userId = auth.currentUser?.uid // Get current user's UID
                        userId?.let { uid ->
                            val docRef = fStore.collection("users").document(uid)
                            val userInfo = hashMapOf(
                                "email" to email
                                // Add more user information if needed later
                            )
                            docRef.set(userInfo)
                                .addOnSuccessListener {
                                    Log.d(
                                        "Firestore",
                                        "Account created for user: " + userId.toString()
                                    )
                                }

                            .addOnFailureListener { e ->
                                Log.e(
                                    "Firestore",
                                    "Error storing login info for user: " + userId.toString()
                                )
                            }
                    }
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        //TODO: RegisterActivity will start Onboarding Activity once created. For now, RegisterActivity routes back to LoginActivity.
                    }
                    else{
                        Alerter.create(this)
                            .setTitle("Bite: Error")
                            .setText("Failed to register with credentials.")
                            .setBackgroundColorRes(com.example.bite.R.color.red)
                            .setDuration(5000)
                            .show()
                    }
                }
            }
            else{
                Alerter.create(this)
                    .setTitle("Bite: Error")
                    .setText("Please enter a valid username and password.")
                    .setBackgroundColorRes(com.example.bite.R.color.red)
                    .setDuration(5000)
                    .show()
            }
        }

        val passwordEditText = binding.editTextTextPasswordInit

        //Toggle Password Visibility
        passwordEditText.setOnTouchListener(OnTouchListener { v, event ->
            val DRAWABLE_END = 2
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= passwordEditText.right - passwordEditText.compoundDrawables[DRAWABLE_END].bounds.width()) {
                    if (passwordEditText.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                        passwordEditText.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    } else {
                        passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    }
                    return@OnTouchListener true
                }
            }
            false
        })

        val grayCheck1 = binding.emptyCheckIcon1
        val grayCheck2 = binding.emptyCheckIcon2
        val greenCheck1 = binding.filledCheckIcon1
        val greenCheck2 = binding.filledCheckIcon2

        //Password Requirement Logic
        passwordEditText.addTextChangedListener{
            val s = passwordEditText.text.toString()
                 if(s != "" && s.length >= 8){
                     grayCheck1.visibility = INVISIBLE
                     greenCheck1.visibility = VISIBLE
                 }
                else {
                     grayCheck1.visibility = VISIBLE
                     greenCheck1.visibility = INVISIBLE
                 }
                if (containsNumber(s)){
                    grayCheck2.visibility = INVISIBLE
                    greenCheck2.visibility = VISIBLE
                }
                else{
                    grayCheck2.visibility = VISIBLE
                    greenCheck2.visibility = INVISIBLE
                }

        }

    }
}