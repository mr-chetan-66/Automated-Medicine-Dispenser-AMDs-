package com.tsa.medissa

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.tsa.medissa.databinding.ActivitySetTimeBinding

class SetTimeActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetTimeBinding
    private lateinit var database: DatabaseReference
    private var currentCompartmentCount = 0
    private val MAX_COMPARTMENTS = 15
    private val adapter = PillTimeAdapter { pillTime ->
        showEditDeleteDialog(pillTime)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetTimeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance().reference.child("pillTimes")

        setupUI()
        loadExistingPillTimes()
    }

    private fun setupUI() {
        updateCompartmentCounter()

        // Setup RecyclerView
        binding.scheduledTimesList.apply {
            layoutManager = LinearLayoutManager(this@SetTimeActivity)
            adapter = this@SetTimeActivity.adapter
        }

        // Setup save button
        binding.btnSaveTime.setOnClickListener {
            savePillTimeToFirebase()
        }
    }

    private fun updateCompartmentCounter() {
        binding.txtCompartmentCount.text = "$currentCompartmentCount/$MAX_COMPARTMENTS"
    }

    private fun savePillTimeToFirebase() {
        val medicationName = binding.edtMedicationName.text.toString()
        if (medicationName.isEmpty()) {
            binding.medicationNameInput.error = "Please enter medication name"
            return
        }

        if (currentCompartmentCount >= MAX_COMPARTMENTS) {
            showMessage("Maximum compartments reached")
            return
        }

        val pillTime = PillTime(
            hour = binding.timePicker.hour,
            minute = binding.timePicker.minute,
            medicationName = medicationName,
            compartmentNumber = currentCompartmentCount + 1
        )

        // Log data being saved
        Log.d("Firebase", "Saving data: $pillTime")

        database.push().setValue(pillTime)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    currentCompartmentCount++
                    updateCompartmentCounter()
                    clearInputs()
                    showMessage("Pill time saved successfully")
                } else {
                    showMessage("Failed to save: ${task.exception?.message}")
                    Log.e("Firebase", "Error saving data: ${task.exception?.message}")
                }
            }
    }

    private fun loadExistingPillTimes() {
        Log.d("Firebase", "Loading existing pill times")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pillTimes = mutableListOf<PillTime>()
                currentCompartmentCount = 0

                for (childSnapshot in snapshot.children) {
                    childSnapshot.getValue(PillTime::class.java)?.let { pillTime ->
                        pillTimes.add(pillTime)
                        currentCompartmentCount++
                    }
                }

                adapter.submitList(pillTimes)
                updateCompartmentCounter()

                // Log data loaded
                Log.d("Firebase", "Data loaded: ${snapshot.value}")
            }

            override fun onCancelled(error: DatabaseError) {
                showMessage("Failed to load pill times: ${error.message}")
                Log.e("Firebase", "Error loading data: ${error.message}")
            }
        })
    }

    private fun showEditDeleteDialog(pillTime: PillTime) {
        val options = arrayOf("Edit", "Delete")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Manage Pill Time")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> editPillTime(pillTime)
                    1 -> deletePillTime(pillTime)
                }
            }
            .show()
    }

    private fun editPillTime(pillTime: PillTime) {
        Log.d("Firebase", "Editing pill time: $pillTime")

        binding.edtMedicationName.setText(pillTime.medicationName)
        binding.timePicker.hour = pillTime.hour
        binding.timePicker.minute = pillTime.minute

        binding.btnSaveTime.text = "Update Schedule"
        binding.btnSaveTime.setOnClickListener {
            updatePillTime(pillTime)
        }
    }

    private fun updatePillTime(oldPillTime: PillTime) {
        val newPillTime = PillTime(
            hour = binding.timePicker.hour,
            minute = binding.timePicker.minute,
            medicationName = binding.edtMedicationName.text.toString(),
            compartmentNumber = oldPillTime.compartmentNumber,
            timestamp = oldPillTime.timestamp
        )

        // Log the update process
        Log.d("Firebase", "Updating pill time: $newPillTime")

        database.orderByChild("timestamp")
            .equalTo(oldPillTime.timestamp.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (childSnapshot in snapshot.children) {
                        childSnapshot.ref.setValue(newPillTime)
                            .addOnSuccessListener {
                                showMessage("Schedule updated successfully")
                                resetSaveButton()
                                clearInputs()
                            }
                            .addOnFailureListener { e ->
                                showMessage("Failed to update: ${e.message}")
                                Log.e("Firebase", "Error updating data: ${e.message}")
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showMessage("Update cancelled: ${error.message}")
                    Log.e("Firebase", "Update cancelled: ${error.message}")
                }
            })
    }

    private fun deletePillTime(pillTime: PillTime) {
        Log.d("Firebase", "Deleting pill time: $pillTime")

        database.orderByChild("timestamp")
            .equalTo(pillTime.timestamp.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (childSnapshot in snapshot.children) {
                        childSnapshot.ref.removeValue()
                            .addOnSuccessListener {
                                currentCompartmentCount--
                                updateCompartmentCounter()
                                showMessage("Schedule deleted successfully")
                            }
                            .addOnFailureListener { e ->
                                showMessage("Failed to delete: ${e.message}")
                                Log.e("Firebase", "Error deleting data: ${e.message}")
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showMessage("Delete cancelled: ${error.message}")
                    Log.e("Firebase", "Delete cancelled: ${error.message}")
                }
            })
    }

    private fun resetSaveButton() {
        binding.btnSaveTime.text = "Save Schedule"
        binding.btnSaveTime.setOnClickListener {
            savePillTimeToFirebase()
        }
    }

    private fun clearInputs() {
        binding.edtMedicationName.text?.clear()
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.d("Firebase", message)  // Log the message for debugging
    }
}
