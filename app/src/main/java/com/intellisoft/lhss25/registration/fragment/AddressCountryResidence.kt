package com.intellisoft.lhss25.registration.fragment

import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.fhir.FhirEngine
import com.google.gson.Gson
import com.intellisoft.lhss25.LocationViewModel
import com.intellisoft.lhss25.R
import com.intellisoft.lhss25.clinical_info.viewmodel.ClinicalInfoViewViewModel
import com.intellisoft.lhss25.databinding.FragmentAddressCountryResidenceBinding
import com.intellisoft.lhss25.dynamic_components.DefaultLabelCustomizer
import com.intellisoft.lhss25.dynamic_components.DefaultSpinnerSelectionHandler
import com.intellisoft.lhss25.dynamic_components.FieldManager
import com.intellisoft.lhss25.dynamic_components.FormUtils
import com.intellisoft.lhss25.dynamic_components.SpinnerSelectionHandler
import com.intellisoft.lhss25.fhir.Constants
import com.intellisoft.lhss25.fhir.FhirApplication
import com.intellisoft.lhss25.registration.viewmodel.AddressViewModel
import com.intellisoft.lhss25.shared.DbClasses
import com.intellisoft.lhss25.shared.DbField
import com.intellisoft.lhss25.shared.DbLocationResponse
import com.intellisoft.lhss25.shared.DbNavigationDetails
import com.intellisoft.lhss25.shared.DbWidgets
import com.intellisoft.lhss25.shared.FormData
import com.intellisoft.lhss25.shared.FormatterClass
import com.intellisoft.lhss25.shared.LocationDetails


class AddressCountryResidence : Fragment() {

    private var _binding: FragmentAddressCountryResidenceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddressViewModel by viewModels()

    private lateinit var fieldManager: FieldManager
//    private var countryOriginList = listOf(
//        "Djibouti", "Eritrea", "Kenya", "Ethiopia", "Somalia", "South Sudan", "Sudan", "Uganda")

    private lateinit var formatterClass: FormatterClass

    private val countryList = listOf(
        DbLocationResponse("Kenya","","", "0"),
        DbLocationResponse("Uganda","","", "Uganda"),
    )
    private val clinicalInfoViewViewModel: ClinicalInfoViewViewModel by viewModels()
    private val spinnerSelectionHandler: SpinnerSelectionHandler = DefaultSpinnerSelectionHandler()
    private lateinit var locationViewModel: LocationViewModel
    private lateinit var fhirEngine: FhirEngine


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAddressCountryResidenceBinding.inflate(inflater, container, false)

        navigationActions()
        formatterClass = FormatterClass(requireContext())

        val workflowTitles = formatterClass.getWorkflowTitles(DbClasses.ADDRESS_RESIDENCE.name)
        if (workflowTitles != null){
            binding.tvTitle.text = formatterClass.toSentenceCase(workflowTitles.text)
            binding.imgBtn.setImageResource(workflowTitles.iconId)
        }

        fhirEngine = FhirApplication.fhirEngine(requireContext())

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
            // Call the function to extract form data
            val (addedFields, missingFields) = FormUtils.extractAllFormData(binding.rootLayout)

            if (missingFields.isNotEmpty()){
                var missingText = ""
                missingFields.forEach { missingText += "\n ${it.tag}, " }

                val mandatoryText = "The following are mandatory fields and " +
                        "need to be filled before proceeding: \n" +
                        missingText

                formatterClass.showDialog("Missing Content", mandatoryText)
            }else{
                findNavController().navigate(R.id.action_addressFragment_to_nextOfKinFragment)
                val formData = FormData(
                    DbClasses.ADDRESS_RESIDENCE.name,
                    addedFields)

                val gson = Gson()
                val json = gson.toJson(formData)

                formatterClass.saveSharedPref(
                    sharedPrefName = DbNavigationDetails.PATIENT_REGISTRATION.name,
                    DbClasses.ADDRESS_RESIDENCE.name,
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
//            DbField(
//                DbWidgets.SPINNER.name,
//                "Country of Origin",
//                true,
//                null,
//                countryList.map { it.name },
//                true,
//                Constants.COUNTRY_RECEIVING
//            ),
//            DbField(
//                DbWidgets.SPINNER.name,
//                "Region/Province/County of Origin",
//                true,
//                null,
//                emptyList(),
//                true,
//                Constants.REGION_COUNTY_RECEIVING
//            ),
//            DbField(
//                DbWidgets.SPINNER.name,
//                "District/Sub County of Origin",
//                false,
//                null,
//                emptyList(),
//                true,
//                Constants.DISTRICT_SUB_COUNTY_RECEIVING
//            ),
//            DbField(
//                DbWidgets.SPINNER.name,
//                "Ward of Origin",
//                false,
//                null,
//                emptyList(),
//                true,
//                Constants.WARD_RECEIVING
//            ),


            DbField(
                DbWidgets.SPINNER.name,
                "Country of Residence",
                true,
                null,
                countryList.map { it.name },
                true,
                Constants.COUNTRY_RECEIVING
            ),
            DbField(
                DbWidgets.SPINNER.name,
                "Region/Province/County of Residence",
                true,
                null,
                emptyList(),
                true,
                Constants.REGION_COUNTY_RECEIVING
            ),
            DbField(
                DbWidgets.SPINNER.name,
                "District/Sub County of Residence",
                false,
                null,
                emptyList(),
                true,
                Constants.DISTRICT_SUB_COUNTY_RECEIVING
            ),
            DbField(
                DbWidgets.SPINNER.name,
                "Ward of Residence",
                false,
                null,
                emptyList(),
                true,
                Constants.WARD_RECEIVING
            ),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Nearest Landmark in Country of Residence", false,
                InputType.TYPE_CLASS_TEXT),
//            DbField(
//                DbWidgets.SPINNER.name,
//                "Country of Origin", true, null,
//                countryOriginList),
//            DbField(
//                DbWidgets.SPINNER.name,
//                "Country of Residence", true, null,
//                countryOriginList),
//            DbField(
//                DbWidgets.EDIT_TEXT.name,
//                "Residential Address in Referring Country", true,
//                InputType.TYPE_CLASS_TEXT),
//
//            DbField(
//                DbWidgets.EDIT_TEXT.name,
//                "Residential Address in Receiving Country", true,
//                InputType.TYPE_CLASS_TEXT),

        )

        FormUtils.populateView(ArrayList(dbFieldList), binding.rootLayout, fieldManager, requireContext())

//        viewModel.extractFormData(binding.rootLayout)

        setSpinnerListener(
            listOf(
//                "Country of Origin",
//                "Region/Province/County of Origin",
//                "District/Sub County of Origin",
//                "Ward of Origin",

                "Country of Residence",
                "Region/Province/County of Residence",
                "District/Sub County of Residence",
                "Ward of Residence",
            )
        )

        FormUtils.loadFormData(
            requireContext(),
            binding.rootLayout,
            DbNavigationDetails.PATIENT_REGISTRATION.name,
            DbClasses.ADDRESS_RESIDENCE.name
        )

        // Use the extension
        val combinedLiveData = clinicalInfoViewViewModel.rootViewSpinner.combineWith(
            clinicalInfoViewViewModel.selectedItem
        )

        combinedLiveData.observe(viewLifecycleOwner) { (rootViewItem, selectedItem) ->
            // Use rootViewItem and selectedItem together
//            val countryOrigin = binding.rootLayout.findViewWithTag<View>("Country of Origin") as Spinner
            val countryResidence = binding.rootLayout.findViewWithTag<View>("Country of Residence") as Spinner

//            val regionOrigin = binding.rootLayout.findViewWithTag<View>("Region/Province/County of Origin") as Spinner
//            val districtOrigin = binding.rootLayout.findViewWithTag<View>("District/Sub County of Origin") as Spinner
//            val wardOrigin = binding.rootLayout.findViewWithTag<View>("Ward of Origin") as Spinner

            val regionResidence = binding.rootLayout.findViewWithTag<View>("Region/Province/County of Residence") as Spinner
            val districtResidence = binding.rootLayout.findViewWithTag<View>("District/Sub County of Residence") as Spinner
            val wardResidence = binding.rootLayout.findViewWithTag<View>("Ward of Residence") as Spinner

            //Handle the origin and residence
            var countryValue: Spinner? = null
            var regionValue: Spinner? = null
            var districtValue: Spinner? = null
            var wardValue: Spinner? = null

            //Check the rootViewItem and work with the origin or residence
            when (rootViewItem) {
//                countryOrigin -> {
//                    countryValue = countryOrigin
//                    regionValue = regionOrigin
//                    districtValue = districtOrigin
//                    wardValue = wardOrigin
//                }
                countryResidence -> {
                    countryValue = countryResidence
                    regionValue = regionResidence
                    districtValue = districtResidence
                    wardValue = wardResidence
                }
                else -> {
                    countryValue = null
                    regionValue = null
                    districtValue = null
                    wardValue = null
                }
            }

            workWithData(regionValue, districtValue, wardValue, selectedItem)
        }

    }

    private fun workWithData(
        regionValue: Spinner?,
        districtValue: Spinner?,
        wardValue: Spinner?,
        selectedItem: String?
    ) {

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
                        if (regionValue != null) {
                            populateSpinner(regionValue, locationList)

                            // Handle county/region selection dynamically
                            regionValue.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                    val selectedRegion = locationList[position].id
                                    //Use this to get the Districts
                                    val extractedId = selectedRegion?.split("/")?.get(1)

                                    if (extractedId != null) {
                                        if (wardValue != null && districtValue != null) {
                                            fetchAndPopulateDistricts(extractedId, districtValue, wardValue)
                                        }
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

    fun <A, B> LiveData<A>.combineWith(other: LiveData<B>): LiveData<Pair<A?, B?>> {
        val result = MediatorLiveData<Pair<A?, B?>>()
        result.addSource(this) { a -> result.value = Pair(a, other.value) }
        result.addSource(other) { b -> result.value = Pair(this.value, b) }
        return result
    }

    private fun fetchAndPopulateDistricts(regionProvinceId: String, districtSpinner: Spinner, wardSpinner: Spinner) {
        val districtList = locationViewModel
            .getHierarchyDetails("Location/$regionProvinceId", "")
        populateSpinner(districtSpinner, districtList)

        // Handle district/sub-county selection dynamically
        districtSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedDistrict = districtList[position].id
                val extractedId = selectedDistrict?.split("/")?.get(1)
                if (extractedId != null) {
                    fetchAndPopulateWards(extractedId, wardSpinner)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun fetchAndPopulateWards(districtId: String, wardSpinner: Spinner) {
        val wardList = locationViewModel.getHierarchyDetails("Location/$districtId", "")

        populateSpinner(wardSpinner, wardList)

        // Handle ward selection dynamically
        wardSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedWard = wardList[position].id
                val extractedId = selectedWard?.split("/")?.get(1)


            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
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