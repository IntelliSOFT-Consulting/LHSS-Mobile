package com.intellisoft.lhss.patient_registration

import android.app.Application
import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.google.gson.Gson
import com.intellisoft.lhss.R
import com.intellisoft.lhss.add_patient.AddPatientViewModel
import com.intellisoft.lhss.databinding.FragmentPatientLocationBinding
import com.intellisoft.lhss.databinding.FragmentRegPreviewBinding
import com.intellisoft.lhss.detail.ui.main.adapters.PatientDetailDataAdapter
import com.intellisoft.lhss.fhir.FhirApplication
import com.intellisoft.lhss.fhir.data.CustomPatient
import com.intellisoft.lhss.fhir.data.DbAdministrative
import com.intellisoft.lhss.fhir.data.DbPatientDataDetails
import com.intellisoft.lhss.fhir.data.FormatterClass
import com.intellisoft.lhss.utils.BlurBackgroundDialog
import com.intellisoft.lhss.viewmodel.PatientDetailsViewModel
import com.intellisoft.lhss.viewmodel.PatientDetailsViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class RegPreviewFragment : Fragment() {

    private lateinit var binding: FragmentRegPreviewBinding
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var fhirEngine: FhirEngine
    private val formatterClass = FormatterClass()
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private var customPatient = CustomPatient("","","","","","", "")
    private var dbAdministrative = DbAdministrative("","","","","", "","")
    private var country :String? = null
    private val viewModel: AddPatientViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentRegPreviewBinding.inflate(layoutInflater)

        fhirEngine = FhirApplication.fhirEngine(requireContext())

        layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setHasFixedSize(true)

        formatterClass.deleteSharedPref("isUpdateBack", requireContext())

        layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setHasFixedSize(true)

        binding.nextSubmit.setOnClickListener {

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
                        BlurBackgroundDialog(this@RegPreviewFragment, requireContext())
                    blurBackgroundDialog.show()

                }

            }
        }

        binding.imgBtnBack.setOnClickListener {
            formatterClass.saveSharedPref("isPatientUpdateBack","true", requireContext())
            findNavController().navigate(R.id.patientLocationFragment)
        }

        getData()

        return binding.root
    }

    private fun getData() {
        formatterClass.deleteSharedPref("isUpdateBack", requireContext())

        val gson = Gson()
        val dbPatientDataDetailsList = ArrayList<DbPatientDataDetails>()

        val personal = formatterClass.getSharedPref("registrationFlowPersonal", requireContext())
        if (personal != null){
            val data = Gson().fromJson(personal, CustomPatient::class.java)

            val firstnameValue = data.firstname
            val lastNameValue = data.lastname
            val middleNameValue = data.middlename
            val gender = data.gender
            val dateOfBirth = data.dateOfBirth
            val phoneNumber = data.phoneNumber

            val fName = DbPatientDataDetails("First name", firstnameValue)
            val lName = DbPatientDataDetails("Last name", lastNameValue)
            val mName = DbPatientDataDetails("Middle name", middleNameValue)
            val genderValue = DbPatientDataDetails("Gender", gender)
            val dob = DbPatientDataDetails("Date of Birth", dateOfBirth)
            val phone = DbPatientDataDetails("Phone number", phoneNumber)

            dbPatientDataDetailsList.addAll(
                listOf(fName, lName, mName, genderValue, dob, phone)
            )

        }

        val missed1 = DbPatientDataDetails("", "")
        dbPatientDataDetailsList.addAll(
            listOf(missed1,missed1,missed1)
        )

        val registrationFlowAdministrative = formatterClass.getSharedPref("registrationFlowAdministrative", requireContext())
        if(registrationFlowAdministrative != null){
            val dbAdministrative = gson.fromJson(registrationFlowAdministrative, DbAdministrative::class.java)

            val identificationType = dbAdministrative.identificationType
            val identificationNumber = dbAdministrative.identificationNumber
            val occupationType = dbAdministrative.occupationType
            val originCountry = dbAdministrative.originCountry
            val residenceCountry = dbAdministrative.residenceCountry
            val region = dbAdministrative.region
            val district = dbAdministrative.district

            val idType = DbPatientDataDetails("Identification Type", identificationType)
            val idNumber = DbPatientDataDetails("Identification Number", identificationNumber)
            val occuType = DbPatientDataDetails("Occupation Type", occupationType)
            val origin = DbPatientDataDetails("Origin Country", originCountry)
            val residence = DbPatientDataDetails("Residence Country", residenceCountry)
            val regionValue = DbPatientDataDetails("Region", region)
            val districtValue = DbPatientDataDetails("District", district)

            dbPatientDataDetailsList.addAll(listOf(
                idType,idNumber,occuType,origin,residence,regionValue,districtValue
            ))

        }

        CoroutineScope(Dispatchers.Main).launch {
            val visitHistoryAdapter = PatientDetailDataAdapter(dbPatientDataDetailsList, requireContext())
            binding.recyclerView.adapter = visitHistoryAdapter
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

}