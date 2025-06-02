package com.tsa.medissa

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class DoctorSignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.doc_signup)
        findViewById<MaterialButton>(R.id.registerButton).setOnClickListener {
            val intent = Intent(this, DoctorStart::class.java)
            startActivity(intent)
        }
        findViewById<TextView>(R.id.doc_already).setOnClickListener {
            val intent = Intent(this, DoctorLoginActivity::class.java)
            startActivity(intent)
        }
    }
}