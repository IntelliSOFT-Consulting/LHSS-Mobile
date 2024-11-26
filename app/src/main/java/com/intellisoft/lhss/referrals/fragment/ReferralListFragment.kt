package com.intellisoft.lhss.referrals.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.fhir.FhirEngine
import com.intellisoft.lhss.R
import com.intellisoft.lhss.databinding.FragmentReferralListBinding
import com.intellisoft.lhss.fhir.FhirApplication
import com.intellisoft.lhss.referrals.viewmodels.ReferralListViewModel
import com.intellisoft.lhss.shared.FormatterClass
import com.intellisoft.lhss.shared.PatientReferralAdapter

class ReferralListFragment : Fragment() {
    private var _binding: FragmentReferralListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ReferralListViewModel
    private lateinit var fhirEngine: FhirEngine
    private lateinit var formatterClass: FormatterClass
    private var patientId:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentReferralListBinding.inflate(inflater, container, false)

        formatterClass = FormatterClass(requireContext())

        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientId = formatterClass.getSharedPref("", "patientId") ?: ""


        viewModel =
            ViewModelProvider(
                this,
                ReferralListViewModel.PatientListViewModelFactory(
                    requireActivity().application,
                    fhirEngine,
                    patientId
                ),
            )[ReferralListViewModel::class.java]


        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle search functionality
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { viewModel.searchPatientsByName(it) }
                return false
            }
        })

        // Handle DatePicker icon click
        binding.datepickerIcon.setOnClickListener {
            binding.datePickerLayout.visibility = View.VISIBLE
        }

        // Handle close DatePicker layout
        binding.closeDatePicker.setOnClickListener {
            binding.datePickerLayout.visibility = View.GONE
        }

        viewModel.liveSearchedPatients.observe(viewLifecycleOwner) {

            val requestList = ArrayList(it)

            // Initialize RecyclerView and adapter
            val patientAdapter = PatientReferralAdapter(requestList) { selectedPatient ->

                val serviceId = selectedPatient?.id
                val status = selectedPatient?.status
                if (status == "COMPLETED") {
                    Toast.makeText(requireContext(), "Patient has already been received. " +
                            "The action cannot be performed twice.", Toast.LENGTH_SHORT).show()
                }else{
                    showReceivePatientDialog()

                    formatterClass.saveSharedPref("","serviceRequestId", serviceId.toString())
                }

                
            }

            binding.patientRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.patientRecyclerView.adapter = patientAdapter

            // Set total patients
            binding.totalPatientsTextView.text = "Total Referrals: ${requestList.size}"

        }
    }


    fun showReceivePatientDialog() {
        // Create an AlertDialog builder
        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle("Receive Patient")
        // Set dialog message
        builder.setMessage("Do you want to Receive the Patient?\n\n" +
                "When you choose to receive patient you will fill in an acknowledgement form. ")

        // Set Yes button and its action
        builder.setPositiveButton("Yes") { dialog, _ ->
            // Trigger the form when Yes is clicked
            dialog.dismiss() // Close the dialog
            findNavController().navigate(R.id.action_referralListFragment_to_referralDetailsFragment)
        }

        // Set No button and its action
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss() // Just close the dialog when No is clicked
        }

        // Create and show the dialog
        val dialog = builder.create()
        dialog.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}