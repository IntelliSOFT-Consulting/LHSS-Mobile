package com.intellisoft.lhss.patient_details.fragment

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.fhir.FhirEngine
import com.intellisoft.lhss.R
import com.intellisoft.lhss.patient_details.viewmodel.PatientListViewModel
import com.intellisoft.lhss.databinding.FragmentPatientListBinding
import com.intellisoft.lhss.dynamic_components.FieldManager
import com.intellisoft.lhss.fhir.FhirApplication
import com.intellisoft.lhss.shared.DbPatientItem
import com.intellisoft.lhss.shared.FormatterClass
import com.intellisoft.lhss.shared.PatientAdapter

class PatientListFragment : Fragment() {
    private var _binding: FragmentPatientListBinding? = null
    private val binding get() = _binding!!
    private lateinit var fieldManager: FieldManager
    private lateinit var patientListViewModel: PatientListViewModel
    private lateinit var fhirEngine: FhirEngine
    private lateinit var formatterClass: FormatterClass
    private var patientFilterList = ArrayList<DbPatientItem>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPatientListBinding.inflate(inflater, container, false)

        formatterClass = FormatterClass(requireContext())
        formatterClass.clearData()

        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientListViewModel =
            ViewModelProvider(
                this,
                PatientListViewModel.PatientListViewModelFactory(
                    requireActivity().application,
                    fhirEngine
                ),
            )[PatientListViewModel::class.java]

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
                newText?.let { patientListViewModel.searchPatientsByName(it) }
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

            binding.tvFromDate.text = ""
            binding.tvToDate.text = ""
        }

        binding.tvFromDate.setOnClickListener {
            formatterClass.showDatePickerWithLimits(binding.tvFromDate, true, null)
            populateRecyclerView(patientFilterList)
        }
        binding.tvToDate.setOnClickListener {
            val fromDate = binding.tvFromDate.text.toString()
            val fromDateStr = if (!TextUtils.isEmpty(fromDate)) fromDate else null

            formatterClass.showDatePickerWithLimits(binding.tvToDate, false, fromDateStr)
            populateRecyclerView(patientFilterList)
        }

        patientListViewModel.liveSearchedPatients.observe(viewLifecycleOwner) {

            val patientList = ArrayList(it)
            patientFilterList = patientList
            populateRecyclerView(patientList)

        }

    }

    private fun populateRecyclerView(patientList: ArrayList<DbPatientItem>) {

        val fromDate = binding.tvFromDate.text.toString()
        val toDate = binding.tvToDate.text.toString()

        val fromDateStr = if (!TextUtils.isEmpty(fromDate)) fromDate else null
        val toDateStr = if (!TextUtils.isEmpty(toDate)) toDate else null

        val patientSortedList = formatterClass.sortPatientListByDate(
            patientList, fromDateStr, toDateStr)

        // Initialize RecyclerView and adapter
        val patientAdapter = PatientAdapter(patientList) { selectedPatient ->

            val id = selectedPatient.id
            val patientName = selectedPatient.name
            formatterClass.saveSharedPref("","patientId", id)
            formatterClass.saveSharedPref("","patientName", patientName)

            findNavController().navigate(R.id.action_patientListFragment_to_patientCardFragment)
        }
        binding.patientRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.patientRecyclerView.adapter = patientAdapter

        // Set total patients
        binding.totalPatientsTextView.text = "Total Patients: ${patientList.size}"

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}