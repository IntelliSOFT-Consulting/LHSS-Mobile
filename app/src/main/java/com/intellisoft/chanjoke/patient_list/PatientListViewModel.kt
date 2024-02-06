/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellisoft.chanjoke.patient_list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.search.Order
import com.google.android.fhir.search.StringFilterModifier
import com.google.android.fhir.search.count
import com.google.android.fhir.search.search
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Location
import org.hl7.fhir.r4.model.Patient

/**
 * The ViewModel helper class for PatientItemRecyclerViewAdapter, that is responsible for preparing
 * data for UI.
 */
class PatientListViewModel(application: Application, private val fhirEngine: FhirEngine) :
    AndroidViewModel(application) {

    val liveSearchedPatients = MutableLiveData<List<PatientItem>>()
    val patientCount = MutableLiveData<Long>()

    init {
        updatePatientListAndPatientCount({ getSearchResults() }, { count() })
    }

    fun searchPatientsByName(nameQuery: String) {
        updatePatientListAndPatientCount({ getSearchResults(nameQuery) }, { count(nameQuery) })
    }

    /**
     * [updatePatientListAndPatientCount] calls the search and count lambda and updates the live data
     * values accordingly. It is initially called when this [ViewModel] is created. Later its called
     * by the client every time search query changes or data-sync is completed.
     */
    private fun updatePatientListAndPatientCount(
        search: suspend () -> List<PatientItem>,
        count: suspend () -> Long,
    ) {
        viewModelScope.launch {
            liveSearchedPatients.value = search()
            patientCount.value = count()
        }
    }

    /**
     * Returns count of all the [Patient] who match the filter criteria unlike [getSearchResults]
     * which only returns a fixed range.
     */
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

    private suspend fun getSearchResults(nameQuery: String = ""): List<PatientItem> {
        var patients: MutableList<PatientItem> = mutableListOf()
        fhirEngine
            .search<Patient> {

                if (nameQuery.isNotEmpty()) {
                    filter(
                        Patient.NAME, {
                            modifier = StringFilterModifier.CONTAINS
                            value = nameQuery
                        },
                    )
                }

                sort(Patient.BIRTHDATE, Order.DESCENDING)
                count = 100
                from = 0
            }
            .mapIndexed { index, fhirPatient -> fhirPatient.toPatientItem(index + 1) }
            .let { patients.addAll(it) }

        /**
         * TODO: Check on the best way to filter by the id or phone number
         */
        if (patients.isEmpty()){
            patients = searchPatientsByPhoneOrIdentification(nameQuery)
        }

        return patients
    }

    private suspend fun searchPatientsByPhoneOrIdentification(searchString: String): MutableList<PatientItem> {
        val matchingPatients = mutableListOf<PatientItem>()
        val patients: MutableList<PatientItem> = mutableListOf()

        fhirEngine
            .search<Patient> {
                sort(Patient.BIRTHDATE, Order.DESCENDING)
                count = 100
                from = 0
            }
            .mapIndexed { index, fhirPatient -> fhirPatient.toPatientItem(index + 1) }
            .let { patients.addAll(it) }


        for (patient in patients) {
            // Check if the phone matches or is close
            if (patient.phone.contains(searchString) || isCloseMatch(patient.phone, searchString)) {
                matchingPatients.add(patient)
            }

            // Check if the identification matches or is close
            if (patient.identification.contains(searchString) || isCloseMatch(patient.identification, searchString)) {
                matchingPatients.add(patient)
            }
        }
        val newList = matchingPatients.distinctBy { it.id }

        return newList.toMutableList()
    }

    fun isCloseMatch(original: String, search: String): Boolean {
        return original.toLowerCase().contains(search.toLowerCase())
    }

    fun retrieveLocations() = runBlocking {
        retrieveAllLocations()
    }

    private suspend fun retrieveAllLocations(): List<LocationItem> {
        val locations: MutableList<LocationItem> = mutableListOf()
        fhirEngine.search<Location> {
            sort(Location.NAME, Order.DESCENDING)
            count = 100
            from = 0
        }
            .mapIndexed { index, fhirPatient -> fhirPatient.toLocationItem(index + 1) }
            .let { locations.addAll(it) }

        return locations
    }


    /** The Patient's details for display purposes. */
    data class PatientItem(
        val id: String,
        val resourceId: String,
        val name: String,
        val gender: String,
        val dob: LocalDate? = null,
        val identification: String,
        val phone: String,
        val city: String,
        val country: String,
        val isActive: Boolean,
        val html: String,
        var risk: String? = "",
    ) {
        override fun toString(): String = name
    }

    data class LocationItem(
        val id: String,
        val resourceId: String,
        val name: String,
    ) {
        override fun toString(): String = name
    }

    /** The Observation's details for display purposes. */


    /** The Observation's details for display purposes. */
    data class ObservationItem(
        val id: String,
        val code: String,
        val effective: String,
        val value: String,
        val dateTime: String? = null,
    ) {
        override fun toString(): String = code
    }

    data class ConditionItem(
        val id: String,
        val code: String,
        val effective: String,
        val value: String,
    ) {
        override fun toString(): String = code
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

internal fun Patient.toPatientItem(position: Int): PatientListViewModel.PatientItem {
    // Show nothing if no values available for gender and date of birth.
    val patientId = if (hasIdElement()) idElement.idPart else ""
    val name = if (hasName()) name[0].nameAsSingleString else ""
    val gender = if (hasGenderElement()) genderElement.valueAsString else ""
    val dob =
        if (hasBirthDateElement()) {
            LocalDate.parse(birthDateElement.valueAsString, DateTimeFormatter.ISO_DATE)
        } else {
            null
        }
    val phone = if (hasTelecom()) telecom[0].value else ""
    val city = if (hasAddress()) address[0].city else ""
    val country = if (hasAddress()) address[0].country else ""
    val isActive = active
    val html: String = if (hasText()) text.div.valueAsString else ""

    val identification: String = if (hasIdentifier()) identifier[0].value else "N/A"

    return PatientListViewModel.PatientItem(
        id = position.toString(),
        resourceId = patientId,
        name = name,
        gender = gender ?: "",
        dob = dob,
        identification = identification,
        phone = phone ?: "",
        city = city ?: "",
        country = country ?: "",
        isActive = isActive,
        html = html,
    )
}

internal fun Location.toLocationItem(position: Int): PatientListViewModel.LocationItem {
    // Show nothing if no values available for gender and date of birth.
    val patientId = if (hasIdElement()) idElement.idPart else ""
    val name = if (hasName()) name.toString() else ""

    return PatientListViewModel.LocationItem(
        id = position.toString(),
        resourceId = patientId,
        name = name,
    )
}
