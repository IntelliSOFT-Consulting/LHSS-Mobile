package com.intellisoft.lhss25.registration.fragment

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.intellisoft.lhss25.R
import com.intellisoft.lhss25.clinical_info.viewmodel.ClinicalInfoViewViewModel
import com.intellisoft.lhss25.databinding.FragmentDemographicsBinding
import com.intellisoft.lhss25.shared.DbClasses
import com.intellisoft.lhss25.shared.DbField
import com.intellisoft.lhss25.shared.DbNavigationDetails
import com.intellisoft.lhss25.shared.DbWidgets
import com.intellisoft.lhss25.dynamic_components.DefaultLabelCustomizer
import com.intellisoft.lhss25.dynamic_components.DefaultSpinnerSelectionHandler
import com.intellisoft.lhss25.dynamic_components.FieldManager
import com.intellisoft.lhss25.shared.FormData
import com.intellisoft.lhss25.dynamic_components.FormUtils
import com.intellisoft.lhss25.dynamic_components.FormUtils.extractAllFormData
import com.intellisoft.lhss25.dynamic_components.FormUtils.loadFormData
import com.intellisoft.lhss25.dynamic_components.MandatoryRadioGroup
import com.intellisoft.lhss25.dynamic_components.SpinnerSelectionHandler
import com.intellisoft.lhss25.shared.FormatterClass
import com.intellisoft.lhss25.shared.MainActivityViewModel

class DemographicsFragment : Fragment() {

    private var _binding: FragmentDemographicsBinding? = null
    private val binding get() = _binding!!
    private lateinit var fieldManager: FieldManager
    private lateinit var formatterClass: FormatterClass

    private val viewModel: MainActivityViewModel by viewModels()
    private var identificationTypes =
        listOf("Birth Certificate", "National ID", "Passport","Drivers License", "None")
    private val clinicalInfoViewViewModel: ClinicalInfoViewViewModel by viewModels()
    private val spinnerSelectionHandler: SpinnerSelectionHandler = DefaultSpinnerSelectionHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDemographicsBinding.inflate(inflater, container, false)

        navigationActions()


        formatterClass = FormatterClass(requireContext())

        val workflowTitles = formatterClass.getWorkflowTitles(DbClasses.DEMOGRAPHICS.name)
        if (workflowTitles != null){
            binding.tvTitle.text = formatterClass.toSentenceCase(workflowTitles.text)
            binding.imgBtn.setImageResource(workflowTitles.iconId)
        }


        return binding.root

    }



    private fun navigationActions() {
        // Set the next button text to "Continue" and add click listeners
        val navigationButtons = binding.navigationButtons
        navigationButtons.setNextButtonText("Next")

        navigationButtons.setBackButtonClickListener {
            // Handle back button click
            findNavController().navigateUp()
        }

        navigationButtons.setNextButtonClickListener {
            // Handle next button click
            // Navigate to the next fragment or perform any action
            val gson = Gson()


            // Call the function to extract form data
            val (addedFields, missingFields) = extractAllFormData(binding.rootLayout)

            if (missingFields.isNotEmpty()){

                var missingText = ""
                missingFields.forEach { missingText += "\n ${it.tag}, " }

                val mandatoryText = "The following are mandatory fields and " +
                        "need to be filled before proceeding: \n" +
                        missingText

                formatterClass.showDialog("Missing Content", mandatoryText)

            }else{

                val telephoneReferringData = addedFields.find { it.tag == "Telephone in referring country" }
                val documentNumber = addedFields.find { it.tag == "Document Number" }

//                val telephoneReceivingData = addedFields.find { it.tag == "Telephone in receiving country" }

                if (telephoneReferringData != null){

                    val textReferringNumber = telephoneReferringData.text
//                    val textReceivingNumber = telephoneReceivingData.text

                    val isReferringPhoneValid = formatterClass.getStandardPhoneNumber(textReferringNumber)
//                    val isReceivingPhoneValid = formatterClass.getStandardPhoneNumber(textReceivingNumber)

                    if (isReferringPhoneValid){
                        findNavController().navigate(R.id.action_demographicsFragment_to_addressFragment)

                        val formData = FormData(
                            DbClasses.DEMOGRAPHICS.name,
                            addedFields)

                        val json = gson.toJson(formData)

                        formatterClass.saveSharedPref(
                            sharedPrefName = DbNavigationDetails.PATIENT_REGISTRATION.name,
                            DbClasses.DEMOGRAPHICS.name,
                            json
                        )
                    }else{
                        Toast.makeText(context, "You have provided an invalid phone number", Toast.LENGTH_LONG).show()
                    }
                }else{

//                    if (documentNumber == null || documentNumber.text.length > 9)
//                        Toast.makeText(context,
//                            "The document number is not correct. Check if the number is less than 9 characters.",
//                            Toast.LENGTH_LONG).show()

                    Toast.makeText(context,
                        "The telephone number in referring country is not provided.",
                        Toast.LENGTH_LONG).show()
                }
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
                "First Name", true,
                InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            ),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Middle Name", false,
                InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            ),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Last Name", true,
                InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            ),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "NickName", false,
                InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            ),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Telephone in referring country", true,
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
                "Telephone in receiving country", false,
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
                "Document Type", false, null,
                identificationTypes
            ),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Document Number", false, // Have this at 9 characters for now
                InputType.TYPE_CLASS_NUMBER
            ),
            DbField(
                DbWidgets.SPINNER.name,
                "Sex", true, null,
                listOf("Male", "Female")
            )


        )

        FormUtils.populateView(ArrayList(dbFieldList), binding.rootLayout, fieldManager, requireContext())

        // Call the function to add the RadioButtons and TextView dynamically
        // On picking the dates have it start at it
        formatterClass.addRadioButtonWithDatePicker(requireContext(), binding.rootLayout)

        setSpinnerListener(
            listOf("Document Type")
        )

        clinicalInfoViewViewModel.selectedItem.observe(viewLifecycleOwner) { selectedItem ->

            val documentNumberText = formatterClass.findTextViewByText(binding.rootLayout, "Document Number")
            val documentNumberOthers = binding.rootLayout.findViewWithTag<View>("Document Number")

            if (selectedItem == "None"){
                documentNumberText?.visibility = View.GONE
                documentNumberOthers?.visibility = View.GONE
            }else{
                documentNumberText?.visibility = View.VISIBLE
                documentNumberOthers?.visibility = View.VISIBLE
            }

        }

        //Repopulate date of birth

        loadFormData(
            requireContext(),
            binding.rootLayout,
            DbNavigationDetails.PATIENT_REGISTRATION.name,
            DbClasses.DEMOGRAPHICS.name
        )

        // Traverse through all child views of rootLayout
//        for (i in 0 until binding.rootLayout.childCount) {
//            val childView = binding.rootLayout.getChildAt(i)
//            when (childView) {
//
//                is LinearLayout -> {
//
//                    for (j in 0 until childView.childCount) {
//                        val innerChild = childView.getChildAt(j)
//
//                        when (innerChild) {
//                            is MandatoryRadioGroup -> {
//
//                                val selectedText = innerChild.getSelectedRadioButtonText()
//                                val isMandatory = innerChild.isMandatory
//                                val tag = innerChild.tag?.toString() ?: ""
//
//                                Log.e(">>>>>>>", "<<<<<<<<")
//                                println("selectedText $selectedText")
//                                println("tag $tag")
//                                Log.e(">>>>>>>", "<<<<<<<<")
//                            }
//                        }
//
//
//                    }
//                }
//            }
//        }


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