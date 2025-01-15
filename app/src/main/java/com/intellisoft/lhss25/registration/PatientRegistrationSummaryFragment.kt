package com.intellisoft.lhss25.registration

import android.app.ProgressDialog
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.fhir.FhirEngine
import com.google.gson.Gson
import com.intellisoft.lhss25.R
import com.intellisoft.lhss25.databinding.FragmentPatientRegistrationSummaryBinding
import com.intellisoft.lhss25.shared.DbClasses
import com.intellisoft.lhss25.shared.DbNavigationDetails
import com.intellisoft.lhss25.dynamic_components.FieldManager
import com.intellisoft.lhss25.shared.BlurBackgroundDialog
import com.intellisoft.lhss25.shared.FormData
import com.intellisoft.lhss25.shared.FormDataAdapter
import com.intellisoft.lhss25.shared.FormatterClass
import com.intellisoft.lhss25.shared.MainActivityViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PatientRegistrationSummaryFragment : Fragment() {
    private var _binding: FragmentPatientRegistrationSummaryBinding? = null
    private val binding get() = _binding!!
    private lateinit var fieldManager: FieldManager

    private lateinit var viewModel: MainActivityViewModel
    private lateinit var formatterClass: FormatterClass

    private val viewModelPatientSummary: PatientRegistrationSummaryViewModel by viewModels()

    private var formDataList = ArrayList<FormData>()
    private lateinit var formDataAdapter: FormDataAdapter
    private lateinit var fhirEngine: FhirEngine


    private val navigationDetails = DbNavigationDetails.PATIENT_REGISTRATION.name
    private val registrationClassesList = listOf(
        DbClasses.DEMOGRAPHICS.name,
        DbClasses.ADDRESS_ORIGIN.name,
        DbClasses.ADDRESS_RESIDENCE.name,
        DbClasses.NEXT_OF_KIN.name,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPatientRegistrationSummaryBinding.inflate(inflater, container, false)


        viewModel = ViewModelProvider(this,
            SavedStateViewModelFactory(requireActivity().application, this))
            .get(MainActivityViewModel::class.java)

        navigationActions()



        formatterClass = FormatterClass(requireContext())

        return binding.root
    }
    private fun navigationActions() {
        // Set the next button text to "Continue" and add click listeners
        val navigationButtons = binding.navigationButtons
        navigationButtons.setNextButtonText("Submit")

        navigationButtons.setBackButtonClickListener {
            // Handle back button click
            findNavController().navigateUp()
        }

        navigationButtons.setNextButtonClickListener {
            // Handle next button click
            // Navigate to the next fragment or perform any action

            submitData()

        }
    }

    private fun submitData() {

        CoroutineScope(Dispatchers.Main).launch {

            val progressDialog = ProgressDialog(requireContext())
            progressDialog.setTitle("Please wait")
            progressDialog.setMessage("Patient is being saved.")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            var savedResources = ArrayList<String>()

            val job = Job()
            CoroutineScope(Dispatchers.IO + job).launch {

                savedResources = ArrayList(viewModelPatientSummary.createPatientResource(formDataList))

                registrationClassesList.forEach {
                    formatterClass.deleteSharedPref(navigationDetails, it)
                }

            }.join()

            progressDialog.dismiss()

            val blurBackgroundDialog = if (savedResources.isNotEmpty()){
                //Save was okay
                BlurBackgroundDialog(requireContext(),
                    "Patient Registered Successfully.",
                    this@PatientRegistrationSummaryFragment,
                    R.id.action_patientRegistrationSummaryFragment_to_patientCardFragment
                )
            }else{
                //Save was not okay
                BlurBackgroundDialog(requireContext(),
                    "There was an issue with the registration.",
                    this@PatientRegistrationSummaryFragment,
                    R.id.landingPageFragment
                )
            }
            blurBackgroundDialog.show()



        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gson = Gson()

        registrationClassesList.forEach {
            val savedJson = formatterClass.getSharedPref(navigationDetails, it)
            val formDataFromJson = gson.fromJson(savedJson, FormData::class.java)
            formDataList.addAll(listOf(formDataFromJson))
        }

        if (formDataList.isEmpty()){
            findNavController().navigate(R.id.landingPageFragment)
        }else{
            formDataAdapter = FormDataAdapter(formDataList, requireContext())
            binding.recyclerView.layoutManager = LinearLayoutManager(context)

            binding.recyclerView.adapter = formDataAdapter
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}