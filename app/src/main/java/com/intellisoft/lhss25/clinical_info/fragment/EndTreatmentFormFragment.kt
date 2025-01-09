package com.intellisoft.lhss25.clinical_info.fragment

import android.app.Application
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.fhir.FhirEngine
import com.google.gson.Gson
import com.intellisoft.lhss25.R
import com.intellisoft.lhss25.clinical_info.viewmodel.ClinicalInfoDetailsViewModel
import com.intellisoft.lhss25.clinical_info.viewmodel.ClinicalInfoViewViewModel
import com.intellisoft.lhss25.databinding.FragmentEndTreatmentFormBinding
import com.intellisoft.lhss25.dynamic_components.DefaultLabelCustomizer
import com.intellisoft.lhss25.dynamic_components.DefaultSpinnerSelectionHandler
import com.intellisoft.lhss25.dynamic_components.FieldManager
import com.intellisoft.lhss25.dynamic_components.FormUtils
import com.intellisoft.lhss25.dynamic_components.SpinnerSelectionHandler
import com.intellisoft.lhss25.fhir.Constants
import com.intellisoft.lhss25.fhir.FhirApplication
import com.intellisoft.lhss25.referrals.viewmodels.ReferralDetailsViewModel
import com.intellisoft.lhss25.referrals.viewmodels.ReferralDetailsViewModelFactory
import com.intellisoft.lhss25.shared.DbClasses
import com.intellisoft.lhss25.shared.DbField
import com.intellisoft.lhss25.shared.DbNavigationDetails
import com.intellisoft.lhss25.shared.DbWidgets
import com.intellisoft.lhss25.shared.FormData
import com.intellisoft.lhss25.shared.FormatterClass

class EndTreatmentFormFragment : Fragment() {


    private var _binding: FragmentEndTreatmentFormBinding? = null
    private val binding get() = _binding!!  // This property will always refer to the latest binding instance

    private lateinit var fieldManager: FieldManager
    private lateinit var formatterClass: FormatterClass
    private var patientId:String = ""
    private var carePlanId:String = ""
    private lateinit var clinicalViewModel: ClinicalInfoDetailsViewModel
    private lateinit var referralViewModel: ReferralDetailsViewModel
    private lateinit var fhirEngine: FhirEngine

    private val clinicalInfoViewViewModel: ClinicalInfoViewViewModel by viewModels()
    private val spinnerSelectionHandler: SpinnerSelectionHandler = DefaultSpinnerSelectionHandler()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentEndTreatmentFormBinding.inflate(inflater, container, false)
        fhirEngine = FhirApplication.fhirEngine(requireContext())


        formatterClass = FormatterClass(requireContext())
        navigationActions()
        patientId = formatterClass.getSharedPref("", "patientId")?: ""
        carePlanId = formatterClass.getSharedPref(DbNavigationDetails.CARE_PLAN.name, "carePlanId")?: ""

        val workflowTitles = formatterClass.getWorkflowTitles(DbClasses.END_TREATMENT_FORM.name)
        if (workflowTitles != null){
            binding.tvTitle.text = formatterClass.toSentenceCase(workflowTitles.text)
            binding.imgBtn.setImageResource(workflowTitles.iconId)
        }

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
                    ""
                ),
            )
                .get(ReferralDetailsViewModel::class.java)

        return binding.root


    }

    private fun navigationActions() {
        // Set the next button text to "Continue" and add click listeners
        val navigationButtons = binding.navigationButtons
        navigationButtons.setNextButtonText("Save")

        navigationButtons.setBackButtonClickListener {
            // Handle back button click
            findNavController().navigateUp()
        }

        navigationButtons.setNextButtonClickListener {
            // Handle next button click
            // Navigate to the next fragment or perform any action


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
                if (emailData == null){
                    Toast.makeText(requireContext(), "Email Contact cannot be null", Toast.LENGTH_SHORT).show()
                    return@setNextButtonClickListener
                }

                if (emailData.text.isEmpty() || !formatterClass.isValidEmail(emailData.text)){
                    Toast.makeText(requireContext(), "Invalid email", Toast.LENGTH_SHORT).show()
                    return@setNextButtonClickListener
                }
                
                findNavController().navigate(R.id.action_endTreatmentFormFragment_to_endTreatmentReviewFragment)

                val formData = FormData(
                    DbClasses.END_TREATMENT_FORM.name,
                    addedFields)

                val gson = Gson()
                val json = gson.toJson(formData)

                formatterClass.saveSharedPref(
                    sharedPrefName = DbNavigationDetails.REFER_PATIENT.name,
                    DbClasses.END_TREATMENT_FORM.name,
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
                "Date Patient Reported to Facility",
                true
            ),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Our TB Registration No",
                true,
                InputType.TYPE_CLASS_NUMBER,
                emptyList(),
                true,
                Constants.TB_OUR_REGISTRATION_CODE
            ),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Your TB Registration No", false,
                InputType.TYPE_CLASS_NUMBER,
                emptyList(),
                false
            ),
//            DbField(
//                DbWidgets.EDIT_TEXT.name,
//                "Final Outcome of treatment", true,
//                InputType.TYPE_CLASS_TEXT
//            ),
            DbField(
                DbWidgets.SPINNER.name,
                "Final Outcome of treatment", true, null,
                listOf("Cured", "Treatment Completed", "Lost to follow-up", "Treatment failed",
                    "Died", "Other Final Outcome of treatment")
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
                DbWidgets.SPINNER.name,
                "Designation", true, null,
                listOf("Doctor", "Nurse", "Clinical Officer", "Other Designation")
            ),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Specify the designation", false,
                InputType.TYPE_CLASS_TEXT
            ),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Email Contact", false,
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            ),
            DbField(
                DbWidgets.RADIO_BUTTON.name,
                "Referral of PTLD",
                false,
                optionList = listOf("Yes", "No")
            )
        )

        FormUtils.populateView(ArrayList(dbFieldList), binding.rootLayout, fieldManager, requireContext())

        //get the patient name from the database if available
        val patientName = formatterClass.getSharedPref("", "patientName")?: ""

        val rootViewParentName = binding.rootLayout.findViewWithTag<View>("Name of Patient")
        if (rootViewParentName!= null && patientName!= "") {
            //Check if rootViewParent is EditText and set its text from the retrieved patient name
            if (rootViewParentName is EditText) {
                rootViewParentName.setText(patientName)
                rootViewParentName.setTypeface(rootViewParentName.typeface, Typeface.BOLD)
                //Set the color to bold
                rootViewParentName.setTextColor(Color.BLACK)
            }
        }

        //Get and populate form data from the database if available
        val tbRegistrationFhirCode = Constants.TB_YOUR_REGISTRATION_CODE
        val tbRegistration = referralViewModel.getObservationCode(tbRegistrationFhirCode)

        val rootViewParent = binding.rootLayout.findViewWithTag<View>("Your TB Registration No")
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

        val rootViewParentReferralDate = binding.rootLayout.findViewWithTag<View>("Date Patient Reported to Facility")
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



        FormUtils.loadFormData(
            requireContext(),
            binding.rootLayout,
            DbNavigationDetails.REFERRALS.name,
            DbClasses.END_TREATMENT_FORM.name
        )

        setSpinnerListener(
            listOf("Designation")
        )

        clinicalInfoViewViewModel.selectedItem.observe(viewLifecycleOwner) { selectedItem ->

            val designationOthersText = formatterClass.findTextViewByText(binding.rootLayout, "Specify the designation")
            val designationOthers = binding.rootLayout.findViewWithTag<View>("Specify the designation")

            if (selectedItem == "Other Designation"){
                designationOthersText?.visibility = View.VISIBLE
                designationOthers?.visibility = View.VISIBLE
            }else{
                designationOthersText?.visibility = View.GONE
                designationOthers?.visibility = View.GONE
            }

            val finalOutcomeOthersText = formatterClass.findTextViewByText(binding.rootLayout, "Final Outcome of treatment")
            val finalOutcomeOthers = binding.rootLayout.findViewWithTag<View>("Final Outcome of treatment")
            if (selectedItem == "Other Final Outcome of treatment"){
                finalOutcomeOthersText?.visibility = View.VISIBLE
                finalOutcomeOthers?.visibility = View.VISIBLE
            }else{
                finalOutcomeOthersText?.visibility = View.GONE
                finalOutcomeOthers?.visibility = View.GONE
            }

        }

    }

    private fun setSpinnerListener(tagList: List<String>) {
        tagList.forEach { tag ->
            val rootViewParent = binding.rootLayout.findViewWithTag<View>(tag)
            if (rootViewParent is Spinner) {
                spinnerSelectionHandler.handleSelection(rootViewParent) { selectedItem ->
                    clinicalInfoViewViewModel.updateSelectedItem(selectedItem, rootViewParent)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}