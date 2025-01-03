package com.intellisoft.lhss25.refer_patient.fragment

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.intellisoft.lhss25.refer_patient.viewmodel.ClinicalInfoIViewModel
import com.intellisoft.lhss25.R
import com.intellisoft.lhss25.databinding.FragmentClinicalInfoIBinding
import com.intellisoft.lhss25.shared.DbField
import com.intellisoft.lhss25.shared.DbWidgets
import com.intellisoft.lhss25.dynamic_components.DefaultLabelCustomizer
import com.intellisoft.lhss25.dynamic_components.FieldManager
import com.intellisoft.lhss25.dynamic_components.FormUtils
import com.intellisoft.lhss25.shared.FormatterClass

class ClinicalInfoIFragment : Fragment() {

    private var _binding: FragmentClinicalInfoIBinding? = null
    private val binding get() = _binding!!
    private lateinit var fieldManager: FieldManager

    private val viewModel: ClinicalInfoIViewModel by viewModels()
    private val tbTypeList = listOf("DSTB, DRTB","PTB , EPTB")
    private lateinit var formatterClass: FormatterClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentClinicalInfoIBinding.inflate(inflater, container, false)

        navigationActions()
        formatterClass = FormatterClass(requireContext())

//        val workflowTitles = formatterClass.getWorkflowTitles(DbClasses.CLINICAL_REFERRAL_I.name)
//        if (workflowTitles != null){
//            binding.tvTitle.text = formatterClass.toSentenceCase(workflowTitles.text)
//            binding.imgBtn.setImageResource(workflowTitles.iconId)
//        }

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
                findNavController().navigate(R.id.action_clinicalInfoIFragment_to_clnicalInfoIIFragment)

//                val formData = FormData(
//                    DbClasses.CLINICAL_REFERRAL_I.name,
//                    addedFields)
//
//                val gson = Gson()
//                val json = gson.toJson(formData)
//
//                formatterClass.saveSharedPref(
//                    sharedPrefName = DbNavigationDetails.REFER_PATIENT.name,
//                    DbClasses.CLINICAL_REFERRAL_I.name,
//                    json
//                )

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
                "TB Registration Number", true,
                InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            ),
            DbField(
                DbWidgets.DATE_PICKER.name,
                "TB Registration Date",
                true
            ),
            DbField(
                DbWidgets.RADIO_BUTTON.name,
                "Type of Patient",
                true,
                optionList = listOf("New", "Previously Treated")
            ),
            DbField(
                DbWidgets.SPINNER.name,
                "Types of TB", true, null,
                tbTypeList)
        )

        FormUtils.populateView(ArrayList(dbFieldList), binding.rootLayout, fieldManager, requireContext())

//        FormUtils.loadFormData(
//            requireContext(),
//            binding.rootLayout,
//            DbNavigationDetails.REFER_PATIENT.name,
//            DbClasses.CLINICAL_REFERRAL_I.name
//        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}