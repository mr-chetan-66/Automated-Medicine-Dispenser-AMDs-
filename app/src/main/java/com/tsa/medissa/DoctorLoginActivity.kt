package com.tsa.medissa

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DoctorLoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var doctorIdEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.doc_login)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        doctorIdEditText = findViewById(R.id.doctorIdEditText)

        val loginButton = findViewById<MaterialButton>(R.id.loginButton)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val doctorID = doctorIdEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || doctorID.isEmpty()) {
                showToast("Please fill all fields")
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val uid = auth.currentUser?.uid ?: return@addOnSuccessListener

                    firestore.collection("users").document(uid).get()
                        .addOnSuccessListener { document ->
                            val role = document.getString("role")
                            val storedDoctorID = document.getString("doctorID")

                            if (role == "doctor" && doctorID == storedDoctorID) {
                                showToast("Login successful")
                                startActivity(Intent(this, DoctorStart::class.java))
                                finish()
                            } else {
                                showToast("Access denied: Doctor ID or role mismatch")
                                auth.signOut()
                            }
                        }
                        .addOnFailureListener {
                            showToast("Failed to fetch doctor data: ${it.message}")
                        }
                }
                .addOnFailureListener {
                    showToast("Login failed: ${it.message}")
                }
        }
    }

    override fun onStart() {
        super.onStart()
        redirectIfLoggedIn(this)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
