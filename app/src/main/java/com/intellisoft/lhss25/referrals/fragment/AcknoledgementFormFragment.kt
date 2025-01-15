package com.intellisoft.lhss25.referrals.fragment

import android.app.Application
import android.graphics.Color
import android.graphics.Typeface
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.fhir.FhirEngine
import com.google.gson.Gson
import com.intellisoft.lhss25.referrals.viewmodels.AcknoledgementFormViewModel
import com.intellisoft.lhss25.R
import com.intellisoft.lhss25.clinical_info.viewmodel.ClinicalInfoDetailsViewModel
import com.intellisoft.lhss25.databinding.FragmentAcknoledgementFormBinding
import com.intellisoft.lhss25.shared.DbClasses
import com.intellisoft.lhss25.shared.DbField
import com.intellisoft.lhss25.shared.DbNavigationDetails
import com.intellisoft.lhss25.shared.DbWidgets
import com.intellisoft.lhss25.dynamic_components.DefaultLabelCustomizer
import com.intellisoft.lhss25.dynamic_components.FieldManager
import com.intellisoft.lhss25.shared.FormData
import com.intellisoft.lhss25.dynamic_components.FormUtils
import com.intellisoft.lhss25.fhir.Constants
import com.intellisoft.lhss25.fhir.FhirApplication
import com.intellisoft.lhss25.referrals.viewmodels.ReferralDetailsViewModel
import com.intellisoft.lhss25.referrals.viewmodels.ReferralDetailsViewModelFactory
import com.intellisoft.lhss25.shared.FormatterClass

class AcknoledgementFormFragment : Fragment() {

    private var _binding: FragmentAcknoledgementFormBinding? = null
    private val binding get() = _binding!!
    private lateinit var fieldManager: FieldManager

    private val viewModel: AcknoledgementFormViewModel by viewModels()
    private lateinit var formatterClass: FormatterClass
    private var patientId:String = ""
    private var serviceRequestId:String = ""
    private lateinit var clinicalViewModel: ClinicalInfoDetailsViewModel
    private lateinit var referralViewModel: ReferralDetailsViewModel
    private var carePlanId:String = ""
    private lateinit var fhirEngine: FhirEngine


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAcknoledgementFormBinding.inflate(inflater, container, false)
        fhirEngine = FhirApplication.fhirEngine(requireContext())

        navigationActions()
        formatterClass = FormatterClass(requireContext())
        carePlanId = formatterClass.getSharedPref(DbNavigationDetails.CARE_PLAN.name, "carePlanId")?: ""

        patientId = formatterClass.getSharedPref("", "patientId") ?: ""
        serviceRequestId = formatterClass.getSharedPref("", "serviceRequestId") ?: ""

        clinicalViewModel =
            ViewModelProvider(
                this,
                ClinicalInfoDetailsViewModel.ClinicalInfoDetailsViewModelFactory(
                    requireActivity().application,
                    patientId
                )
            )[ClinicalInfoDetailsViewModel::class.java]

        referralViewModel =
            ViewModelProvider(
                this,
                ReferralDetailsViewModelFactory(
                    requireContext().applicationContext as Application,
                    fhirEngine,
                    patientId,
                    serviceRequestId
                ),
            )
                .get(ReferralDetailsViewModel::class.java)

        val workflowTitles = formatterClass.getWorkflowTitles(DbClasses.ACKNOWLEDGEMENT_FORM.name)
        if (workflowTitles != null){
            binding.tvTitle.text = formatterClass.toSentenceCase(workflowTitles.text)
            binding.imgBtn.setImageResource(workflowTitles.iconId)
        }

        return binding.root

    }

    private fun navigationActions() {
        // Set the next button text to "Continue" and add click listeners
        val navigationButtons = binding.navigationButtons
        navigationButtons.setNextButtonText("Review")

        navigationButtons.setBackButtonClickListener {
            // Handle back button click
            findNavController().navigateUp()
        }

        navigationButtons.setNextButtonClickListener {
            // Handle next button click
            // Navigate to the next fragment or perform any action

            // Call the function to extract form data
            val (addedFields, missingFields) = FormUtils.extractAllFormData(binding.rootLayout)

            if (missingFields.isNotEmpty()){
                var missingText = ""
                missingFields.forEach { missingText += "\n ${it.tag}, " }

                val mandatoryText = "The following are mandatory fields and " +
                        "need to be filled before proceeding: \n" +
                        missingText

                formatterClass.showDialog("Missing Content", mandatoryText)
            }else{

                val emailData = addedFields.find { it.tag == "Email Contact" }
//                if (emailData == null){
//                    Toast.makeText(requireContext(), "Email Contact cannot be null", Toast.LENGTH_SHORT).show()
//                    return@setNextButtonClickListener
//                }

                if (emailData != null){
                    if (!formatterClass.isValidEmail(emailData.text)){
                        Toast.makeText(requireContext(), "Invalid email", Toast.LENGTH_SHORT).show()
                        return@setNextButtonClickListener
                    }
                }

//                if (emailData.text.isEmpty() || !formatterClass.isValidEmail(emailData.text)){
//                    Toast.makeText(requireContext(), "Invalid email", Toast.LENGTH_SHORT).show()
//                    return@setNextButtonClickListener
//                }

                findNavController().navigate(R.id.action_acknoledgementFormFragment_to_acknoledgementDetailsFragment)

                val formData = FormData(
                    DbClasses.ACKNOWLEDGEMENT_FORM.name,
                    addedFields)

                val gson = Gson()
                val json = gson.toJson(formData)

                formatterClass.saveSharedPref(
                    sharedPrefName = DbNavigationDetails.REFERRALS.name,
                    DbClasses.ACKNOWLEDGEMENT_FORM.name,
                    json
                )

            }

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize FieldManager with dependencies (inject via constructor or manually)
        fieldManager = FieldManager(DefaultLabelCustomizer(), requireContext())

        val dbFieldList = listOf(
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Name of Patient", false,
                InputType.TYPE_TEXT_VARIATION_PERSON_NAME,
                emptyList(),
                false
            ),
            DbField(
                DbWidgets.DATE_PICKER.name,
                "Date Patient Reported at Receiving Facility",
                true
            ),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Our TB Registration No", true,
                InputType.TYPE_CLASS_TEXT
            ),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Your TB Registration No", false,
                InputType.TYPE_CLASS_NUMBER,
                emptyList(),
                false
            ),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Name of Health Facility", true,
                InputType.TYPE_CLASS_TEXT
            ),
//            DbField(
//                DbWidgets.EDIT_TEXT.name,
//                "Country", true,
//                InputType.TYPE_CLASS_TEXT
//            ),
//            DbField(
//                DbWidgets.EDIT_TEXT.name,
//                "Region/Province/County", true,
//                InputType.TYPE_CLASS_TEXT
//            ),
//            DbField(
//                DbWidgets.EDIT_TEXT.name,
//                "District/Sub-county", true,
//                InputType.TYPE_CLASS_TEXT
//            ),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Phone Number", false,
                InputType.TYPE_CLASS_TEXT
            ),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Contact Person", true,
                InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            ),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Telephone of contact person", true,
                InputType.TYPE_CLASS_PHONE,
                emptyList(),
                true,
                null,
                true,
                null,
                null,
                "7XXXXXXXXXX"
            ),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Tb Focal Person", true,
                InputType.TYPE_CLASS_TEXT
            ),
            DbField(
                DbWidgets.SPINNER.name,
                "Designation", true, null,
                listOf("Doctor", "Nurse", "Clinical Officer", "Other")
            ),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Email Contact", false,
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            ),
//            DbField(
//                DbWidgets.RADIO_BUTTON.name,
//                "Referral of PTLD",
//                true,
//                optionList = listOf("Yes", "No")
//            )

        )

        FormUtils.populateView(ArrayList(dbFieldList), binding.rootLayout, fieldManager, requireContext())

        //Get and populate form data from the database if available
        val ourTbFhirCode = Constants.TB_OUR_REGISTRATION_CODE
        val ourTbRegistration = referralViewModel.getObservationCode(ourTbFhirCode)

        val rootViewOurTbParent = binding.rootLayout
            .findViewWithTag<View>("Our TB Registration No")
        if (rootViewOurTbParent != null && ourTbRegistration != null) {
            //Check if rootViewParent is EditText and set its text from the retrieved observation
            if (rootViewOurTbParent is EditText) {
                rootViewOurTbParent.setText(ourTbRegistration.text)
                rootViewOurTbParent.setTypeface(rootViewOurTbParent.typeface, Typeface.BOLD)
                //Set the color to bold
                rootViewOurTbParent.setTextColor(Color.BLACK)
            }
        }

        //get the patient name from the database if available
        val patientName = formatterClass.getSharedPref("", "patientName")?: ""

        val rootViewParentName = binding.rootLayout
            .findViewWithTag<View>("Name of Patient")
        if (rootViewParentName!= null && patientName!= "") {
            //Check if rootViewParent is EditText and set its text from the retrieved patient name
            if (rootViewParentName is EditText) {
                rootViewParentName.setText(patientName)
                rootViewParentName.setTypeface(rootViewParentName.typeface, Typeface.BOLD)
                rootViewParentName.isEnabled = false

                //Set the color to bold
                rootViewParentName.setTextColor(Color.BLACK)
            }
        }

        //Get and populate form data from the database if available
        val fhirCode = Constants.TB_YOUR_REGISTRATION_CODE
        val tbRegistration = referralViewModel.getObservationCode(fhirCode)

        val rootViewParent = binding.rootLayout
            .findViewWithTag<View>("Your TB Registration No")
        if (rootViewParent != null && tbRegistration != null) {
            //Check if rootViewParent is EditText and set its text from the retrieved observation
            if (rootViewParent is EditText) {
                rootViewParent.setText(tbRegistration.text)
                rootViewParent.setTypeface(rootViewParent.typeface, Typeface.BOLD)
                //Set the color to bold
                rootViewParent.setTextColor(Color.BLACK)
            }
        }



        //Get When the patient was referred from the database if available
        val referralDateFhirCode = Constants.REFERRAL_DATE
        val referralDate = referralViewModel.getObservationCode(referralDateFhirCode)

        val rootViewParentReferralDate = binding.rootLayout
            .findViewWithTag<View>("Date Patient Reported at Receiving Facility")
        if (rootViewParentReferralDate!= null && referralDate!= null) {
            //Check if rootViewParent is EditText and set its text from the retrieved observation
            if (rootViewParentReferralDate is EditText) {
                rootViewParentReferralDate.setText(referralDate.text)
                rootViewParentReferralDate.isEnabled = false
                rootViewParentReferralDate.setTypeface(rootViewParentReferralDate.typeface, Typeface.BOLD)
                //Set the color to bold
                rootViewParentReferralDate.setTextColor(Color.BLACK)
            }
        }

        val healthFacilityName = formatterClass.getSharedPref("", "userFullName")?: ""

        val rootViewParentHealthFacilityName = binding.rootLayout
            .findViewWithTag<View>("Name of Health Facility")
        if (rootViewParentHealthFacilityName!= null) {
            //Check if rootViewParent is EditText and set its text from the retrieved observation
            if (rootViewParentHealthFacilityName is EditText) {
                rootViewParentHealthFacilityName.setText(healthFacilityName)
                rootViewParentHealthFacilityName.isEnabled = false
                rootViewParentHealthFacilityName.setTypeface(rootViewParentHealthFacilityName.typeface, Typeface.BOLD)
                //Set the color to bold
                rootViewParentHealthFacilityName.setTextColor(Color.BLACK)
            }
        }
        val userPhoneNumber = formatterClass.getSharedPref("", "userPhoneNumber")?: ""
        val rootViewParentPhoneNumber = binding.rootLayout
            .findViewWithTag<View>("Phone Number")
        if (rootViewParentPhoneNumber!= null) {
            //Check if rootViewParent is EditText and set its text from the retrieved observation
            if (rootViewParentPhoneNumber is EditText) {
                rootViewParentPhoneNumber.setText(userPhoneNumber)
                rootViewParentHealthFacilityName.isEnabled = false
                rootViewParentPhoneNumber.setTypeface(rootViewParentPhoneNumber.typeface, Typeface.BOLD)
                //Set the color to bold
                rootViewParentPhoneNumber.setTextColor(Color.BLACK)
            }
        }


        FormUtils.loadFormData(
            requireContext(),
            binding.rootLayout,
            DbNavigationDetails.REFERRALS.name,
            DbClasses.ACKNOWLEDGEMENT_FORM.name
        )

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}