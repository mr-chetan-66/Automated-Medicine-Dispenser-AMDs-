package com.tsa.medissa

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class PatientLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.patient_login)
        findViewById<MaterialButton>(R.id.loginButton).setOnClickListener {
            val intent = Intent(this, PatientStart::class.java)
            startActivity(intent)
        }
    }
}