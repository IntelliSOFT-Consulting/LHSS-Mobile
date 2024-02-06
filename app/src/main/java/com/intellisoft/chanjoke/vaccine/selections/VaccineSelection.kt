package com.intellisoft.chanjoke.vaccine.selections

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ExpandableListView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.databinding.ActivityVaccineSelectionBinding
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.vaccine.validations.BasicVaccine
import com.intellisoft.chanjoke.vaccine.validations.ImmunizationHandler
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory

class VaccineSelection : AppCompatActivity() {
    private val formatterClass = FormatterClass()
    private lateinit var binding: ActivityVaccineSelectionBinding
    private var patientId :String = ""

    private var lastExpandedPositionRoutine = -1
    private var lastExpandedPositionNonRoutine = -1
    private var lastExpandedPositionPregnancyVaccine = -1
    private val immunizationHandler = ImmunizationHandler()
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var fhirEngine: FhirEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVaccineSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar =
            findViewById<Toolbar>(R.id.toolbar) // Assuming you have a Toolbar with id 'toolbar' in your layout
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        patientId = formatterClass.getSharedPref("patientId",this).toString()
        fhirEngine = FhirApplication.fhirEngine(this)

        patientDetailsViewModel = ViewModelProvider(
            this,
            PatientDetailsViewModelFactory(
                application,
                fhirEngine,
                patientId
            )
        )[PatientDetailsViewModel::class.java]

        setExpandableProperties()

        setExpandableVisibility(binding.expandableListViewRoutine)

        setOnClick()

        getVaccines()


    }

    private fun getVaccines() {

        val patientDob = formatterClass.getSharedPref("patientDob", this)
        if (patientDob != null){

            val ageInWeeks = formatterClass.calculateWeeksFromDate(patientDob)
            if (ageInWeeks != null){
                val administeredList = ArrayList<BasicVaccine>()
                val vaccineList = patientDetailsViewModel.getVaccineList()
                vaccineList.forEach {
                    val vaccineName = it.vaccineName
                    val basicVaccine = immunizationHandler.getVaccineDetailsByBasicVaccineName(vaccineName)
                    if (basicVaccine != null) {
                        administeredList.add(basicVaccine)
                    }
                }

                val (routineList, nonRoutineVaccineList,  pregnancyVaccineList) =
                    immunizationHandler.getAllVaccineList(administeredList, ageInWeeks, this)

                if(routineList.isEmpty()) binding.linearRoutine.visibility = View.GONE
                if(nonRoutineVaccineList.isEmpty()) binding.linearNonRoutine.visibility = View.GONE
                if(pregnancyVaccineList.isEmpty()) binding.linearPregnancy.visibility = View.GONE

                //Routine Vaccine
                val routineGroupList = mutableListOf<String>()
                val routineChildList = hashMapOf<String, List<String>>()
                routineList.forEach { routineVaccine ->
                    routineGroupList.add(routineVaccine.targetDisease)
                    routineChildList[routineVaccine.targetDisease] = routineVaccine.vaccineList.map { it.vaccineName }
                }
                val routineAdapter = BottomSheetAdapter(routineGroupList, routineChildList, this)
                binding.expandableListViewRoutine.setAdapter(routineAdapter)

                // Add Non-Routine Vaccines to the expandable list
                val nonRoutineGroupList = mutableListOf<String>()
                val nonRoutineChildList = hashMapOf<String, List<String>>()
                nonRoutineVaccineList.forEach { nonRoutineVaccine ->
                    nonRoutineGroupList.add(nonRoutineVaccine.targetDisease)
                    nonRoutineChildList[nonRoutineVaccine.targetDisease] = nonRoutineVaccine.vaccineList
                        .flatMap { it.vaccineList }
                        .map { it.vaccineName }
                }
                val nonRoutineAdapter = BottomSheetAdapter(nonRoutineGroupList, nonRoutineChildList, this)
                binding.expandableListViewNonRoutine.setAdapter(nonRoutineAdapter)


                // Add Pregnancy Vaccines to the expandable list
                val pregnancyGroupList = mutableListOf<String>()
                val pregnancyChildList = hashMapOf<String, List<String>>()
                pregnancyVaccineList.forEach { pregnancyVaccine ->
                    pregnancyGroupList.add(pregnancyVaccine.targetDisease)
                    pregnancyChildList[pregnancyVaccine.targetDisease] = pregnancyVaccine.vaccineList.map { it.vaccineName }
                }
                val pregnancyAdapter = BottomSheetAdapter(pregnancyGroupList, pregnancyChildList, this)
                binding.expandableListViewPregnancy.setAdapter(pregnancyAdapter)

            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }


    private fun setOnClick() {
        binding.tvRoutineVaccine.setOnClickListener { setExpandableVisibility(binding.expandableListViewRoutine) }
        binding.tvNonRoutineVaccine.setOnClickListener { setExpandableVisibility(binding.expandableListViewNonRoutine) }
        binding.tvPregnancyVaccine.setOnClickListener { setExpandableVisibility(binding.expandableListViewPregnancy) }

    }

    private fun setExpandableVisibility(expandableListView: ExpandableListView){
        val listExpandableListView = mutableListOf(
            binding.expandableListViewRoutine,
            binding.expandableListViewNonRoutine,
            binding.expandableListViewPregnancy )
        listExpandableListView.remove(expandableListView)
        listExpandableListView.forEach {
            it.visibility = View.GONE
        }

        expandableListView.visibility = View.VISIBLE

    }

    private fun setExpandableProperties() {
        //Routine Vaccines
        binding.expandableListViewRoutine.setOnGroupClickListener { parent, view, groupPosition, id ->
            // Handle group click here
            if (lastExpandedPositionRoutine != -1 && lastExpandedPositionRoutine != groupPosition) {
                binding.expandableListViewRoutine.collapseGroup(lastExpandedPositionRoutine)
            }

            if (binding.expandableListViewRoutine.isGroupExpanded(groupPosition)) {
                binding.expandableListViewRoutine.collapseGroup(groupPosition)
                lastExpandedPositionRoutine = -1
            } else {
                binding.expandableListViewRoutine.expandGroup(groupPosition)
                lastExpandedPositionRoutine = groupPosition
            }

            true // Return true to consume the click event
        }

        //Non routine Vaccines
        binding.expandableListViewNonRoutine.setOnGroupClickListener { parent, view, groupPosition, id ->
            // Handle group click here
            if (lastExpandedPositionNonRoutine != -1 && lastExpandedPositionNonRoutine != groupPosition) {
                binding.expandableListViewNonRoutine.collapseGroup(lastExpandedPositionNonRoutine)
            }

            if (binding.expandableListViewNonRoutine.isGroupExpanded(groupPosition)) {
                binding.expandableListViewNonRoutine.collapseGroup(groupPosition)
                lastExpandedPositionNonRoutine = -1
            } else {
                binding.expandableListViewNonRoutine.expandGroup(groupPosition)
                lastExpandedPositionNonRoutine = groupPosition
            }
            true // Return true to consume the click event
        }

        //Pregnancy Vaccines
        binding.expandableListViewPregnancy.setOnGroupClickListener { parent, view, groupPosition, id ->
            // Handle group click here
            if (lastExpandedPositionPregnancyVaccine != -1 && lastExpandedPositionPregnancyVaccine != groupPosition) {
                binding.expandableListViewPregnancy.collapseGroup(lastExpandedPositionPregnancyVaccine)
            }

            if (binding.expandableListViewPregnancy.isGroupExpanded(groupPosition)) {
                binding.expandableListViewPregnancy.collapseGroup(groupPosition)
                lastExpandedPositionPregnancyVaccine = -1
            } else {
                binding.expandableListViewPregnancy.expandGroup(groupPosition)
                lastExpandedPositionPregnancyVaccine = groupPosition
            }
            true // Return true to consume the click event
        }

    }
}