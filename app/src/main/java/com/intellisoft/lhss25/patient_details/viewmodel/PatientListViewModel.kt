package com.intellisoft.lhss25.patient_details.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.search.StringFilterModifier
import com.google.android.fhir.search.count
import com.google.android.fhir.search.search
import com.intellisoft.lhss25.shared.DbPatientItem
import com.intellisoft.lhss25.shared.FormatterClass
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Patient
import java.text.SimpleDateFormat
import java.util.Locale

class PatientListViewModel(
    application: Application, 
    private val fhirEngine: FhirEngine) :
    AndroidViewModel(application) {

    val liveSearchedPatients = MutableLiveData<List<DbPatientItem>>()
    val patientCount = MutableLiveData<Long>()
    private var formatterClass = FormatterClass(application.applicationContext)

    init {
        updatePatientListAndPatientCount({ getSearchResults() }, { count() })
    }

    private fun updatePatientListAndPatientCount(
        search: suspend () -> List<DbPatientItem>,
        count: suspend () -> Long,
    ) {
        viewModelScope.launch {
            liveSearchedPatients.value = search()
            patientCount.value = count()
        }
    }

    private suspend fun count(nameQuery: String = ""): Long {
        return fhirEngine.count<Patient> {
            if (nameQuery.isNotEmpty()) {
                filter(
                    Patient.NAME,
                    {
                        modifier = StringFilterModifier.CONTAINS
                        value = nameQuery
                    },
                )
            }
        }
    }

    fun searchPatientsByName(nameQuery: String) {
        updatePatientListAndPatientCount({ getSearchResults(nameQuery) }, { count(nameQuery) })
    }

    private suspend fun getSearchResults(nameQuery: String = ""): ArrayList<DbPatientItem> {

        var patients: MutableList<DbPatientItem> = mutableListOf()

        fhirEngine
            .search<Patient> {
                if (nameQuery.isNotEmpty()) {
                    filter(
                        Patient.NAME,
                        {
                            modifier = StringFilterModifier.CONTAINS
                            value = nameQuery
                        },
                    )
                }
                count = 100
                from = 0
            }
            .mapIndexed { index, fhirPatient -> createPatient(fhirPatient.resource) }
            .let { patients.addAll(it) }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)

        //Sort the patients by dateCreated such that the patient with the most recent date is the first
        patients.sortWith(compareByDescending {
            try {
                it.dateCreated?.let { it1 -> formatterClass.parseDateSafely(it1, dateFormat) }
            } catch (e: Exception) {
                Log.e("PatientListViewModel", "Error parsing date: ${it.dateCreated}")
                null
            }
        })

        return ArrayList(patients)
    }

    private fun createPatient(fhirPatient: Patient):DbPatientItem {

        val id = fhirPatient.id
        var name = ""
        var dob = ""
        var dateCreated = ""

        if (fhirPatient.hasIdentifier()){
            val identifierList = fhirPatient.identifier
            identifierList.forEach {
                if (it.hasSystem() && it.hasValue() && it.system == "system-creation"){
//                    val valueStr = formatterClass.convertDateFormat(it.value)

                    if (it.value != null){
                        dateCreated = it.value.toString()
                    }
                }
            }
        }

        if (fhirPatient.hasName()){
            fhirPatient.name.forEach {humanName->
                val text = if (humanName.hasGiven()){
                    humanName.givenAsSingleString
                } else if (humanName.hasFamily()){
                    humanName.family
                }else{
                    ""
                }
                name = "$name $text"
            }
        }

        //DOB
        if (fhirPatient.hasBirthDate()) {
            val birthDateElement = fhirPatient.birthDate
            val valueStr = formatterClass.convertDateFormat(birthDateElement.toString())
            if (valueStr != null) {
                dob = valueStr
            }
        }

        val logicalId = cleanUpString(id)

        return DbPatientItem(
            logicalId,
            name,
            logicalId.substring(0..6),
            dob,
            dateCreated
        )

    }

    fun cleanUpString(input: String): String {
        // Use a regex to replace "Patient/" and "/_history/any number"
        return input.replace("Patient/", "")
            .replace(Regex("/_history/\\d+"), "")
    }

    private fun isCloseMatch(original: String, search: String): Boolean {
        return original.toLowerCase().contains(search.toLowerCase())
    }

    class PatientListViewModelFactory(
        private val application: Application,
        private val fhirEngine: FhirEngine,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PatientListViewModel::class.java)) {
                return PatientListViewModel(application, fhirEngine) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}


