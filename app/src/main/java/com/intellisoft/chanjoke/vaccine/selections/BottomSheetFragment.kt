package com.intellisoft.chanjoke.vaccine.selections

import android.app.Application
import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.vaccine.validations.ImmunizationHandler
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory


class BottomSheetFragment : BottomSheetDialogFragment() {

    private var lastExpandedPosition = -1
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientId: String
    private val formatterClass = FormatterClass()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_bottom_sheet, container, false)

        fhirEngine = FhirApplication.fhirEngine(requireContext())
        patientId = formatterClass.getSharedPref("patientId", requireContext()).toString()

        patientDetailsViewModel = ViewModelProvider(
            this,
            PatientDetailsViewModelFactory(
                requireContext().applicationContext as Application,
                fhirEngine,
                patientId
            )
        )[PatientDetailsViewModel::class.java]

        val expandableListView: ExpandableListView = view.findViewById(R.id.expandableListView)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)

        expandableListView.setOnGroupClickListener { parent, view, groupPosition, id ->
            // Handle group click here
            if (lastExpandedPosition != -1 && lastExpandedPosition != groupPosition) {
                expandableListView.collapseGroup(lastExpandedPosition)
            }

            if (expandableListView.isGroupExpanded(groupPosition)) {
                expandableListView.collapseGroup(groupPosition)
                lastExpandedPosition = -1
            } else {
                expandableListView.expandGroup(groupPosition)
                lastExpandedPosition = groupPosition
            }

            true // Return true to consume the click event
        }

        val immunizationHandler = ImmunizationHandler()
//        val (groupList, childList) = immunizationHandler.eligibleVaccineList(requireContext(), patientDetailsViewModel)
//
//        if (groupList.isEmpty()){
//            val text = "Client Not eligible for any vaccines"
//            tvTitle.text = text
//        }

//        val adapter = BottomSheetAdapter(groupList, childList, requireContext())
//        expandableListView.setAdapter(adapter)

        return view
    }

}