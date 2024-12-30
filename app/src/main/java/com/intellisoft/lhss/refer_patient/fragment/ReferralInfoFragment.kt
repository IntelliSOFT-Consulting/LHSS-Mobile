package com.intellisoft.lhss.refer_patient.fragment

import android.app.Application
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.fhir.FhirEngine
import com.google.gson.Gson
import com.intellisoft.lhss.LocationViewModel
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
import com.intellisoft.lhss.fhir.Constants
import com.intellisoft.lhss.fhir.FhirApplication
import com.intellisoft.lhss.refer_patient.viewmodel.ReferralInfoViewModel
import com.intellisoft.lhss.referrals.viewmodels.ReferralDetailsViewModel
import com.intellisoft.lhss.referrals.viewmodels.ReferralDetailsViewModelFactory
import com.intellisoft.lhss.shared.DbLocationResponse
import com.intellisoft.lhss.shared.FormatterClass
import com.intellisoft.lhss.shared.LocationDetails

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
    private var startDate: String? = null
    private var endDate: String? = null
    private lateinit var referralViewModel: ReferralDetailsViewModel
    private lateinit var fhirEngine: FhirEngine
    private var patientId:String = ""
    private var serviceRequestId:String = ""
    private lateinit var locationViewModel: LocationViewModel
    val countryList = listOf("Kenya", "Uganda", "Tanzania")


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

        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientId = formatterClass.getSharedPref("", "patientId") ?: ""
        serviceRequestId = formatterClass.getSharedPref("", "serviceRequestId") ?: ""

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

        locationViewModel =
            ViewModelProvider(
                this,
                LocationViewModel.LocationViewModelFactory(
                    requireActivity().application,
                    fhirEngine
                ),
            )[LocationViewModel::class.java]

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

        val gson = Gson()

        var referralDate = ""

        val savedJson = formatterClass.getSharedPref(
            DbNavigationDetails.REFER_PATIENT.name,
            DbClasses.REFERRING_FACILITY_INFO.name)

        val formDataFromJson = gson.fromJson(savedJson, FormData::class.java)

        formDataFromJson?.formDataList?.forEach {
            val tag = it.tag
            val text = it.text
            if (tag == "Date of Referral") referralDate = text
        }

        if (referralDate != "") {
            val newDate = formatterClass.convertDateFormat(referralDate)
            if (newDate!= null) {
                startDate = newDate
            }
        }

        // Initialize FieldManager with dependencies (inject via constructor or manually)
        fieldManager = FieldManager(DefaultLabelCustomizer(), requireContext())


        val dbFieldList = listOf(
            DbField(
                DbWidgets.SPINNER.name,
                "Country of Receiving Facility", true, null,
                countryList
            ),
            DbField(
                DbWidgets.SPINNER.name,
                "Region/Province/County of Receiving Facility", true, null,
                emptyList()
            ),
            DbField(
                DbWidgets.SPINNER.name,
                "District/Sub County of Receiving Facility", true, null,
                emptyList()
            ),
            DbField(
                DbWidgets.SPINNER.name,
                "Ward of Receiving Facility", true, null,
                emptyList()
            ),


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
                true,
                startDate
            )

        )

        FormUtils.populateView(ArrayList(dbFieldList), binding.rootLayout, fieldManager, requireContext())

        setSpinnerListener(
            listOf(
                "Country of Receiving Facility",
                "Region/Province/County of Receiving Facility"
            )
        )

        FormUtils.loadFormData(
            requireContext(),
            binding.rootLayout,
            DbNavigationDetails.REFER_PATIENT.name,
            DbClasses.REFERRAL_INFO.name
        )

        clinicalInfoViewViewModel.selectedItem.observe(viewLifecycleOwner) { selectedItem ->

            val countryReceiving = binding.rootLayout.findViewWithTag<View>("Country of Receiving Facility") as Spinner
            val regionReceiving = binding.rootLayout.findViewWithTag<View>("Region/Province/County of Receiving Facility") as Spinner
            val districtReceiving = binding.rootLayout.findViewWithTag<View>("District/Sub County of Receiving Facility") as Spinner
            val wardReceiving = binding.rootLayout.findViewWithTag<View>("Ward of Receiving Facility") as Spinner

            val countryName = if (selectedItem == "Kenya"){
                "0"
            }else {
                selectedItem
            }

            val locationList = locationViewModel
                .getHierarchyDetails("Location/$countryName","")

            val codeName = locationList.firstOrNull()?.code
            if (codeName != null){
                when (codeName) {
                    LocationDetails.WARD.name -> {
                        populateSpinner(wardReceiving, locationList)
                    }
                    "SUB-COUNTY", LocationDetails.DISTRICT.name -> {
                        populateSpinner(districtReceiving, locationList)
                    }
                    LocationDetails.COUNTY.name, LocationDetails.REGION.name -> {
                        populateSpinner(regionReceiving, locationList)
                    }
                }
            }



            Log.e("---------->","<----------")
            println("selectedItem $selectedItem")
            Log.e("---------->","<----------")

        }

    }

    private fun populateSpinner(spinner: Spinner, data: List<DbLocationResponse>) {
        val dataList = data.map { it.name }
        // Create an ArrayAdapter using the string list and a default spinner layout
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dataList)

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        spinner.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}