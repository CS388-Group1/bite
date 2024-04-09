package com.example.bite

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bite.daos.UserPreferencesDao
import com.example.bite.models.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DietPrefsActivity : AppCompatActivity() {

    private lateinit var userPreferencesDao: UserPreferencesDao
    private val frameLayoutIds = arrayOf(R.id.noneButton, R.id.veganButton, R.id.vegetarianButton, R.id.ketoButton, R.id.glutenButton, R.id.pescetarianButton, R.id.paleoButton, R.id.primalButton)
    private val buttonIds = arrayOf(R.id.dairy, R.id.peanut, R.id.soy, R.id.egg, R.id.seafood, R.id.sulfite, R.id.gluten, R.id.sesame, R.id.treeNut, R.id.grain, R.id.shellfish, R.id.wheat)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dietary_prefs)

        // Obtain the instance of the database
        val appDatabase = AppDatabase.getInstance(applicationContext)
        userPreferencesDao = appDatabase.userPreferencesDao()

        // Retrieve user preferences from Room database
        GlobalScope.launch(Dispatchers.IO) {
            var userPreferences = userPreferencesDao.getUserPreferences()
            if (userPreferences == null) {
                // If preferences are null, create a new instance with default values
                userPreferences = UserPreferences()
                userPreferencesDao.insertUserPreferences(userPreferences)
            }

            runOnUiThread {
                // Update UI based on user preferences
                // For dietary restrictions
                val dietaryRestrictions = userPreferences.dietaryRestrictions.split(",")
                Log.d("DietaryRestrictions", "Current dietary restrictions: $dietaryRestrictions")
                for (frameLayoutId in frameLayoutIds) {
                    val frameLayout = findViewById<FrameLayout>(frameLayoutId)
                    val textView = frameLayout.getChildAt(1) as? TextView
                    val restrictionText = textView?.text?.toString() ?: ""
                    frameLayout.tag = restrictionText
                    frameLayout.isSelected = dietaryRestrictions.contains(restrictionText)
                }
                // For allergies
                val allergies = userPreferences.allergies.split(",")
                Log.d("Allergies", "Current allergies: $allergies")
                for (buttonId in buttonIds) {
                    val button = findViewById<Button>(buttonId)
                    button.isSelected = allergies.contains(button.text.toString())
                }
            }
        }

        // Handle click events for dietary restrictions
        handleClicks()

        // Handle click events for allergies
        handleAllergiesClick()
    }



    private fun handleClicks() {
        val frameLayouts = frameLayoutIds.map { findViewById<FrameLayout>(it) }
        for (frameLayout in frameLayouts) {
            frameLayout.setOnClickListener {
                // Toggle the isSelected state of the clicked FrameLayout
                it.isSelected = !it.isSelected
                // Update user preferences based on the current selected items
                updateUserPreferences(getSelectedRestrictions(frameLayouts))
            }
        }

    }

    private fun getSelectedRestrictions(frameLayouts: List<FrameLayout>): String {
        val selectedRestrictions = mutableListOf<String>()
        for (layout in frameLayouts) {
            if (layout.isSelected) {
                val textView = layout.getChildAt(1) as? TextView
                val restrictionText = textView?.text?.toString() ?: ""
                layout.tag = restrictionText
                selectedRestrictions.add(restrictionText)
            }
        }
        return selectedRestrictions.joinToString(",")
    }


    private fun handleAllergiesClick() {
        val buttons = buttonIds.map { findViewById<Button>(it) }
        for (buttonId in buttonIds) {
            val button = findViewById<Button>(buttonId)
            button.setOnClickListener {
                it.isSelected = !it.isSelected
                val selectedAllergies = mutableListOf<String>()
                for (button in buttons) {
                    if (button.isSelected) {
                        selectedAllergies.add(button.text.toString())
                    }
                }
                updateUserPreferences(allergies = selectedAllergies.joinToString(","))
            }
        }
    }

    private fun updateUserPreferences(dietaryRestrictions: String = "", allergies: String = "") {
        GlobalScope.launch(Dispatchers.IO) {
            val userPreferences = userPreferencesDao.getUserPreferences() ?: UserPreferences() // If null, create a new instance
            userPreferences.dietaryRestrictions = dietaryRestrictions
            userPreferences.allergies = allergies
            userPreferencesDao.updateUserPreferences(userPreferences)
        }
    }
}
