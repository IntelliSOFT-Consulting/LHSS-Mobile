package com.intellisoft.lhss25.clinical_info.fragment

import android.app.Application
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.fhir.FhirEngine
import com.intellisoft.lhss25.clinical_info.shared.ClinicalParentAdapter
import com.intellisoft.lhss25.clinical_info.viewmodel.ClinicalInfoViewViewModel
import com.intellisoft.lhss25.databinding.FragmentClinicalInfoFormIIIIVBinding
import com.intellisoft.lhss25.dynamic_components.DefaultSpinnerSelectionHandler
import com.intellisoft.lhss25.dynamic_components.FieldManager
import com.intellisoft.lhss25.dynamic_components.SpinnerSelectionHandler
import com.intellisoft.lhss25.fhir.FhirApplication
import com.intellisoft.lhss25.referrals.viewmodels.ReferralDetailsViewModel
import com.intellisoft.lhss25.referrals.viewmodels.ReferralDetailsViewModelFactory
import com.intellisoft.lhss25.shared.DbClasses
import com.intellisoft.lhss25.shared.DbField
import com.intellisoft.lhss25.shared.DbNavigationDetails
import com.intellisoft.lhss25.shared.DbWidgets
import com.intellisoft.lhss25.shared.FormatterClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ClinicalInfoFormIII_IVFragment : Fragment() {

    private var _binding: FragmentClinicalInfoFormIIIIVBinding? = null
    private lateinit var viewModel: ReferralDetailsViewModel

    private val binding get() = _binding!!
    private lateinit var fieldManager: FieldManager
    private lateinit var formatterClass: FormatterClass
    private var patientId:String = ""
    private var serviceRequestId:String = ""
    private var workflowTitles:String = ""
    private val monthList = listOf("1 month", "2 months","3 months", "4 months",
        "5 months", "6 months", "7 months", "8 months", "9 months", "10 months",
        "11 months", "12 months")
    private val monthList1 = listOf("1 month", "2 months","3 months", "4 months",
        "5 months", "15 months", "18 months", "21 months", "24 months", "18 months",
        "21 months", "24 months")
    private lateinit var fhirEngine: FhirEngine
    private var carePlanId:String = ""
    private var carePlanStatus:String = ""
    private var encounterId:String = ""
    private val clinicalInfoViewViewModel: ClinicalInfoViewViewModel by viewModels()
    private val spinnerSelectionHandler: SpinnerSelectionHandler = DefaultSpinnerSelectionHandler()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = FragmentClinicalInfoFormIIIIVBinding.inflate(inflater, container, false)
        formatterClass = FormatterClass(requireContext())
        navigationActions()
        patientId = formatterClass.getSharedPref("", "patientId")?: ""
        serviceRequestId = formatterClass.getSharedPref("", "serviceRequestId")?: ""
        workflowTitles = formatterClass.getSharedPref("", "CLINICAL_REFERRAL")?: ""

        carePlanId = formatterClass.getSharedPref(DbNavigationDetails.CARE_PLAN.name,"carePlanId")?: ""
        carePlanStatus = formatterClass.getSharedPref(DbNavigationDetails.CARE_PLAN.name,"carePlanStatus")?: ""
        encounterId = formatterClass.getSharedPref("","encounterId")?: ""

        if (workflowTitles != ""){
            val sectionTitle = if (workflowTitles == DbClasses.DST.name){
                "DST/Cultures/LPA"
            }else {
                formatterClass.toSentenceCase(workflowTitles)
            }
            binding.tvTitle.text = sectionTitle
//            binding.imgBtn.setImageResource(workflowTitles)
        }

        val dbFieldList = loadFormData()

        if (carePlanStatus == "COMPLETED"){
            binding.btnAdd.visibility = View.GONE
        }

        binding.btnAdd.setOnClickListener {
            val bottomNavigationDrawerFragment =
                BottomNavigationDrawerFragmentWithWidgets(dbFieldList,workflowTitles)
            bottomNavigationDrawerFragment.show(parentFragmentManager,
                bottomNavigationDrawerFragment.tag)
        }

        // Create sample data


        fhirEngine = FhirApplication.fhirEngine(requireContext())

        viewModel =
            ViewModelProvider(
                this,
                ReferralDetailsViewModelFactory(
                    requireContext().applicationContext as Application,
                    fhirEngine,
                    patientId,
                    serviceRequestId
                ),
            )[ReferralDetailsViewModel::class.java]


        CoroutineScope(Dispatchers.IO).launch {

            val formData = viewModel.getEncounterObservationList(workflowTitles)

            CoroutineScope(Dispatchers.Main).launch {

                binding.parentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                binding.parentRecyclerView.adapter = ClinicalParentAdapter(formData)
            }
        }

        startRepeatingTask()

        return binding.root

    }


    fun startRepeatingTask() {
        // Use lifecycleScope to ensure coroutines respect the Activity/Fragment lifecycle
        lifecycleScope.launch {
            while (true) {
                // Invoke the function and get the result
                val formData = viewModel.getEncounterObservationList(workflowTitles)
                binding.parentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                binding.parentRecyclerView.adapter = ClinicalParentAdapter(formData)

                // Suspend for 5 seconds before the next iteration
                delay(5000)
            }
        }
    }

    private fun loadFormData(): List<DbField> {
        if (workflowTitles == DbClasses.LABORATORY_RESULTS.name) {
            val dbFieldList = listOf(
                DbField(
                    DbWidgets.SPINNER.name,
                    "Month", true, null,
                    monthList),
                DbField(
                    DbWidgets.EDIT_TEXT.name,
                    "GXP Result", false,
                    InputType.TYPE_TEXT_VARIATION_PERSON_NAME
                ),
                DbField(
                    DbWidgets.EDIT_TEXT.name,
                    "Smear Result", false,
                    InputType.TYPE_TEXT_VARIATION_PERSON_NAME
                ),
                DbField(
                    DbWidgets.DATE_PICKER.name,
                    "Date",
                    true
                ),
            )
            return dbFieldList
        }
        if (workflowTitles == DbClasses.DST.name) {
            val dbFieldList = listOf(
                DbField(
                    DbWidgets.SPINNER.name,
                    "Drug", true, null,
                    listOf("R", "BDQ","H","LZD", "LFX", "MFX", "CFZ","Cs","DLM","ETO","SLIDs","Others, Specify")
                ),
                DbField(
                    DbWidgets.EDIT_TEXT.name,
                    "Specify Others", false,
                    InputType.TYPE_CLASS_TEXT
                ),
                DbField(
                    DbWidgets.SPINNER.name,
                    "Result", true, null,
                    listOf("Resistance", "Susceptible","Not Done","Done Awaiting Results")
                ),

            )
            return dbFieldList
        }
        if (workflowTitles == DbClasses.DR_TB_FOLLOW_UP_TEST.name) {
            val dbFieldList = listOf(
                DbField(
                    DbWidgets.SPINNER.name,
                    "Month", false, null,
                    monthList1),
                DbField(
                    DbWidgets.EDIT_TEXT.name,
                    "Culture Result", false,
                    InputType.TYPE_TEXT_VARIATION_PERSON_NAME
                ),
                DbField(
                    DbWidgets.EDIT_TEXT.name,
                    "Smear Result", false,
                    InputType.TYPE_TEXT_VARIATION_PERSON_NAME
                ),
            )
            return dbFieldList
        }

        return emptyList()
    }


    private fun navigationActions() {
        // Set the next button text to "Continue" and add click listeners
        val navigationButtons = binding.navigationButtons
        navigationButtons.setNextButtonText("Close")

        navigationButtons.setBackButtonClickListener {
            // Handle back button click
            findNavController().navigateUp()
        }

        navigationButtons.setNextButtonClickListener {
            // Handle next button click
            // Navigate to the next fragment or perform any action

            findNavController().navigateUp()
        }
    }


}