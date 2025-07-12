package com.tsa.medissa

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore

class PatientLoginActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.patient_login)

        firestore = FirebaseFirestore.getInstance()

        val patientIdEditText = findViewById<EditText>(R.id.patientIdEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<MaterialButton>(R.id.loginButton)

        loginButton.setOnClickListener {
            val patientId = patientIdEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (patientId.isEmpty() || password.isEmpty()) {
                showToast("Please fill all fields")
                return@setOnClickListener
            }

            firestore.collection("users")
                .whereEqualTo("role", "patient")
                .whereEqualTo("patientID", patientId)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        showToast("Patient not found")
                        return@addOnSuccessListener
                    }

                    val doc = documents.first()
                    val savedPassword = doc.getString("password")

                    if (savedPassword == password) {
                        // 🔐 Save session
                        val sharedPrefs = getSharedPreferences("MedissaPrefs", MODE_PRIVATE)
                        sharedPrefs.edit().putString("patientID", patientId).apply()

                        showToast("Login successful")
                        startActivity(Intent(this, PatientStart::class.java))
                        finish()
                    } else {
                        showToast("Incorrect password")
                    }
                }
                .addOnFailureListener {
                    showToast("Login error: ${it.message}")
                }
        }
    }

    override fun onStart() {
        super.onStart()
        val sharedPrefs = getSharedPreferences("MedissaPrefs", MODE_PRIVATE)
        val savedPatientId = sharedPrefs.getString("patientID", null)

        if (savedPatientId != null) {
            startActivity(Intent(this, PatientStart::class.java))
            finish()
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
