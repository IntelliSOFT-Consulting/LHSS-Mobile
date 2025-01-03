package com.intellisoft.lhss25.clinical_info.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.intellisoft.lhss25.R
import com.intellisoft.lhss25.clinical_info.viewmodel.ClinicalInfoDetailsViewModel
import com.intellisoft.lhss25.clinical_info.viewmodel.ClinicalInfoViewViewModel
import com.intellisoft.lhss25.dynamic_components.DefaultLabelCustomizer
import com.intellisoft.lhss25.dynamic_components.DefaultSpinnerSelectionHandler
import com.intellisoft.lhss25.dynamic_components.FieldManager
import com.intellisoft.lhss25.dynamic_components.FormUtils
import com.intellisoft.lhss25.dynamic_components.SpinnerSelectionHandler
import com.intellisoft.lhss25.refer_patient.viewmodel.ReviewReferViewModel
import com.intellisoft.lhss25.shared.DbField
import com.intellisoft.lhss25.shared.FormData
import com.intellisoft.lhss25.shared.FormatterClass

import org.hl7.fhir.r4.model.CarePlan

class BottomNavigationDrawerFragmentWithWidgets(
    dbFieldList: List<DbField>, workflowTitles: String)
    : BottomSheetDialogFragment() {

    private lateinit var fieldManager: FieldManager
    private lateinit var formatterClass: FormatterClass
    private val dbFieldList: List<DbField> by lazy { dbFieldList }
    private val workflowTitles: String by lazy { workflowTitles }
    private val viewModel: ReviewReferViewModel by viewModels()
    private lateinit var clinicalViewModel: ClinicalInfoDetailsViewModel
    private var patientId:String = ""
    private val clinicalInfoViewViewModel: ClinicalInfoViewViewModel by viewModels()
    private val spinnerSelectionHandler: SpinnerSelectionHandler = DefaultSpinnerSelectionHandler()
    private lateinit var rootLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the custom layout (without BottomSheetDialog in XML)
        return inflater.inflate(R.layout.bottom_navigation_drawer_with_widgets, container, false)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fieldManager = FieldManager(DefaultLabelCustomizer(), requireContext())
        formatterClass = FormatterClass(requireContext())
        patientId = formatterClass.getSharedPref("", "patientId") ?: ""

        clinicalViewModel =
            ViewModelProvider(
                this,
                ClinicalInfoDetailsViewModel.ClinicalInfoDetailsViewModelFactory(
                    requireActivity().application,
                    patientId
                )
            )[ClinicalInfoDetailsViewModel::class.java]

        // Get references to widgets and handle interactions
        val btnAdd: Button = view.findViewById(R.id.btnAdd)
        rootLayout = view.findViewById(R.id.rootLayout)

        FormUtils.populateView(ArrayList(dbFieldList),
            rootLayout, fieldManager, requireContext())

        btnAdd.setOnClickListener {

            val (addedFields, missingFields) = FormUtils.extractAllFormData(rootLayout)
            if (missingFields.isNotEmpty()){
                var missingText = ""
                missingFields.forEach { missingText += "\n ${it.tag}, " }

                val mandatoryText = "The following are mandatory fields and " +
                        "need to be filled before proceeding: \n" +
                        missingText

                formatterClass.showDialog("Missing Content", mandatoryText)
            }else{

                val formDataList = ArrayList<FormData>()
                val formData = FormData(
                    workflowTitles.toString(),
                    addedFields)
                formDataList.add(formData)

                savedData(formDataList)



            }

            dismiss() // Close drawer if necessary
        }

        setSpinnerListener(
            listOf("Drug")
        )

        clinicalInfoViewViewModel.selectedItem.observe(viewLifecycleOwner) { selectedItem ->

            val drugOthersText = formatterClass.findTextViewByText(rootLayout, "Specify Others")
            val drugOthers = rootLayout.findViewWithTag<View>("Specify Others")


            when (selectedItem) {
                "Others, Specify" -> {
                    drugOthersText?.visibility = View.VISIBLE
                    drugOthers?.visibility = View.VISIBLE
                }
                "R", "BDQ","H","LZD", "LFX", "MFX", "CFZ","Cs","DLM","ETO","SLIDs" -> {
                    drugOthersText?.visibility = View.GONE
                    drugOthers?.visibility = View.GONE
                }
            }

        }

    }

    private fun setSpinnerListener(tagList: List<String>) {
        tagList.forEach { tag ->
            val rootViewParent = rootLayout.findViewWithTag<View>(tag)
            if (rootViewParent is Spinner) {
                spinnerSelectionHandler.handleSelection(rootViewParent) { selectedItem ->
                    clinicalInfoViewViewModel.updateSelectedItem(selectedItem, rootViewParent)
                }
            }
        }
    }


    private fun savedData(formDataList: ArrayList<FormData>) {

        viewModel.createClinicalInfo(formDataList,
            workflowTitles,
            clinicalViewModel,
            CarePlan.CarePlanStatus.ACTIVE)

        val successMessage = formatterClass.toSentenceCase(workflowTitles)
        Toast.makeText(this.context,
            "Data saved successfully for $successMessage",
            Toast.LENGTH_SHORT).show()

    }
}