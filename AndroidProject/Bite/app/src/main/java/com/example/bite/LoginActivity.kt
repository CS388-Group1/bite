package com.example.bite

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.bite.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.tasks.Task

class LoginActivity : AppCompatActivity() {

    private lateinit var binding:ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient : GoogleSignInClient

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        binding.signUpLabel.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        //Login Logic
        binding.loginButton.setOnClickListener{
            val email = binding.editTextTextEmailAddress.text.toString()
            val password = binding.editTextTextPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()){
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener{
                    if (it.isSuccessful){
                        val intent = Intent(this, DashboardActivity::class.java)
                        intent.putExtra("email", email)
                        startActivity(intent)
                    }
                    else{
                        Toast.makeText(this, "Invalid login credentials.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else{
                Toast.makeText(this, "Please enter an email and password.", Toast.LENGTH_SHORT).show()
            }
        }

        //Google Sign In Logic
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) //Web Client Id is generated
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.loginGoogleButton.setOnClickListener{
            signInGoogle()
        }


        val passwordEditText = binding.editTextTextPassword

        //Toggle Password Visibility
        passwordEditText.setOnTouchListener(View.OnTouchListener { v, event ->
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
    }

    //Google Sign In Functions
    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
            if (result.resultCode == Activity.RESULT_OK){
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleResults(task)
            }
    }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful){
            val account : GoogleSignInAccount? = task.result
            if (account != null){
                updateUI(account)
            }
        }
        else{
            Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener{
            if (it.isSuccessful){
                val intent = Intent(this, DashboardActivity::class.java)
                intent.putExtra("email", account.email)
                startActivity(intent)
            }
            else{
                Toast.makeText(this, "Unable to sign in with Google Authentication.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}