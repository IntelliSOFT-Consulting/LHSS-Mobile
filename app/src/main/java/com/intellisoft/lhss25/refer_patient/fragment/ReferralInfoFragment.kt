package com.intellisoft.lhss25.refer_patient.fragment

import android.app.Application
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.fhir.FhirEngine
import com.google.gson.Gson
import com.intellisoft.lhss25.LocationViewModel
import com.intellisoft.lhss25.R
import com.intellisoft.lhss25.clinical_info.viewmodel.ClinicalInfoViewViewModel
import com.intellisoft.lhss25.databinding.FragmentReferralInfoBinding
import com.intellisoft.lhss25.shared.DbClasses
import com.intellisoft.lhss25.shared.DbField
import com.intellisoft.lhss25.shared.DbNavigationDetails
import com.intellisoft.lhss25.shared.DbWidgets
import com.intellisoft.lhss25.dynamic_components.DefaultLabelCustomizer
import com.intellisoft.lhss25.dynamic_components.DefaultSpinnerSelectionHandler
import com.intellisoft.lhss25.dynamic_components.FieldManager
import com.intellisoft.lhss25.shared.FormData
import com.intellisoft.lhss25.dynamic_components.FormUtils
import com.intellisoft.lhss25.dynamic_components.SpinnerSelectionHandler
import com.intellisoft.lhss25.fhir.Constants
import com.intellisoft.lhss25.fhir.FhirApplication
import com.intellisoft.lhss25.refer_patient.viewmodel.ReferralInfoViewModel
import com.intellisoft.lhss25.referrals.viewmodels.ReferralDetailsViewModel
import com.intellisoft.lhss25.referrals.viewmodels.ReferralDetailsViewModelFactory
import com.intellisoft.lhss25.shared.DbLocationResponse
import com.intellisoft.lhss25.shared.FormatterClass
import com.intellisoft.lhss25.shared.LocationDetails

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

    private val countryList = listOf(
        DbLocationResponse("Kenya","","", "0"),
        DbLocationResponse("Uganda","","", "Uganda"),
    )



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
                    clinicalInfoViewViewModel.updateSelectedItem(selectedItem, rootViewParent)
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
                "Country of Receiving Facility",
                true,
                null,
                countryList.map { it.name },
                true,
                Constants.COUNTRY_RECEIVING
            ),
            DbField(
                DbWidgets.SPINNER.name,
                "Region/Province/County of Receiving Facility",
                true,
                null,
                emptyList(),
                true,
                Constants.REGION_COUNTY_RECEIVING
            ),
            DbField(
                DbWidgets.SPINNER.name,
                "District/Sub County of Receiving Facility",
                false,
                null,
                emptyList(),
                true,
                Constants.DISTRICT_SUB_COUNTY_RECEIVING
            ),
            DbField(
                DbWidgets.SPINNER.name,
                "Ward of Receiving Facility",
                false,
                null,
                emptyList(),
                true,
                Constants.WARD_RECEIVING
            ),
            DbField(
                DbWidgets.SPINNER.name,
                "Name of Receiving Facility",
                false,
                null,
                emptyList(),
                true,
                Constants.FACILITY_RECEIVING
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
                false,
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
                "Region/Province/County of Receiving Facility",
                "District/Sub County of Receiving Facility",
                "Ward of Receiving Facility",
                "Name of Receiving Facility"
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

            //Check if country is in countryList
            val countryId = countryList.firstOrNull { it.name == selectedItem }?.id
            //Set region and district dropdowns to empty
            if (countryId != null){
                val locationList = locationViewModel
                    .getHierarchyDetails("Location/$countryId","")
                val codeName = locationList.firstOrNull()?.code
                if (codeName!= null){
                    when (codeName) {
                        LocationDetails.COUNTY.name, LocationDetails.REGION.name -> {
                            populateSpinner(regionReceiving, locationList)

                            // Handle county/region selection dynamically
                            regionReceiving.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                    val selectedRegion = locationList[position].id
                                    //Use this to get the Districts
                                    val extractedId = selectedRegion?.split("/")?.get(1)

                                    if (extractedId != null) {
                                        fetchAndPopulateDistricts(extractedId, districtReceiving)
                                    }
                                }

                                override fun onNothingSelected(parent: AdapterView<*>?) {}
                            }
                        }
                    }
                }
            }


        }

    }

    /**
     * Function to fetch districts/sub-counties and populate the spinner
     */
    private fun fetchAndPopulateDistricts(regionProvinceId: String, districtSpinner: Spinner) {
        val districtList = locationViewModel
            .getHierarchyDetails("Location/$regionProvinceId", "")
        populateSpinner(districtSpinner, districtList)

        // Handle district/sub-county selection dynamically
        districtSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedDistrict = districtList[position].id
                val extractedId = selectedDistrict?.split("/")?.get(1)
                if (extractedId != null) {
                    fetchAndPopulateWards(extractedId)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    /**
     * Function to fetch wards and populate the spinner
     */
    private fun fetchAndPopulateWards(districtId: String) {
        val wardList = locationViewModel.getHierarchyDetails("Location/$districtId", "")
        val wardSpinner = binding.rootLayout.findViewWithTag<View>("Ward of Receiving Facility") as Spinner
        populateSpinner(wardSpinner, wardList)

        // Handle ward selection dynamically
        wardSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedWard = wardList[position].id
                val extractedId = selectedWard?.split("/")?.get(1)

                if (extractedId != null) {
                    fetchAndPopulateFacilities(extractedId)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    /**
     * Function to fetch facilities and populate the spinner
     */
    private fun fetchAndPopulateFacilities(wardId: String) {
        val facilityList = locationViewModel.getHierarchyDetails("Location/$wardId", "")
        val facilitySpinner = binding.rootLayout.findViewWithTag<View>("Name of Receiving Facility") as Spinner
        populateSpinner(facilitySpinner, facilityList)
    }

    private fun populateSpinner(spinner: Spinner, data: List<DbLocationResponse>) {
        val dataList = data.map { it.name }
        //Change the texts to Uppercase
        dataList.map { it.uppercase() }

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