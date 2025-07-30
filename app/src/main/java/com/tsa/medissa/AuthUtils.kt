package com.tsa.medissa

import android.app.Activity
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

fun redirectIfLoggedIn(activity: Activity) {
    val currentUser = FirebaseAuth.getInstance().currentUser

    if (currentUser != null) {
        val uid = currentUser.uid
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                when (document.getString("role")) {
                    "doctor" -> {
                        activity.startActivity(Intent(activity, DoctorStart::class.java))
                        activity.finish()
                    }
                    "nurse" -> {
                        activity.startActivity(Intent(activity, NurseStart::class.java))
                        activity.finish()
                    }
                }
            }
    }
}

