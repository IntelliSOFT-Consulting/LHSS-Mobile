package com.intellisoft.lhss.registration.fragment

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.intellisoft.lhss.registration.viewmodel.NextOfKinViewModel
import com.intellisoft.lhss.R
import com.intellisoft.lhss.clinical_info.viewmodel.ClinicalInfoViewViewModel
import com.intellisoft.lhss.databinding.FragmentNextOfKinBinding
import com.intellisoft.lhss.shared.DbClasses
import com.intellisoft.lhss.shared.DbField
import com.intellisoft.lhss.shared.DbNavigationDetails
import com.intellisoft.lhss.shared.DbWidgets
import com.intellisoft.lhss.dynamic_components.DefaultLabelCustomizer
import com.intellisoft.lhss.dynamic_components.DefaultRadioGroupSelectionHandler
import com.intellisoft.lhss.dynamic_components.DefaultSpinnerSelectionHandler
import com.intellisoft.lhss.dynamic_components.FieldManager
import com.intellisoft.lhss.shared.FormData
import com.intellisoft.lhss.dynamic_components.FormUtils
import com.intellisoft.lhss.dynamic_components.MandatoryRadioGroup
import com.intellisoft.lhss.dynamic_components.SpinnerSelectionHandler
import com.intellisoft.lhss.shared.DbFormData
import com.intellisoft.lhss.shared.FormatterClass
import com.intellisoft.lhss.shared.MainActivityViewModel

class NextOfKinFragment : Fragment() {

    private var _binding: FragmentNextOfKinBinding? = null
    private val binding get() = _binding!!
    private lateinit var fieldManager: FieldManager

    private val viewModel: MainActivityViewModel by viewModels()

    private val viewModelNextOfKin: NextOfKinViewModel by viewModels()

    private val relationShipList = listOf("Wife", "Husband", "Sister", "Brother", "Son", "Daughter", "Parent")
    private lateinit var formatterClass: FormatterClass
    private val clinicalInfoViewViewModel: ClinicalInfoViewViewModel by viewModels()
    private val spinnerSelectionHandler: SpinnerSelectionHandler = DefaultSpinnerSelectionHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentNextOfKinBinding.inflate(inflater, container, false)

        navigationActions()
        formatterClass = FormatterClass(requireContext())

        val workflowTitles = formatterClass.getWorkflowTitles(DbClasses.NEXT_OF_KIN.name)
        if (workflowTitles != null){
            binding.tvTitle.text = formatterClass.toSentenceCase(workflowTitles.text)
            binding.imgBtn.setImageResource(workflowTitles.iconId)
        }

        return binding.root

    }
    private fun setSpinnerListener(tagList: List<String>) {
        tagList.forEach { tag ->
            val rootViewParent = binding.rootLayout.findViewWithTag<View>(tag)
            if (rootViewParent is Spinner) {
                spinnerSelectionHandler.handleSelection(rootViewParent) { selectedItem ->
                    clinicalInfoViewViewModel.updateSelectedItem(selectedItem)
                }
            }
        }
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

            // Check that if No is selected we can just proceed to the next fragment
            val contactConsent = addedFields.find { it.tag == "Can we contact your Next of Kin/Relative?" }
            if (contactConsent?.text == "Yes") {

                if (missingFields.isNotEmpty()){
                    var missingText = ""
                    missingFields.forEach { missingText += "\n ${it.tag}, " }

                    val mandatoryText = "The following are mandatory fields and " +
                            "need to be filled before proceeding: \n" +
                            missingText

                    formatterClass.showDialog("Missing Content", mandatoryText)
                }else{

                    val telephoneData = addedFields.find { it.tag == "Telephone" }

                    if (telephoneData != null) {
                        val textNumber = telephoneData.text
                        val isPhoneValid = formatterClass.getStandardPhoneNumber(textNumber)
                        if (isPhoneValid) {

                            saveData(addedFields)

                        }else{
                            Toast.makeText(context, "You have provided an invalid phone number", Toast.LENGTH_LONG).show()
                        }
                    }
                }

            }else {
                saveData(addedFields)
            }



        }
    }

    private fun saveData(addedFields: ArrayList<DbFormData>) {

        findNavController().navigate(R.id.action_nextOfKinFragment_to_patientRegistrationSummaryFragment)

        val formData = FormData(
            DbClasses.NEXT_OF_KIN.name,
            addedFields)

        val gson = Gson()
        val json = gson.toJson(formData)

        formatterClass.saveSharedPref(
            sharedPrefName = DbNavigationDetails.PATIENT_REGISTRATION.name,
            DbClasses.NEXT_OF_KIN.name,
            json
        )

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize FieldManager with dependencies (inject via constructor or manually)
        fieldManager = FieldManager(DefaultLabelCustomizer(), requireContext())

        val dbFieldList = listOf(
            DbField(
                DbWidgets.SPINNER.name,
                "Can we contact your Next of Kin/Relative?", true, null,
                optionList = listOf("Yes", "No")),
//            DbField(
//                DbWidgets.RADIO_BUTTON.name,
//                "Can we contact your Next of Kin/Relative?",
//                true,
//                optionList = listOf("Yes", "No")
//            ),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Full Name", true,
                InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            ),
            DbField(
                DbWidgets.SPINNER.name,
                "Relationship", true, null,
                relationShipList),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Telephone", true,
                InputType.TYPE_CLASS_PHONE
            )
        )

        FormUtils.populateView(ArrayList(dbFieldList), binding.rootLayout, fieldManager, requireContext())

        setSpinnerListener(
            listOf("Can we contact your Next of Kin/Relative?")
        )

        clinicalInfoViewViewModel.selectedItem.observe(viewLifecycleOwner) { selectedItem ->

            val fullName = binding.rootLayout.findViewWithTag<View>("Full Name")
            val relationship = binding.rootLayout.findViewWithTag<View>("Relationship")
            val telephone = binding.rootLayout.findViewWithTag<View>("Telephone")

            val fullNameText = formatterClass.findTextViewByText(binding.rootLayout, "Full Name *")
            val relationshipText = formatterClass.findTextViewByText(binding.rootLayout, "Relationship *")
            val telephoneText = formatterClass.findTextViewByText(binding.rootLayout, "Telephone *")
            var countryCodePicker: com.hbb20.CountryCodePicker? = null

            for (i in 0 until binding.rootLayout.childCount) {
                val childView = binding.rootLayout.getChildAt(i)

                if (childView is LinearLayout){

                    for (j in 0 until childView.childCount) {
                        val innerChildView = childView.getChildAt(j)

                        //Check if the inner child view is com.hbb20.CountryCodePicker
                        if (innerChildView is com.hbb20.CountryCodePicker) {
                            countryCodePicker = innerChildView as com.hbb20.CountryCodePicker
                            break
                        }

                    }

                }



            }

            when (selectedItem) {

                "Yes" -> {
                    fullName.visibility = View.VISIBLE
                    relationship.visibility = View.VISIBLE
                    telephone.visibility = View.VISIBLE

                    fullNameText?.visibility = View.VISIBLE
                    relationshipText?.visibility = View.VISIBLE
                    telephoneText?.visibility = View.VISIBLE
                    countryCodePicker?.visibility = View.VISIBLE
                }

                "No" -> {
                    fullName.visibility = View.GONE
                    relationship.visibility = View.GONE
                    telephone.visibility = View.GONE

                    fullNameText?.visibility = View.GONE
                    relationshipText?.visibility = View.GONE
                    telephoneText?.visibility = View.GONE
                    countryCodePicker?.visibility = View.GONE
                }

            }


        }

        FormUtils.loadFormData(
            requireContext(),
            binding.rootLayout,
            DbNavigationDetails.PATIENT_REGISTRATION.name,
            DbClasses.NEXT_OF_KIN.name
        )


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}