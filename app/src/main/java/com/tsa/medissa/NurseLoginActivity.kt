package com.tsa.medissa

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NurseLoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nurse_login)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<MaterialButton>(R.id.loginButton)
        val forgotPasswordTextView = findViewById<TextView>(R.id.nurse_forgot_password)


        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                showToast("Please enter email and password")
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val uid = auth.currentUser?.uid ?: return@addOnSuccessListener

                    firestore.collection("users").document(uid).get()
                        .addOnSuccessListener { document ->
                            val role = document.getString("role")

                            if (role == "nurse") {
                                showToast("Login successful as Nurse")
                                startActivity(Intent(this, NurseStart::class.java))
                                finish()
                            } else {
                                showToast("Access denied: Not a nurse")
                                auth.signOut()
                            }
                        }
                        .addOnFailureListener {
                            showToast("Failed to fetch user role: ${it.message}")
                        }
                }
                .addOnFailureListener {
                    showToast("Login failed: ${it.message}")
                }
        }

        forgotPasswordTextView.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordNurseActivity::class.java))
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
