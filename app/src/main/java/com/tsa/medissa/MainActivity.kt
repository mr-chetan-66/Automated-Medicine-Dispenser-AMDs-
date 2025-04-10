package com.tsa.medissa

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.usertype)

        findViewById<MaterialCardView>(R.id.cv_doctor).setOnClickListener {
            val intent = Intent(this, DoctorSignupActivity::class.java)
            startActivity(intent)
        }

        // Nurse card click listener
        findViewById<MaterialCardView>(R.id.cv_nurse).setOnClickListener {
            val intent = Intent(this, NurseSignupActivity::class.java)
            startActivity(intent)
        }

        // Patient card click listener
        findViewById<MaterialCardView>(R.id.cv_patient).setOnClickListener {
            val intent = Intent(this, PatientLoginActivity::class.java)
            startActivity(intent)
        }

    }
}
