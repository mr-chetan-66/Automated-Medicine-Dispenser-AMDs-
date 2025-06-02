package com.tsa.medissa

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class PatientStart : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.patient_start)
        findViewById<MaterialButton>(R.id.btnPatientStart).setOnClickListener {
            val intent = Intent(this, PatientNavbar::class.java)
            startActivity(intent)
        }
    }
}
