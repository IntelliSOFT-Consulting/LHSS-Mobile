package com.intellisoft.lhss

import android.app.Application
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.DatePicker
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.intellisoft.lhss.databinding.FragmentAddVisitBinding
import com.intellisoft.lhss.databinding.FragmentDoReferralBinding
import com.intellisoft.lhss.fhir.FhirApplication
import com.intellisoft.lhss.fhir.data.DbPatientDataDetails
import com.intellisoft.lhss.fhir.data.FormatterClass
import com.intellisoft.lhss.viewmodel.PatientDetailsViewModel
import com.intellisoft.lhss.viewmodel.PatientDetailsViewModelFactory
import java.util.Calendar


class DoReferralFragment : Fragment() {

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine
    private val formatterClass = FormatterClass()
    private lateinit var layoutManager: RecyclerView.LayoutManager

    private lateinit var binding: FragmentDoReferralBinding
    private var referredTofacilityName = ""
    private var referredFromfacilityName = ""
    private var referralReason = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDoReferralBinding.inflate(inflater, container, false)
        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientId = formatterClass.getSharedPref("patientId", requireContext()).toString()

        layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.imgBtnBack.setOnClickListener {
            findNavController().navigate(R.id.patientDataDetailFragment)
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

        val selectedDate = binding.tvDatePicker.text.toString()
        val providerName = binding.etProviderName.text.toString()
        val referralDetails = binding.etDetails.text.toString()

        if (selectedDate == "Date of Visit *"){
            binding.tvDatePicker.error = "Select a date"
            binding.tvDatePicker.requestFocus()
            return
        }
        if (referredTofacilityName == ""){
            binding.facilityReferredTo.requestFocus()
            Toast.makeText(requireContext(), "Select a facility", Toast.LENGTH_SHORT).show()
            return
        }
        if (referralReason == ""){
            binding.referralReason.requestFocus()
            Toast.makeText(requireContext(), "Select a referral reason", Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(providerName)){
            binding.etProviderName.setError("Field cannot be empty")
            binding.etProviderName.requestFocus()
            return
        }
        if (binding.tvDetails.visibility == View.VISIBLE){
            if (TextUtils.isEmpty(referralDetails)){
                binding.etDetails.setError("Field cannot be empty")
                binding.etDetails.requestFocus()
                return
            }
        }

        val dbPatientDataDetailsList = ArrayList<DbPatientDataDetails>()

        val dbPatientDataDetails1 = DbPatientDataDetails("Referring Health Facility",referredFromfacilityName)
        val dbPatientDataDetails2 = DbPatientDataDetails("Health Facility Referred to",referredTofacilityName)
        val dbPatientDataDetails3 = DbPatientDataDetails("Reason for Referral",referralReason)
        val dbPatientDataDetails4 = DbPatientDataDetails("Details",referralDetails)
        val dbPatientDataDetails5 = DbPatientDataDetails("Date of referral",selectedDate)

        dbPatientDataDetailsList.addAll(
            listOf(
                dbPatientDataDetails1,
                dbPatientDataDetails2,
                dbPatientDataDetails3,
                dbPatientDataDetails4,
                dbPatientDataDetails5,
            )
        )

        formatterClass.saveSharedPref("workFlowPersonal","dbPatientDataDetailsList",requireContext())

        findNavController().navigate(R.id.workFlowReviewFragment)

    }

    private fun loadClass() {

        val practitionerFacility = formatterClass.getSharedPref("practitionerFacility", requireContext())
        if (practitionerFacility != null){
            referredFromfacilityName = practitionerFacility
        }

        val country = formatterClass.getSharedPref("country", requireContext())
        if (country == null){
            Toast.makeText(requireContext(), "Updae the patient to have a country associated with them.", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.patientDataDetailFragment)
        }

        val djibouti_facilities = resources.getStringArray(R.array.djibouti_facilities)
        val ethiopia_facilities = resources.getStringArray(R.array.ethiopia_facilities)

        var facilityList = ArrayList<String>()

        facilityList = if (country == "Ethiopia") ArrayList(ethiopia_facilities.toMutableList()) else ArrayList(djibouti_facilities.toMutableList())

        val referral_reason = resources.getStringArray(R.array.referral_reason)
        val referralList = ArrayList(referral_reason.toMutableList())

        if (referredFromfacilityName != ""){
            facilityList.remove(referredFromfacilityName)
        }

        createSpinner(facilityList, binding.facilityReferredTo, "FACILITY")
        createSpinner(referralList, binding.referralReason, "REFERRAL")


    }

    private fun createSpinner(spinnerList: ArrayList<String>, spinner: Spinner, valueType: String){

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                if (selectedItem != spinnerList.first()){
                    if (valueType == "FACILITY"){
                        this@DoReferralFragment.referredTofacilityName = selectedItem
                    }
                    if (valueType == "REFERRAL"){
                        this@DoReferralFragment.referralReason = selectedItem
                        if (selectedItem == spinnerList.last()){
                            binding.tvDetails.visibility = View.VISIBLE
                        }else{
                            binding.tvDetails.visibility = View.GONE
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
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() // Set the limit for the last date

        // Show the DatePickerDialog
        datePickerDialog.show()
    }

}