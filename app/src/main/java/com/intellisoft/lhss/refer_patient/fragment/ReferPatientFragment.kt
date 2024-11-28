package com.intellisoft.lhss.refer_patient.fragment

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.intellisoft.lhss.R
import com.intellisoft.lhss.databinding.FragmentReferPatientBinding
import com.intellisoft.lhss.shared.DbClasses
import com.intellisoft.lhss.shared.DbField
import com.intellisoft.lhss.shared.DbNavigationDetails
import com.intellisoft.lhss.shared.DbWidgets
import com.intellisoft.lhss.dynamic_components.DefaultLabelCustomizer
import com.intellisoft.lhss.dynamic_components.FieldManager
import com.intellisoft.lhss.shared.FormData
import com.intellisoft.lhss.dynamic_components.FormUtils
import com.intellisoft.lhss.refer_patient.viewmodel.ReferPatientViewModel
import com.intellisoft.lhss.shared.FormatterClass

class ReferPatientFragment : Fragment() {

    private var _binding: FragmentReferPatientBinding? = null
    private val binding get() = _binding!!
    private lateinit var fieldManager: FieldManager

    private val viewModel: ReferPatientViewModel by viewModels()
    private val countryList = listOf("Djibouti", "Eritrea", "Kenya", "Ethiopia", "Somalia", "South Sudan", "Sudan", "Uganda")
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

            findNavController().navigate(R.id.action_referPatientFragment_to_referralInfoFragment)

            // Handle next button click
            // Navigate to the next fragment or perform any action
            val (addedFields, missingFields) = FormUtils.extractAllFormData(binding.rootLayout)

            Log.e("------>","<------")
            println("addedFields $addedFields")

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
                com.intellisoft.lhss.fhir.Constants.REFERRAL_DATE,
                true
            ),
            DbField(
                DbWidgets.SPINNER.name,
                "Country", true, null,
                countryList),
//            DbField(
//                DbWidgets.SPINNER.name,
//                "Region/Province/County", true, null,
//                countryList),
//            DbField(
//                DbWidgets.SPINNER.name,
//                "District/Sub County", true, null,
//                countryList),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Region/Province/County", true,
                InputType.TYPE_CLASS_TEXT
            ),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "District/Sub County", true,
                InputType.TYPE_CLASS_TEXT
            ),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Ward", true,
                InputType.TYPE_CLASS_TEXT
            ),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Name of Referring Officer", true,
                InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            ),
            DbField(
                DbWidgets.SPINNER.name,
                "Name of Receiving facility", true, null,
                listOf("Pumwani", "Avenue Hospital"), true,
                com.intellisoft.lhss.fhir.Constants.RECEIVING_FACILITY_NAME,
            ),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Name of Receiving facility", true,
                InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            ),
            DbField(
                DbWidgets.SPINNER.name,
                "Title", true, null,
                titleList),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Telephone", true,
                InputType.TYPE_CLASS_PHONE
            ),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Email", false,
                InputType.TYPE_CLASS_TEXT
            ),

        )

        FormUtils.populateView(ArrayList(dbFieldList), binding.rootLayout, fieldManager, requireContext())

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