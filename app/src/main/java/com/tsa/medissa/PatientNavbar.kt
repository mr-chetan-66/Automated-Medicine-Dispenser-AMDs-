package com.tsa.medissa

import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class PatientNavbar : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    private var backPressedTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.patient_navbar)
        Toast.makeText(this, "Welcome to Medissa!", Toast.LENGTH_SHORT).show()
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        loadFragment(PatientProfile())
        // Handle bottom navigation item selection
        bottomNavigationView.setOnItemSelectedListener { item ->
            val selectedFragment: Fragment = when (item.itemId) {
                R.id.nav_noti -> PatientNotification()
                else -> PatientProfile()
            }
            loadFragment(selectedFragment)
            true
        }
        handleBackPress()
    }

    private var profileFragment: PatientProfile? = null

    private fun loadFragment(newFragment: Fragment) {
        val fragmentToLoad: Fragment = if (newFragment is NurseDashboardFragment && profileFragment != null) {
            profileFragment!! // Safe to use !! here because we checked for null
        } else {
            if (newFragment is PatientProfile) profileFragment = newFragment
            newFragment // This is guaranteed to be non-null
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragmentToLoad)
            .commit()
    }

    private fun handleBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentTime = System.currentTimeMillis()

                // If back button is pressed within 2 seconds, exit the app
                if (currentTime - backPressedTime <= 2000) {
                    finish() // Exit the app
                } else {
                    // Show a Toast asking for confirmation
                    Toast.makeText(
                        this@PatientNavbar,
                        "Press back again to exit",
                        Toast.LENGTH_SHORT
                    ).show()
                    backPressedTime = currentTime
                }
            }
        })
    }
}