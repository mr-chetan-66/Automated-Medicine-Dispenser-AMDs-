package com.tsa.medissa

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.tsa.medissa.databinding.ActivitySetPillScheduleBinding

class SetPillScheduleFragment : Fragment() {

    private var _binding: ActivitySetPillScheduleBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private var selectedPatientId: String? = null
    private var selectedMachineId: String? = null

    private val MAX_COMPARTMENTS = 15
    private var currentCompartmentCount = 0

    private val adapter = PillTimeAdapter { pillTime ->
        showEditDeleteDialog(pillTime)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivitySetPillScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        loadPatientsIntoSpinner()
    }

    private fun setupUI() {
        updateCompartmentCounter()

        binding.scheduledTimesList.layoutManager = LinearLayoutManager(requireContext())
        binding.scheduledTimesList.adapter = adapter

        binding.btnSaveTime.setOnClickListener {
            if (selectedMachineId != null) {
                savePillTimeToFirebase()
            } else {
                showMessage("Please select a patient")
            }
        }
    }

    private fun loadPatientsIntoSpinner() {
        val ref = FirebaseDatabase.getInstance().getReference("patients")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val patientList = mutableListOf<Pair<String, String>>()

                for (child in snapshot.children) {
                    val id = child.key ?: continue
                    val name = child.child("name").getValue(String::class.java) ?: continue
                    patientList.add(Pair(id, name))
                }

                val spinnerItems = patientList.map { it.second }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerItems)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                binding.spinnerPatientList.adapter = adapter

                binding.spinnerPatientList.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        selectedPatientId = patientList[position].first
                        loadMachineIdForPatient(selectedPatientId!!)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showMessage("Failed to load patients: ${error.message}")
            }
        })
    }

    private fun loadMachineIdForPatient(patientId: String) {
        val db = FirebaseDatabase.getInstance().reference
        db.child("patients").child(patientId).child("machineId")
            .get().addOnSuccessListener { snapshot ->
                selectedMachineId = snapshot.value.toString()
                binding.textViewMachineId.text = "Machine ID: $selectedMachineId"

                database = FirebaseDatabase.getInstance().reference
                    .child("pillSchedules").child(selectedMachineId!!).child("pillList")

                loadExistingPillTimes()
            }
            .addOnFailureListener {
                showMessage("Failed to load machine ID")
            }
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
            compartmentNumber = currentCompartmentCount + 1,
            timestamp = System.currentTimeMillis()
        )

        database.push().setValue(pillTime)
            .addOnSuccessListener {
                currentCompartmentCount++
                updateCompartmentCounter()
                clearInputs()
                showMessage("Pill time saved successfully")
            }
            .addOnFailureListener {
                showMessage("Failed to save: ${it.message}")
            }
    }

    private fun loadExistingPillTimes() {
        if (selectedMachineId == null) return

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
            }

            override fun onCancelled(error: DatabaseError) {
                showMessage("Failed to load pill times: ${error.message}")
            }
        })
    }

    private fun showEditDeleteDialog(pillTime: PillTime) {
        val options = arrayOf("Edit", "Delete")
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
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
                            .addOnFailureListener {
                                showMessage("Update failed: ${it.message}")
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showMessage("Update cancelled: ${error.message}")
                }
            })
    }

    private fun deletePillTime(pillTime: PillTime) {
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
                            .addOnFailureListener {
                                showMessage("Delete failed: ${it.message}")
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showMessage("Delete cancelled: ${error.message}")
                }
            })
    }

    private fun updateCompartmentCounter() {
        binding.txtCompartmentCount.text = "$currentCompartmentCount/$MAX_COMPARTMENTS"
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
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        Log.d("SetPillSchedule", message)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}