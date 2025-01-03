package com.intellisoft.lhss25.refer_patient.fragment

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
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.intellisoft.lhss25.R
import com.intellisoft.lhss25.databinding.FragmentReferPatientBinding
import com.intellisoft.lhss25.shared.DbClasses
import com.intellisoft.lhss25.shared.DbField
import com.intellisoft.lhss25.shared.DbNavigationDetails
import com.intellisoft.lhss25.shared.DbWidgets
import com.intellisoft.lhss25.dynamic_components.DefaultLabelCustomizer
import com.intellisoft.lhss25.dynamic_components.FieldManager
import com.intellisoft.lhss25.shared.FormData
import com.intellisoft.lhss25.dynamic_components.FormUtils
import com.intellisoft.lhss25.refer_patient.viewmodel.ReferPatientViewModel
import com.intellisoft.lhss25.shared.DbWorkFlow
import com.intellisoft.lhss25.shared.FormatterClass

class ReferPatientFragment : Fragment() {

    private var _binding: FragmentReferPatientBinding? = null
    private val binding get() = _binding!!
    private lateinit var fieldManager: FieldManager

    private val viewModel: ReferPatientViewModel by viewModels()
    private val countryList = listOf("Kenya", "Uganda")
    private val titleList = listOf("Mr", "Miss", "Mrs", "Dr")
    private lateinit var formatterClass: FormatterClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentReferPatientBinding.inflate(inflater, container, false)

        navigationActions()
        formatterClass = FormatterClass(requireContext())

        val workflowTitles = formatterClass.getWorkflowTitles(DbClasses.REFERRING_FACILITY_INFO.name)
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
            val (addedFields, missingFields) = FormUtils.extractAllFormData(binding.rootLayout)


            if (missingFields.isNotEmpty()){
                var missingText = ""
                missingFields.forEach { missingText += "\n ${it.tag}, " }

                val mandatoryText = "The following are mandatory fields and " +
                        "need to be filled before proceeding: \n" +
                        missingText

                formatterClass.showDialog("Missing Content", mandatoryText)
            }else{

                val telephoneData = addedFields.find { it.tag == "Telephone" }
                val emailData = addedFields.find { it.tag == "Email" }


                if (telephoneData != null){
                    val textNumber = telephoneData.text
                    val isPhoneValid = formatterClass.getStandardPhoneNumber(textNumber)

                    if (isPhoneValid){
                        findNavController().navigate(R.id.action_referPatientFragment_to_referralInfoFragment)

                        val formData = FormData(
                            DbClasses.REFERRING_FACILITY_INFO.name,
                            addedFields)

                        val gson = Gson()
                        val json = gson.toJson(formData)

                        formatterClass.saveSharedPref(
                            sharedPrefName = DbNavigationDetails.REFER_PATIENT.name,
                            DbClasses.REFERRING_FACILITY_INFO.name,
                            json
                        )
                    }else{
                        if (!isPhoneValid) Toast.makeText(context, "You have provided an invalid phone number", Toast.LENGTH_LONG).show()
                    }
                }else{
                    Toast.makeText(context, "You have not provided a telephone", Toast.LENGTH_LONG).show()
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
                DbWidgets.DATE_PICKER.name,
                "Date of Referral",
                true,
                null,
                emptyList(),
                true,
                com.intellisoft.lhss25.fhir.Constants.REFERRAL_DATE,
                true,
            ),
//            DbField(
//                DbWidgets.SPINNER.name,
//                "Country", true, null,
//                countryList),
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
//                "District/Sub County", true,
//                InputType.TYPE_CLASS_TEXT
//            ),
//            DbField(
//                DbWidgets.EDIT_TEXT.name,
//                "Ward", true,
//                InputType.TYPE_CLASS_TEXT
//            ),
//            DbField(
//                DbWidgets.SPINNER.name,
//                "Name of Receiving facility", true, null,
//                availableLocations, true,
//                com.intellisoft.lhss.fhir.Constants.RECEIVING_FACILITY_NAME,
//            ),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Name of Referring Officer", true,
                InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            ),
//            DbField(
//                DbWidgets.EDIT_TEXT.name,
//                "Name of Receiving facility", true,
//                InputType.TYPE_TEXT_VARIATION_PERSON_NAME
//            ),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Tb Focal Person", true,
                InputType.TYPE_CLASS_TEXT
            ),
            DbField(
                DbWidgets.SPINNER.name,
                "Title", true, null,
                titleList),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Telephone", true,
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
                "Email", false,
                InputType.TYPE_CLASS_TEXT
            ),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Comments", false,
                InputType.TYPE_CLASS_TEXT
            ),

        )

        FormUtils.populateView(ArrayList(dbFieldList), binding.rootLayout, fieldManager, requireContext())

        val rootViewParentReferralCountry = binding.rootLayout.findViewWithTag<View>("Country")
        val rootViewParentReferralCounty = binding.rootLayout.findViewWithTag<View>("Region/Province/County")
        val rootViewParentReferralSubCountry = binding.rootLayout.findViewWithTag<View>("District/Sub County")
        val rootViewParentReferralWard = binding.rootLayout.findViewWithTag<View>("Ward")
//        val rootViewParentReferralNameReceivingFacility = binding.rootLayout.findViewWithTag<View>("Name of Receiving facility")

        val referralList = ArrayList<DbWorkFlow>()

//        referralList.add(DbWorkFlow(rootViewParentReferralNameReceivingFacility, formatterClass.getSharedPref("", "userFullName") ?: ""))

        referralList.add(DbWorkFlow(rootViewParentReferralCountry, formatterClass.getSharedPref("", "userCountry") ?: ""))
        referralList.add(DbWorkFlow(rootViewParentReferralCounty, formatterClass.getSharedPref("", "userCountyName") ?: ""))

//        referralList.add(DbWorkFlow(rootViewParentReferralWard, formatterClass.getSharedPref("", "userWardName") ?: ""))

        referralList.add(DbWorkFlow(rootViewParentReferralSubCountry,
            formatterClass.getSharedPref("", "userSubCountyName") ?:
            formatterClass.getSharedPref("", "userRegionName") ?: "")
        )

        referralList.forEach { view ->

            val viewData = view.view
            val value = view.value

            if (viewData != null) {

                //Check if rootViewParent is EditText and set its text from the retrieved observation
                if (viewData is EditText) {
                    viewData.setText(value)
                    viewData.isEnabled = false
                    viewData.setTypeface(viewData.typeface, Typeface.BOLD)
                    //Set the color to bold
                    viewData.setTextColor(Color.BLACK)
                }
            }

        }



        FormUtils.loadFormData(
            requireContext(),
            binding.rootLayout,
            DbNavigationDetails.REFER_PATIENT.name,
            DbClasses.REFERRING_FACILITY_INFO.name
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}