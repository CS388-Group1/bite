package com.example.bite

import android.R
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
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


class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        fun containsNumber(str: String): Boolean {
            val pattern = ".*\\d.*".toRegex()
            return pattern.matches(str)
        }

        binding.signUpButton.setOnClickListener{
            val email = binding.editTextTextEmailAddressInit.text.toString()
            val password = binding.editTextTextPasswordInit.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && password.length >=8 && containsNumber(password)){
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{
                    if (it.isSuccessful){
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        //TODO: RegisterActivity will start Onboarding Activity once created. For now, RegisterActivity routes back to LoginActivity.
                    }
                    else{
                        Toast.makeText(this, "Failed to register with credentials.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else{
                Toast.makeText(this, "Please enter a valid username and password.", Toast.LENGTH_LONG).show()
            }
        }

        val passwordEditText = binding.editTextTextPasswordInit
        passwordEditText.setOnTouchListener(OnTouchListener { v, event ->
            val DRAWABLE_END = 2
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= passwordEditText.right - passwordEditText.compoundDrawables[DRAWABLE_END].bounds.width()) {
                    // Toggle password visibility
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