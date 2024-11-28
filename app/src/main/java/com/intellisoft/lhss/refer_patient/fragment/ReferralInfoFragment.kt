package com.intellisoft.lhss.refer_patient.fragment

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.intellisoft.lhss.R
import com.intellisoft.lhss.clinical_info.viewmodel.ClinicalInfoViewViewModel
import com.intellisoft.lhss.databinding.FragmentReferralInfoBinding
import com.intellisoft.lhss.shared.DbClasses
import com.intellisoft.lhss.shared.DbField
import com.intellisoft.lhss.shared.DbNavigationDetails
import com.intellisoft.lhss.shared.DbWidgets
import com.intellisoft.lhss.dynamic_components.DefaultLabelCustomizer
import com.intellisoft.lhss.dynamic_components.DefaultSpinnerSelectionHandler
import com.intellisoft.lhss.dynamic_components.FieldManager
import com.intellisoft.lhss.shared.FormData
import com.intellisoft.lhss.dynamic_components.FormUtils
import com.intellisoft.lhss.dynamic_components.SpinnerSelectionHandler
import com.intellisoft.lhss.refer_patient.viewmodel.ReferralInfoViewModel
import com.intellisoft.lhss.shared.FormatterClass

class ReferralInfoFragment : Fragment() {

    private var _binding: FragmentReferralInfoBinding? = null
    private val binding get() = _binding!!
    private lateinit var fieldManager: FieldManager
    private val clinicalInfoViewViewModel: ClinicalInfoViewViewModel by viewModels()
    private val spinnerSelectionHandler: SpinnerSelectionHandler = DefaultSpinnerSelectionHandler()

    private val viewModel: ReferralInfoViewModel by viewModels()
    private var referralReasonList = listOf(
        "Leave", "Holidays", "Permanent  Return", "Medical", "Work", "Others")
    private lateinit var formatterClass: FormatterClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentReferralInfoBinding.inflate(inflater, container, false)
        navigationActions()
        formatterClass = FormatterClass(requireContext())

        val workflowTitles = formatterClass.getWorkflowTitles(DbClasses.REFERRAL_INFO.name)
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
        navigationButtons.setNextButtonText("Next")

        navigationButtons.setBackButtonClickListener {
            // Handle back button click
            findNavController().navigateUp()
        }

        navigationButtons.setNextButtonClickListener {
            // Handle next button click
            // Navigate to the next fragment or perform any action

            val (addedFields, missingFields) =
                FormUtils.extractAllFormData(binding.rootLayout)

            if (missingFields.isNotEmpty()){
                var missingText = ""
                missingFields.forEach { missingText += "\n ${it.tag}, " }

                val mandatoryText = "The following are mandatory fields and " +
                        "need to be filled before proceeding: \n" +
                        missingText

                formatterClass.showDialog("Missing Content", mandatoryText)
            }else{
                findNavController().navigate(R.id.action_referralInfoFragment_to_reviewReferFragment)

                val formData = FormData(
                    DbClasses.REFERRAL_INFO.name,
                    addedFields)

                val gson = Gson()
                val json = gson.toJson(formData)

                formatterClass.saveSharedPref(
                    sharedPrefName = DbNavigationDetails.REFER_PATIENT.name,
                    DbClasses.REFERRAL_INFO.name,
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
                DbWidgets.SPINNER.name,
                "Reason for Referral", true, null,
                referralReasonList),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Specify Other Referral Reasons", false,
                InputType.TYPE_CLASS_TEXT
            ),
            DbField(
                DbWidgets.DATE_PICKER.name,
                "Projected Time of Return",
                true,
                null,
                emptyList(),
                true,
                "",
                false,
            )

        )

        FormUtils.populateView(ArrayList(dbFieldList), binding.rootLayout, fieldManager, requireContext())

        setSpinnerListener(
            listOf("Reason for Referral")
        )

        FormUtils.loadFormData(
            requireContext(),
            binding.rootLayout,
            DbNavigationDetails.REFER_PATIENT.name,
            DbClasses.REFERRAL_INFO.name
        )

        clinicalInfoViewViewModel.selectedItem.observe(viewLifecycleOwner) { selectedItem ->
            val otherReasons = binding.rootLayout.findViewWithTag<View>("Specify Other Referral Reasons")
            val otherReasonsText = formatterClass.findTextViewByText(binding.rootLayout, "Specify Other Referral Reasons")

            when (selectedItem) {
                "Others" -> {
                    otherReasons?.visibility = View.VISIBLE
                    otherReasonsText?.visibility = View.VISIBLE
                }
                else -> {
                    otherReasons?.visibility = View.GONE
                    otherReasonsText?.visibility = View.GONE
                }
            }

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}