package com.tsa.medissa

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class NurseStart : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nurse_start)
        Toast.makeText(this, "Welcome Nurse!", Toast.LENGTH_SHORT).show()

        findViewById<MaterialButton>(R.id.btnNurseStart).setOnClickListener {
            val intent = Intent(this, NurseNavbar::class.java)
            startActivity(intent)
            finish() // ✅ Prevents going back to NurseStart
        }
    }
}