package com.tsa.medissa

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DoctorSignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    override fun onStart() {
        super.onStart()
        redirectIfLoggedIn(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.doc_signup)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirmPasswordEditText)
        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val hospitalNumberEditText = findViewById<EditText>(R.id.hospitalNumberEditText)
        val signupButton = findViewById<Button>(R.id.registerButton)

        signupButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()
            val name = nameEditText.text.toString().trim()
            val hospitalNumber = hospitalNumberEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || name.isEmpty() || hospitalNumber.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get next available doctorID
            val counterRef = db.collection("counters").document("doctor")
            counterRef.get().addOnSuccessListener { snapshot ->
                val next = snapshot.getLong("nextID") ?: 1
                val doctorID = "DOC%03d".format(next)

                // Register doctor in Firebase Auth
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { authResult ->
                        val uid = authResult.user?.uid ?: return@addOnSuccessListener
                        val userMap = hashMapOf(
                            "name" to name,
                            "email" to email,
                            "hospitalNumber" to hospitalNumber,
                            "role" to "doctor",
                            "doctorID" to doctorID
                        )

                        db.collection("users").document(uid).set(userMap)
                            .addOnSuccessListener {
                                counterRef.update("nextID", next + 1)
                                Toast.makeText(this, "Doctor registered successfully!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, DoctorStart::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Firestore save failed", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Signup failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
