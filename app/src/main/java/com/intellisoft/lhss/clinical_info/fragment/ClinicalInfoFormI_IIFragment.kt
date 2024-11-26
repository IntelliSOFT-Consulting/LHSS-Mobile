package com.intellisoft.lhss.clinical_info.fragment

import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.fhir.FhirEngine
import com.google.gson.Gson
import com.intellisoft.lhss.R
import com.intellisoft.lhss.clinical_info.viewmodel.ClinicalInfoViewViewModel
import com.intellisoft.lhss.databinding.FragmentClinicalInfoFormIIIBinding
import com.intellisoft.lhss.dynamic_components.DefaultLabelCustomizer
import com.intellisoft.lhss.dynamic_components.DefaultSpinnerSelectionHandler
import com.intellisoft.lhss.dynamic_components.FieldManager
import com.intellisoft.lhss.dynamic_components.FormUtils
import com.intellisoft.lhss.dynamic_components.SpinnerSelectionHandler
import com.intellisoft.lhss.fhir.Constants
import com.intellisoft.lhss.shared.DbClasses
import com.intellisoft.lhss.shared.DbField
import com.intellisoft.lhss.shared.DbNavigationDetails
import com.intellisoft.lhss.shared.DbWidgets
import com.intellisoft.lhss.shared.FormData
import com.intellisoft.lhss.shared.FormDataAdapter
import com.intellisoft.lhss.shared.FormatterClass

class ClinicalInfoFormI_IIFragment : Fragment() {

    private var _binding: FragmentClinicalInfoFormIIIBinding? = null
    private val binding get() = _binding!!
    private lateinit var fieldManager: FieldManager
    private var formDataList = ArrayList<FormData>()
    private lateinit var formDataAdapter: FormDataAdapter
    private lateinit var fhirEngine: FhirEngine
    private lateinit var formatterClass: FormatterClass
    private var patientId:String = ""
    private var serviceRequestId:String = ""
    private var workflowTitles:String = ""
    private val tbTypeList = listOf("PTB" , "EPTB")
    private val monthList = listOf("2 Weeks","1 month", "2 months","3 months", "4 months",
        "5 months", "6 months", "7 months", "8 months", "9 months", "10 months",
        "11 months", "12 months")
    private val clinicalInfoViewViewModel: ClinicalInfoViewViewModel by viewModels()
    private val spinnerSelectionHandler: SpinnerSelectionHandler = DefaultSpinnerSelectionHandler()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = FragmentClinicalInfoFormIIIBinding.inflate(inflater, container, false)
        formatterClass = FormatterClass(requireContext())
        navigationActions()
        patientId = formatterClass.getSharedPref("", "patientId")?: ""
        serviceRequestId = formatterClass.getSharedPref("", "serviceRequestId")?: ""
        workflowTitles = formatterClass.getSharedPref("", "CLINICAL_REFERRAL")?: ""

        if (workflowTitles != ""){
            var title = formatterClass.toSentenceCase(workflowTitles)
            if (workflowTitles == DbClasses.TB_TREATMENT.name){
                title = "Tb Treatment"
            }
            binding.tvTitle.text = title
//            binding.imgBtn.setImageResource(workflowTitles)
        }


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
                findNavController().navigate(
                    R.id.action_clinicalInfoFormI_IIFragment_to_clinicalInfoReview)

                val formData = FormData(
                    workflowTitles.toString(),
                    addedFields)

                val gson = Gson()
                val json = gson.toJson(formData)

                formatterClass.saveSharedPref(
                    sharedPrefName = DbNavigationDetails.REFER_PATIENT.name,
                    workflowTitles.toString(),
                    json
                )

            }
        }
    }

    private fun loadFormData(): List<DbField> {
        if (workflowTitles == DbClasses.TB_TREATMENT.name) {
            val dbFieldList = listOf(
                DbField(
                    DbWidgets.EDIT_TEXT.name,
                    "TB Registration Number", true,
                    InputType.TYPE_CLASS_TEXT,
                    emptyList(),
                    true,
                    Constants.SYSTEM_TB_REGISTRATION
                ),
                DbField(
                    DbWidgets.DATE_PICKER.name,
                    "TB Registration Date",
                    true
                ),
                DbField(
                    DbWidgets.SPINNER.name,
                    "Type of Patient",
                    true,
                    optionList = listOf("New", "Previously Treated")
                ),
                DbField(
                    DbWidgets.EDIT_TEXT.name,
                    "Specify Site (EPTB)", false,
                    InputType.TYPE_CLASS_TEXT
                ),
                DbField(
                    DbWidgets.SPINNER.name,
                    "Patient Classification",
                    true,
                    optionList = listOf("DSTB", "DRTB", "TPT")
                ),
                DbField(
                    DbWidgets.SPINNER.name,
                    "Type of TB", true, null,
                    tbTypeList),
                DbField(
                    DbWidgets.EDIT_TEXT.name,
                    "Radiological Information (If Applicable)", false,
                    InputType.TYPE_CLASS_TEXT
                ),
                DbField(
                    DbWidgets.DATE_PICKER.name,
                    "Tb Treatment initiation date",
                    true
                ),
                DbField(
                    DbWidgets.SPINNER.name,
                    "Month of Treatment", true, null,
                    monthList
                ),
                DbField(
                    DbWidgets.SPINNER.name,
                    "Regimen", true, null,
                    listOf("2RHZE/4RH", "2RHZE/1ORH", "2RHZE/2RH", "Others (Specify Regimen)")
                ),
                DbField(
                    DbWidgets.EDIT_TEXT.name,
                    "Other Specify DRTB Regimen", false,
                    InputType.TYPE_CLASS_TEXT
                ),
                DbField(
                    DbWidgets.EDIT_TEXT.name,
                    "Drugs issued for (Indicate duration)", false,
                    InputType.TYPE_CLASS_TEXT
                )

            )
            return dbFieldList
        }
        if (workflowTitles == DbClasses.HIV_STATUS_TREATMENT.name) {
            val dbFieldList = listOf(
                DbField(
                    DbWidgets.SPINNER.name,
                    "HIV Status", true, null,
                    listOf("Negative", "Positive", "Unknown")
                ),
                DbField(
                    DbWidgets.EDIT_TEXT.name,
                    "ART Regimen", false,
                    InputType.TYPE_CLASS_TEXT
                ),
                DbField(
                    DbWidgets.EDIT_TEXT.name,
                    "CD4 Count", false, //Should be a number
                    InputType.TYPE_CLASS_NUMBER
                ),
                DbField(
                    DbWidgets.EDIT_TEXT.name,
                    "Viral Load", false, //Should be a number
                    InputType.TYPE_CLASS_NUMBER
                ),
                DbField(
                    DbWidgets.EDIT_TEXT.name,
                    "Others", false,
                    InputType.TYPE_CLASS_TEXT
                )
            )


            return dbFieldList
        }

        return emptyList()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize FieldManager with dependencies (inject via constructor or manually)
        fieldManager = FieldManager(DefaultLabelCustomizer(), requireContext())

        val dbFieldList = loadFormData()


        FormUtils.populateView(ArrayList(dbFieldList),
            binding.rootLayout, fieldManager, requireContext())

        // Make a function that will handle multiple tags from a

        setSpinnerListener(
            listOf("HIV Status", "Tb Location", "Regimen")
        )


        // Observe the LiveData and react to changes
        clinicalInfoViewViewModel.selectedItem.observe(viewLifecycleOwner) { selectedItem ->
            // Update UI or perform actions based on selected item
            // For example, updating edit texts based on spinner selection
            val artRegimen = binding.rootLayout.findViewWithTag<View>("ART Regimen")
            val cd4Count = binding.rootLayout.findViewWithTag<View>("CD4 Count")
            val viralLoad = binding.rootLayout.findViewWithTag<View>("Viral Load")

            val artRegimenText = formatterClass.findTextViewByText(binding.rootLayout, "ART Regimen")
            val cd4CountText = formatterClass.findTextViewByText(binding.rootLayout, "CD4 Count")
            val viralLoadText = formatterClass.findTextViewByText(binding.rootLayout, "Viral Load")

            val eptbSiteText = formatterClass.findTextViewByText(binding.rootLayout, "Specify Site (EPTB)")
            val eptbSite = binding.rootLayout.findViewWithTag<View>("Specify Site (EPTB)")

            val regimenOthersText = formatterClass.findTextViewByText(binding.rootLayout, "Other Specify DRTB Regimen")
            val regimenOthers = binding.rootLayout.findViewWithTag<View>("Other Specify DRTB Regimen")


            //Use when for below
            when (selectedItem) {
                "Others (Specify Regimen)" -> {
                    regimenOthersText?.visibility = View.VISIBLE
                    regimenOthers?.visibility = View.VISIBLE
                }
                "2RHZE/4RH","2RHZE/1ORH","2RHZE/2RH" -> {
                    regimenOthersText?.visibility = View.GONE
                    regimenOthers?.visibility = View.GONE
                }
                "PTB" -> {
                    eptbSite?.visibility = View.GONE
                    eptbSiteText?.visibility = View.GONE
                }
                "EPTB" -> {
                    eptbSite?.visibility = View.VISIBLE
                    eptbSiteText?.visibility = View.VISIBLE
                }
                "Positive" -> {
                    artRegimen?.visibility = View.VISIBLE
                    artRegimenText?.visibility = View.VISIBLE

                    cd4Count?.visibility = View.VISIBLE
                    cd4CountText?.visibility = View.VISIBLE

                    viralLoad?.visibility = View.VISIBLE
                    viralLoadText?.visibility = View.VISIBLE
                }
                "Negative", "Unknown" -> {
                    artRegimen?.visibility = View.GONE
                    artRegimenText?.visibility = View.GONE

                    cd4Count?.visibility = View.GONE
                    cd4CountText?.visibility = View.GONE

                    viralLoad?.visibility = View.GONE
                    viralLoadText?.visibility = View.GONE
                }
            }


        }

        FormUtils.loadFormData(
            requireContext(),
            binding.rootLayout,
            DbNavigationDetails.REFER_PATIENT.name,
            workflowTitles.toString()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}