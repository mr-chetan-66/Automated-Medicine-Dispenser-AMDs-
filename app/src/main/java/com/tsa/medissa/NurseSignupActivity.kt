package com.tsa.medissa

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class NurseSignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nurse_signup)
        findViewById<MaterialButton>(R.id.registerButton).setOnClickListener {
            val intent = Intent(this, NurseStart::class.java)
            startActivity(intent)
        }
        findViewById<TextView>(R.id.nurse_already).setOnClickListener {
            val intent = Intent(this, NurseLoginActivity::class.java)
            startActivity(intent)
        }
    }
}