package com.tsa.medissa


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.*

class NurseDashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.nurse_dashboard_activity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ Set Pill Schedule button
        val btnSetPillSchedule = view.findViewById<MaterialButton>(R.id.btnSetPillSchedule)
        btnSetPillSchedule.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SetPillScheduleFragment())
                .addToBackStack(null)
                .commit()
        }

        // ✅ Add Patient button (Properly declared now)
        val addPatientButton = view.findViewById<MaterialButton>(R.id.addPatientButton)
        addPatientButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AssignMachineFragment())
                .addToBackStack(null)
                .commit()
        }

        // ✅ Display patient count
        val patientCountText = view.findViewById<TextView>(R.id.patientCountText)
        FirebaseDatabase.getInstance().getReference("patients")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.childrenCount
                    patientCountText.text = count.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    patientCountText.text = "Error"
                    Toast.makeText(requireContext(), "Failed to load patient count", Toast.LENGTH_SHORT).show()
                }
            })

        // ✅ View All Patients button
        val viewPatientsButton = view.findViewById<MaterialButton>(R.id.viewPatientsButton)
        viewPatientsButton.setOnClickListener {
            Toast.makeText(requireContext(), "View All Patients (Coming Soon)", Toast.LENGTH_SHORT).show()

            // Uncomment when ready:
            // val intent = Intent(requireContext(), PatientListActivity::class.java)
            // startActivity(intent)
        }
    }
}
