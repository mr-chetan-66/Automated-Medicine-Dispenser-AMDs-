package com.tsa.medissa

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class NurseLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nurse_login)
        findViewById<MaterialButton>(R.id.loginButton).setOnClickListener {
            val intent = Intent(this, DoctorStart::class.java)
            startActivity(intent)
        }
    }
}
