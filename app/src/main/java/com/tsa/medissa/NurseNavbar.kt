package com.tsa.medissa

import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.android.material.bottomnavigation.BottomNavigationView

class NurseNavbar : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private var backPressedTime = 0L
    private var homeFragment: NurseDashboardFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nurse_navbar)

        Toast.makeText(this, "Welcome to Medissa!", Toast.LENGTH_SHORT).show()
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        // Load default home fragment
        loadFragment(NurseDashboardFragment())

        // Setup bottom navigation
        bottomNavigationView.setOnItemSelectedListener {
            val selectedFragment = when (it.itemId) {
                R.id.nav_home -> NurseDashboardFragment()
                R.id.nav_dr -> DrDr()
                R.id.nav_profile -> PatientProfile()
                R.id.nav_noti -> NotiDoc()
                else -> NurseDashboardFragment()
            }
            loadFragment(selectedFragment)
            true
        }

        handleBackPress()
    }

    private fun loadFragment(newFragment: Fragment) {
        val fragmentToLoad = if (newFragment is NurseDashboardFragment && homeFragment != null) {
            homeFragment!!
        } else {
            if (newFragment is NurseDashboardFragment) homeFragment = newFragment
            newFragment
        }

        supportFragmentManager.commit {
            replace(R.id.fragment_container, fragmentToLoad)
        }
    }

    private fun handleBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentTime = System.currentTimeMillis()
                if (currentTime - backPressedTime <= 2000) {
                    finish()
                } else {
                    Toast.makeText(
                        this@NurseNavbar,
                        "Press back again to exit",
                        Toast.LENGTH_SHORT
                    ).show()
                    backPressedTime = currentTime
                }
            }
        })
    }
}
