package com.intellisoft.lhss25.patient_details.fragment

import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.fhir.FhirEngine
import com.google.gson.Gson
import com.intellisoft.lhss25.R
import com.intellisoft.lhss25.clinical_info.viewmodel.ClinicalInfoDetailsViewModel
import com.intellisoft.lhss25.databinding.FragmentPatientCardBinding
import com.intellisoft.lhss25.dynamic_components.FieldManager
import com.intellisoft.lhss25.fhir.FhirApplication
import com.intellisoft.lhss25.patient_details.viewmodel.PatientCardViewModel
import com.intellisoft.lhss25.patient_details.viewmodel.PatientDetailsViewModelFactory
import com.intellisoft.lhss25.shared.DbClasses
import com.intellisoft.lhss25.shared.DbNavigationDetails
import com.intellisoft.lhss25.shared.FormData
import com.intellisoft.lhss25.shared.FormDataAdapter
import com.intellisoft.lhss25.shared.FormatterClass

class PatientCardFragment : Fragment() {

    private var _binding: FragmentPatientCardBinding? = null
    private val binding get() = _binding!!
    private lateinit var fieldManager: FieldManager
    private lateinit var formatterClass: FormatterClass

    private lateinit var patientDetailsViewModel: PatientCardViewModel
    private lateinit var fhirEngine: FhirEngine
    private var patientId:String = ""
    private lateinit var formDataAdapter: FormDataAdapter
    private lateinit var clinicalViewModel: ClinicalInfoDetailsViewModel
    private var lastDestinationId: Int? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPatientCardBinding.inflate(inflater, container, false)

        formatterClass = FormatterClass(requireContext())

        formatterClass.deleteSharedPref("","referralStatus")

        patientId = formatterClass.getSharedPref("", "patientId") ?: ""

        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientDetailsViewModel =
            ViewModelProvider(
                this,
                PatientDetailsViewModelFactory(
                    requireContext().applicationContext as Application,
                    fhirEngine,
                    patientId
                ),
            )
                .get(PatientCardViewModel::class.java)

        clinicalViewModel =
            ViewModelProvider(
                this,
                ClinicalInfoDetailsViewModel.ClinicalInfoDetailsViewModelFactory(
                    requireActivity().application,
                    patientId
                )
            )[ClinicalInfoDetailsViewModel::class.java]

        formatterClass.clearPatientData()

        formatterClass.deleteSharedPref("","FORM_NAME")

        return binding.root

    }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnReferrals.setOnClickListener {
            findNavController().navigate(R.id.action_patientCardFragment_to_patientOwnReferralListFragment)
        }
        binding.btnReferPatient.setOnClickListener {
            findNavController().navigate(R.id.action_patientCardFragment_to_referPatientFragment)
        }
        binding.btnPatientFile.setOnClickListener {

            val isServiceRequest = getServiceRequestInfo()

            if (isServiceRequest) {
                findNavController().navigate(R.id.action_patientCardFragment_to_patientFileFragment)
            } else {
                Toast.makeText(requireContext(), "Kindly start with performing a referral first", Toast.LENGTH_SHORT).show()
            }
        }

        val formDataList = patientDetailsViewModel.getPatientInfo()

        formDataAdapter = FormDataAdapter(formDataList, requireContext())
        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        binding.recyclerView.adapter = formDataAdapter

        val fullName = formatterClass.getNameFields(formDataList)
        binding.tvFullName.text = fullName
        val crossBorderId = "Cross Border Id: ${patientId.substring(0,6)}"
        binding.tvCrossBorderId.text = crossBorderId

        binding.imgBtnEdit.setOnClickListener {

            val gson = Gson()

            formDataList.forEach {formDataValue ->

                val title = formDataValue.title
                val addedFields = formDataValue.formDataList

                val formData = FormData(
                    title,
                    addedFields)

                val json = gson.toJson(formData)

                formatterClass.saveSharedPref(
                    sharedPrefName = DbNavigationDetails.PATIENT_REGISTRATION.name,
                    title,
                    json
                )


            }

            findNavController().navigate(R.id.action_patientCardFragment_to_demographicsFragment2)


        }

        // get navController to listen for back button presses
        val navController = findNavController()
        navController.addOnDestinationChangedListener { previous, destination, res ->
            lastDestinationId = previous.previousBackStackEntry?.destination?.id
        }

        formDataList.forEach {
            Log.e("-------->","<------")
            println(it)
            println(it.title)
            println(it.formDataList)
            Log.e("-------->","<------")
        }

        // Access the backQueue which contains the back stack entries

//        navController.addOnDestinationChangedListener { controller, destination, arguments ->
//            Log.e("BackStack", "Navigated to: ${destination.displayName}")
//
//            // Log current and previous entries
//            controller.currentBackStackEntry?.let {
//                Log.e("BackStack", "Current Fragment: ${it.destination.displayName}")
//            }
//
//            controller.previousBackStackEntry?.let {
//                Log.e("BackStack", "Previous Fragment: ${it.destination.displayName}")
//            }
//        }

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the menu specific to this fragment
        inflater.inflate(R.menu.patient_card_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle menu item clicks here
        return when (item.itemId) {
            R.id.acknoledgementDetailsFragment -> {
                // Perform the action for this menu item
                formatterClass.saveSharedPref("", "FORM_NAME", "ACKNOWLEDGEMENT_FORM")
                findNavController().navigate(R.id.action_patientCardFragment_to_filledFormsListFragment)
                true
            }
            R.id.endTreatmentFormFragment -> {
                formatterClass.saveSharedPref("", "FORM_NAME", "END_TREATMENT_FORM")
                findNavController().navigate(R.id.action_patientCardFragment_to_filledFormsListFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getServiceRequestInfo():Boolean {
        val serviceRequestList = clinicalViewModel.getServiceRequests()
        val serviceRequest = serviceRequestList.size
        return serviceRequest > 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}