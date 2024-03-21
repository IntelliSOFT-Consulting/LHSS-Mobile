package com.intellisoft.lhss

import android.app.Application
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.google.gson.Gson
import com.intellisoft.lhss.databinding.FragmentAddVisitBinding
import com.intellisoft.lhss.detail.PatientDetailActivity
import com.intellisoft.lhss.fhir.FhirApplication
import com.intellisoft.lhss.fhir.data.DbPatientDataDetails
import com.intellisoft.lhss.fhir.data.FormatterClass
import com.intellisoft.lhss.fhir.data.NavigationDetails
import com.intellisoft.lhss.fhir.data.ValueString
import com.intellisoft.lhss.viewmodel.PatientDetailsViewModel
import com.intellisoft.lhss.viewmodel.PatientDetailsViewModelFactory
import java.util.Calendar

class AddVisitFragment : Fragment() {

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine
    private val formatterClass = FormatterClass()
    private lateinit var layoutManager: RecyclerView.LayoutManager

    private lateinit var binding: FragmentAddVisitBinding

    private var facilityName = ""
    private var serviceProvided = ""
    private var treatmentProvided = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentAddVisitBinding.inflate(inflater, container, false)
        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientId = formatterClass.getSharedPref("patientId", requireContext()).toString()

        layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.imgBtnBack.setOnClickListener {
            val intent = Intent(requireContext(), PatientDetailActivity::class.java)
            startActivity(intent)
        }

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModelFactory(requireContext().applicationContext as Application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        loadClass()

        binding.tvDatePicker.setOnClickListener { showDatePickerDialog() }

        binding.nextSubmit.setOnClickListener {
            validateData()
        }

        return binding.root
    }

    private fun validateData() {
        val treatmentValue = binding.treatmentDetails.text.toString()
        val otherServices = binding.etOthers.text.toString()
        val selectedDate = binding.tvDatePicker.text.toString()

        if (serviceProvided == ""){
            binding.serviceProvided.requestFocus()
            Toast.makeText(requireContext(), "Select a service", Toast.LENGTH_SHORT).show()
            return
        }
        if (treatmentProvided == ""){
            binding.serviceProvided.requestFocus()
            Toast.makeText(requireContext(), "Select a service", Toast.LENGTH_SHORT).show()
            return
        }
        if (binding.treatment.visibility == View.VISIBLE){
            if (TextUtils.isEmpty(treatmentValue)){
                binding.treatmentDetails.setError("Field cannot be empty")
                binding.treatmentDetails.requestFocus()
                return
            }
        }
        if (binding.tvOthers.visibility == View.VISIBLE){
            if (TextUtils.isEmpty(otherServices)){
                binding.tvOthers.setError("Field cannot be empty")
                binding.tvOthers.requestFocus()
                return
            }
        }
        if (selectedDate == "Date of Visit *"){
            binding.tvDatePicker.error = "Select a date"
            binding.tvDatePicker.requestFocus()
            return
        }

        val dbPatientDataDetailsList = ArrayList<DbPatientDataDetails>()

        val dbPatientDataDetails1 = DbPatientDataDetails("Facility Name",facilityName)
        val dbPatientDataDetails2 = DbPatientDataDetails("Service Provided",serviceProvided)
        val dbPatientDataDetails6 = DbPatientDataDetails("Other Service Provided",otherServices)
        val dbPatientDataDetails3 = DbPatientDataDetails("Treatment Provided",treatmentProvided)
        val dbPatientDataDetails4 = DbPatientDataDetails("Treatment Details",treatmentValue)
        val dbPatientDataDetails5 = DbPatientDataDetails("Date of visit",selectedDate)

        dbPatientDataDetailsList.addAll(
            listOf(
                dbPatientDataDetails1,
                dbPatientDataDetails2,
                dbPatientDataDetails6,
                dbPatientDataDetails3,
                dbPatientDataDetails4,
                dbPatientDataDetails5,
            )
        )

        formatterClass.saveSharedPref("workFlowPersonal",Gson().toJson(dbPatientDataDetailsList),requireContext())

        val intent = Intent(requireContext(), PatientDetailActivity::class.java)
        intent.putExtra("functionToCall", NavigationDetails.ADD_VISIT_HISTORY.name)
        intent.putExtra("patientId", patientId)
        startActivity(intent)

    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val maxCalendar = Calendar.getInstance()
//        maxCalendar.add(Calendar.DAY_OF_MONTH, -7)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                // Handle the selected date (e.g., update the TextView)
                val formattedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.tvDatePicker.text = formattedDate
            },
            year,
            month,
            day
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis() // Set the limit for the last date

        // Show the DatePickerDialog
        datePickerDialog.show()
    }

    private fun loadClass() {

        val titleValue = formatterClass.getSharedPref("title", requireContext())

        val toolbar = view?.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = titleValue
        }

        val country = formatterClass.getSharedPref("country", requireContext())
        if (country == null){
            Toast.makeText(requireContext(), "Updae the patient to have a country associated with them.", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.patientDataDetailFragment)
        }

        val gson = Gson()
        val services_provided = resources.getStringArray(R.array.services_provided)
        val treatment_provided = resources.getStringArray(R.array.treatment_provided)

        val djibouti_facilities = resources.getStringArray(R.array.djibouti_facilities)
        val ethiopia_facilities = resources.getStringArray(R.array.ethiopia_facilities)

        var facilityList = ArrayList<String>()

        facilityList = if (country == "Ethiopia") ArrayList(ethiopia_facilities.toMutableList()) else ArrayList(djibouti_facilities.toMutableList())
        val serviceList = ArrayList(services_provided.toMutableList())
        val treatmentList = ArrayList(treatment_provided.toMutableList())

        createSpinner(facilityList, binding.facilityName, "FACILITY")
        createSpinner(serviceList, binding.serviceProvided, "SERVICE")
        createSpinner(treatmentList, binding.treatmentProvided, "TREATMENT")

        val practitionerFacility = formatterClass.getSharedPref("practitionerFacility", requireContext())
        if (practitionerFacility != null){
            facilityName = practitionerFacility
            binding.tvFacilityName.setText(practitionerFacility)
        }


    }

    private fun createSpinner(spinnerList: ArrayList<String>, spinner: Spinner, valueType: String){

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                if (selectedItem != spinnerList.first()){
                    if (valueType == "FACILITY"){
                        this@AddVisitFragment.facilityName = selectedItem
                    }
                    if (valueType == "SERVICE"){
                        this@AddVisitFragment.serviceProvided = selectedItem
                        if (selectedItem == spinnerList.last()){
                            binding.tvOthers.visibility = View.VISIBLE
                        }else{
                            binding.tvOthers.visibility = View.GONE
                        }
                    }
                    if (valueType == "TREATMENT"){
                        this@AddVisitFragment.treatmentProvided = selectedItem
                        if (selectedItem == "Treated"){
                            binding.treatment.visibility = View.VISIBLE
                            binding.treatment.hint = "Treatment Details"
                        }else if (selectedItem == "Admitted"){
                            binding.treatment.visibility = View.VISIBLE
                            binding.treatment.hint = "Admission Details"
                        }else{
                            binding.treatment.visibility = View.GONE
                        }
                    }
                }
                // Do something with the selected item
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle no selection
            }
        }
    }


}