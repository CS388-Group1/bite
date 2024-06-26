package com.example.bite

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
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
import com.google.firebase.firestore.FirebaseFirestore
import com.tapadoo.alerter.Alerter

class LoginActivity : AppCompatActivity() {

    private lateinit var binding:ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var fStore: FirebaseFirestore
    private lateinit var googleSignInClient : GoogleSignInClient
    val SHARED_PREFS: String = "sharedPrefs"

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // FOR TESTING: Pre-fill login fields for testing. Remove for production.
//        binding.editTextTextEmailAddress.setText("test@test.com")
//        binding.editTextTextPassword.setText("testing123")

        auth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()

        autoLogin()

        binding.signUpLabel.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }


        // FOR TESTING: Automatically attempt to log in
//        val email = binding.editTextTextEmailAddress.text.toString()
//        val password = binding.editTextTextPassword.text.toString()
//        if (email.isNotEmpty() && password.isNotEmpty()) {
//            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    val intent = Intent(this, MainActivity::class.java)
//                    startActivity(intent)
//                } else {
//                    Toast.makeText(this, "Invalid login credentials.", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
        // Remove above code for production

        //Login Logic
        binding.loginButton.setOnClickListener{
            val email = binding.editTextTextEmailAddress.text.toString()
            val password = binding.editTextTextPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()){
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener{
                    if (it.isSuccessful){

                        //initialize auto-login
                        val sharedPreferences: SharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
                        val editor : SharedPreferences.Editor = sharedPreferences.edit()
                        editor.putString("name", "true")
                        editor.apply()

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }
                    else{
                        Alerter.create(this)
                            .setTitle("Bite: Error")
                            .setText("Invalid login credentials.")
                            .setBackgroundColorRes(R.color.red)
                            .setDuration(5000)
                            .show()
                    }
                }
            }
            else{
                Alerter.create(this)
                    .setTitle("Bite: Error")
                    .setText("Please enter an email and password.")
                    .setBackgroundColorRes(R.color.red)
                    .setDuration(5000)
                    .show()
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
        val passwordVisibilityIcon = binding.passwordVisibilityIcon

        // Allow user to press "Enter" on keyboard to login (for convenience when testing)
        binding.editTextTextPassword.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.loginButton.performClick()
                true
            } else {
                false
            }
        }

        passwordVisibilityIcon.setOnClickListener {
            if (passwordEditText.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                passwordVisibilityIcon.setImageResource(R.drawable.baseline_remove_red_eye_24)
            } else {
                passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                passwordVisibilityIcon.setImageResource(R.drawable.baseline_remove_red_eye_24)
            }
            passwordEditText.setSelection(passwordEditText.text.length)
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
            Alerter.create(this)
                .setTitle("Bite: Error")
                .setText(task.exception.toString())
                .setBackgroundColorRes(com.example.bite.R.color.red)
                .setDuration(5000)
                .show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener{
            if (it.isSuccessful){
                val userId = auth.currentUser?.uid
                userId?.let { uid ->
                    val docRef = fStore.collection("users").document(uid)
                    val userInfo = hashMapOf(
                        "email" to account.email
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
                //initialize auto login
                val sharedPreferences: SharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
                val editor : SharedPreferences.Editor = sharedPreferences.edit()
                editor.putString("name", "true")
                editor.apply()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            else{
                Alerter.create(this)
                    .setTitle("Bite: Error")
                    .setText("Unable to sign in with Google Authentication.")
                    .setBackgroundColorRes(com.example.bite.R.color.red)
                    .setDuration(5000)
                    .show()
            }
        }
    }
}