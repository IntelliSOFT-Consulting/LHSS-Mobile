package com.intellisoft.lhss.patient_registration

import android.app.ProgressDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.intellisoft.lhss.R
import com.intellisoft.lhss.add_patient.AddPatientViewModel
import com.intellisoft.lhss.databinding.FragmentPatientLocationBinding
import com.intellisoft.lhss.fhir.data.DbAdministrative
import com.intellisoft.lhss.fhir.data.FormatterClass
import com.intellisoft.lhss.utils.BlurBackgroundDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PatientLocationFragment : Fragment() {

    private lateinit var binding: FragmentPatientLocationBinding
    private var formatterClass = FormatterClass()
    private var identificationTypeValue = ""
    private var identificationNumberValue = ""
    private var occupationTypeValue = ""
    private var originCountryValue = ""
    private var residenceCountryValue = ""
    private var regionValue = ""
    private var districtValue = ""
    private val formatter = FormatterClass()
    private val viewModel: AddPatientViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentPatientLocationBinding.inflate(layoutInflater)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isUpdate = formatterClass.getSharedPref("isPatientUpdate", requireContext())

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = "Register Patient"
        }
        binding.imgBtnBack.setOnClickListener { findNavController().navigate(R.id.patientDetailsFragment) }

        if (isUpdate != null) {
            displayInitialData()
        }

        getData()

    }

    private fun displayInitialData() {

        val gson = Gson()

        val identificationTypeList = resources.getStringArray(R.array.identification_type)
        val occupationList = resources.getStringArray(R.array.occupation_type)
        val originCountryList = resources.getStringArray(R.array.origin_country)
        val residenceCountryList = resources.getStringArray(R.array.residence_country)

        val djiboutiRegionList = resources.getStringArray(R.array.djibouti_region)
        val ethiopiaRegionList = resources.getStringArray(R.array.ethiopia_region)

        val djiboutiDistrictList = resources.getStringArray(R.array.djibouti_district)
        val ethiopiaDistrictList = resources.getStringArray(R.array.ethiopia_district)

        val registrationFlowAdministrative = formatterClass.getSharedPref("registrationFlowAdministrative", requireContext())
        val dbAdministrative = gson.fromJson(registrationFlowAdministrative, DbAdministrative::class.java)

        val identificationType = dbAdministrative.identificationType
        val identificationNumber = dbAdministrative.identificationNumber
        val occupationType = dbAdministrative.occupationType
        val originCountry = dbAdministrative.originCountry
        val residenceCountry = dbAdministrative.residenceCountry
        val region = dbAdministrative.region
        val district = dbAdministrative.district

        createSpinner(ArrayList(identificationTypeList.toMutableList()), binding.identificationType, identificationType)
        createSpinner(ArrayList(occupationList.toMutableList()), binding.occupationType, occupationType)
        createSpinner(ArrayList(originCountryList.toMutableList()), binding.originCountry, originCountry)
        createSpinner(ArrayList(residenceCountryList.toMutableList()), binding.residenceCountry, residenceCountry)

        val regionList: ArrayList<String>
        val districtList: ArrayList<String>

        if (residenceCountry.contains("Djibouti")){
            regionList = ArrayList(djiboutiRegionList.toMutableList())
            districtList = ArrayList(djiboutiDistrictList.toMutableList())
        }else{
            regionList = ArrayList(ethiopiaRegionList.toMutableList())
            districtList = ArrayList(ethiopiaDistrictList.toMutableList())
        }

        createSpinner(ArrayList(regionList.toMutableList()), binding.region, region)
        createSpinner(ArrayList(districtList.toMutableList()), binding.district, district)

        if (identificationNumber != ""){
            binding.identificationNumber.setText(identificationNumber)
        }

    }

    private fun createSpinner(spinnerList: ArrayList<String>, spinner: Spinner, valueName:String){
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            spinnerList)

        var preselectedIndex = 0
        for (index in spinnerList.indices ){
            if (valueName == spinnerList[index]){
                preselectedIndex = index
                break
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.setSelection(preselectedIndex)
    }

    private fun getData() {

        val identificationTypeList = resources.getStringArray(R.array.identification_type)
        val occupationList = resources.getStringArray(R.array.occupation_type)
        val originCountryList = resources.getStringArray(R.array.origin_country)
        val residenceCountryList = resources.getStringArray(R.array.residence_country)

        val djiboutiRegionList = resources.getStringArray(R.array.djibouti_region)
        val ethiopiaRegionList = resources.getStringArray(R.array.ethiopia_region)

        val djiboutiDistrictList = resources.getStringArray(R.array.djibouti_district)
        val ethiopiaDistrictList = resources.getStringArray(R.array.ethiopia_district)



        binding.apply {

            identificationType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedItem = parent?.getItemAtPosition(position).toString()
                    if (selectedItem != identificationTypeList.first()){
                        this@PatientLocationFragment.identificationTypeValue = selectedItem
                    }
                    // Do something with the selected item
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Handle no selection
                }
            }
            occupationType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedItem = parent?.getItemAtPosition(position).toString()
                    if (selectedItem != occupationList.first()){
                        this@PatientLocationFragment.occupationTypeValue = selectedItem
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Handle no selection
                }
            }
            originCountry.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedItem = parent?.getItemAtPosition(position).toString()
                    if (selectedItem != originCountryList.first()){
                        this@PatientLocationFragment.originCountryValue = selectedItem
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Handle no selection
                }
            }
            residenceCountry.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedItem = parent?.getItemAtPosition(position).toString()
                    if (selectedItem != residenceCountryList.first()){
                        region.visibility = View.VISIBLE
                        district.visibility = View.VISIBLE

                        val regionList: ArrayList<String>
                        val districtList: ArrayList<String>

                        if (selectedItem.contains("Djibouti")){
                            regionList = ArrayList(djiboutiRegionList.toMutableList())
                            districtList = ArrayList(djiboutiDistrictList.toMutableList())
                        }else{
                            regionList = ArrayList(ethiopiaRegionList.toMutableList())
                            districtList = ArrayList(ethiopiaDistrictList.toMutableList())
                        }

                        createRegionSpinner(regionList, region)
                        createRegionSpinner(districtList, district)

                        this@PatientLocationFragment.residenceCountryValue = selectedItem
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Handle no selection
                }
            }

            previousButton.setOnClickListener {
                findNavController().navigate(R.id.patientDetailsFragment)
            }
            nextButton.setOnClickListener {

                val identificationNumberValue = identificationNumber.text.toString()
                if (TextUtils.isEmpty(identificationNumberValue)) {
                    identificationNumber.error = "Field cannot be empty"
                }else{

                    val dbAdministrative = DbAdministrative(
                        identificationTypeValue,
                        identificationNumberValue,
                        occupationTypeValue,
                        originCountryValue,
                        residenceCountryValue,
                        regionValue,
                        districtValue
                    )

                    formatter.saveSharedPref("registrationFlowAdministrative", Gson().toJson(dbAdministrative), requireContext())

                    val progressBar = ProgressDialog(requireContext())
                    progressBar.setCanceledOnTouchOutside(false)
                    progressBar.setTitle("Saving Patient")
                    progressBar.setMessage("Please wait as the patient is being saved")
                    progressBar.show()

                    CoroutineScope(Dispatchers.IO).launch {
                        viewModel.createManualPatient()

                        CoroutineScope(Dispatchers.Main).launch {
                            progressBar.dismiss()
                            val blurBackgroundDialog =
                                BlurBackgroundDialog(this@PatientLocationFragment, requireContext())
                            blurBackgroundDialog.show()

                        }

                    }

                }

            }

        }

    }


    private fun createRegionSpinner(regionList: ArrayList<String>, spinner: Spinner) {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            regionList)


        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                if (selectedItem != regionList.first()){
                    if (regionList.first() == "Region *"){
                        regionValue = selectedItem
                    }
                    if (regionList.first() == "District *"){
                        districtValue = selectedItem
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