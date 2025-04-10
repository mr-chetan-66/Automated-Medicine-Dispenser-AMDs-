package com.tsa.medissa

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class DoctorStart : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.doctor_start)
        findViewById<MaterialButton>(R.id.btnDoctorStart).setOnClickListener {
            val intent = Intent(this, NotiDoc::class.java)
            startActivity(intent)
        }
    }
}