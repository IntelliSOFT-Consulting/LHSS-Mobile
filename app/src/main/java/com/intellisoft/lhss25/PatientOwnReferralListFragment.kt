package com.intellisoft.lhss25

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.fhir.FhirEngine
import com.intellisoft.lhss25.databinding.FragmentPatientOwnReferralListBinding
import com.intellisoft.lhss25.databinding.FragmentReferralListBinding
import com.intellisoft.lhss25.fhir.FhirApplication
import com.intellisoft.lhss25.referrals.viewmodels.ReferralListViewModel
import com.intellisoft.lhss25.shared.FormatterClass
import com.intellisoft.lhss25.shared.PatientReferralAdapter

class PatientOwnReferralListFragment : Fragment() {

    private var _binding: FragmentPatientOwnReferralListBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ReferralListViewModel
    private lateinit var fhirEngine: FhirEngine
    private lateinit var formatterClass: FormatterClass
    private var patientId:String = ""
    private var userFhirPractitionerId:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = FragmentPatientOwnReferralListBinding.inflate(inflater, container, false)

        formatterClass = FormatterClass(requireContext())

        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientId = formatterClass.getSharedPref("", "patientId") ?: ""
        userFhirPractitionerId = formatterClass.getSharedPref("", "userFhirPractitionerId") ?: ""

        formatterClass.saveSharedPref("", "referralStatus", "ALL")

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

        viewModel.liveSearchedPatients.observe(viewLifecycleOwner) {

            val requestList = ArrayList(it)

            // Initialize RecyclerView and adapter
            val patientAdapter = PatientReferralAdapter(requestList) { selectedPatient ->

                val serviceId = selectedPatient?.id
                val status = selectedPatient?.status
                val requesterId = selectedPatient?.requesterId

                formatterClass.saveSharedPref("","serviceRequestId", serviceId.toString())

                findNavController().navigate(R.id.action_patientOwnReferralListFragment_to_viewFormDetailsFragment)

            }

            binding.patientRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.patientRecyclerView.adapter = patientAdapter

            // Set total patients
            binding.totalPatientsTextView.text = "Total Referrals: ${requestList.size}"


        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}