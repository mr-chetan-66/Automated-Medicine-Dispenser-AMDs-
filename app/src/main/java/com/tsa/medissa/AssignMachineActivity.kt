package com.tsa.medissa

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.database.*

class AssignMachineFragment : Fragment() {

    private lateinit var editTextPatientName: EditText
    private lateinit var editTextBedNumber: EditText
    private lateinit var textViewScannedResult: TextView
    private lateinit var buttonScanQR: Button
    private lateinit var buttonAssignMachine: Button
    private lateinit var progressBar: ProgressBar

    private var scannedMachineId: String? = null

    private val qrScannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val qrResult = result.data?.getStringExtra(QRScannerActivity.EXTRA_QR_RESULT)
            qrResult?.let {
                scannedMachineId = it
                textViewScannedResult.text = "Scanned Machine ID: $it"
                validateForm()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_assign_machine, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind views
        editTextPatientName = view.findViewById(R.id.editTextPatientName)
        editTextBedNumber = view.findViewById(R.id.editTextBedNumber)
        textViewScannedResult = view.findViewById(R.id.textViewScannedResult)
        buttonScanQR = view.findViewById(R.id.buttonScanQR)
        buttonAssignMachine = view.findViewById(R.id.buttonAssignMachine)
        progressBar = view.findViewById(R.id.progressBar)

        buttonScanQR.setOnClickListener {
            val intent = Intent(requireContext(), QRScannerActivity::class.java)
            qrScannerLauncher.launch(intent)
        }

        buttonAssignMachine.setOnClickListener {
            assignMachineToPatient()
        }
    }

    private fun validateForm() {
        buttonAssignMachine.isEnabled =
            editTextPatientName.text.toString().isNotBlank() &&
                    editTextBedNumber.text.toString().isNotBlank() &&
                    scannedMachineId != null
    }

    private fun assignMachineToPatient() {
        val name = editTextPatientName.text.toString().trim()
        val bedNumber = editTextBedNumber.text.toString().trim()
        val machineId = scannedMachineId ?: return

        if (name.isEmpty() || bedNumber.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter patient name and bed number", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseDatabase.getInstance().reference
        val patientId = db.child("patients").push().key ?: return

        progressBar.visibility = View.VISIBLE
        buttonAssignMachine.isEnabled = false

        val patientData = mapOf(
            "name" to name,
            "bedNumber" to bedNumber,
            "machineId" to machineId
        )

        db.child("patients").child(patientId).setValue(patientData)
            .addOnSuccessListener {
                val assignment = mapOf(
                    "patientId" to patientId,
                    "bedNumber" to bedNumber,
                    "assignedAt" to ServerValue.TIMESTAMP
                )

                db.child("machineAssignments").child(machineId).setValue(assignment)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Machine assigned to $name", Toast.LENGTH_SHORT).show()
                        resetForm()
                    }
                    .addOnFailureListener {
                        showError("Assignment failed: ${it.message}")
                    }
            }
            .addOnFailureListener {
                showError("Patient creation failed: ${it.message}")
            }
    }

    private fun resetForm() {
        editTextPatientName.text?.clear()
        editTextBedNumber.text?.clear()
        scannedMachineId = null
        textViewScannedResult.text = "Scanned Machine ID: None"
        buttonAssignMachine.isEnabled = false
        progressBar.visibility = View.GONE
    }

    private fun showError(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        progressBar.visibility = View.GONE
        buttonAssignMachine.isEnabled = true
    }
}
