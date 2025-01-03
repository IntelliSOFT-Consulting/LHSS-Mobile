package com.intellisoft.lhss25.referrals.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.search.Order
import com.google.android.fhir.search.search
import com.intellisoft.lhss25.shared.DbEncounter
import com.intellisoft.lhss25.shared.DbFormData
import com.intellisoft.lhss25.shared.DbNavigationDetails
import com.intellisoft.lhss25.shared.FormData
import com.intellisoft.lhss25.shared.FormatterClass
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Encounter
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.ServiceRequest

class ReferralDetailsViewModel(
    application: Application,
    private val fhirEngine: FhirEngine,
    private val patientId: String,
    private val serviceRequestId: String,
) : AndroidViewModel(application) {

    // LiveData to expose the list of items
    private val _clinicalLiveData = MutableLiveData<List<FormData>>()
    val clinicalLiveData: LiveData<List<FormData>> = _clinicalLiveData

    // LiveData to handle loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val formatterClass = FormatterClass(application.applicationContext)

    fun getEncounterObservationList(title: String):ArrayList<FormData>{

        val formDataList = ArrayList<FormData>()

        val carePlanId = formatterClass.getSharedPref(
            DbNavigationDetails.CARE_PLAN.name,"carePlanId")?: ""

        val encounterList = getEncounterList(carePlanId)
        encounterList.forEach {
            val encounter = it.id
            val title = it.referralReason

            val formData = getEncounterObservationDetails(encounter)

            if (formData != null) {
                formDataList.add(formData)
            }
        }
        val newFormDataList = formDataList.filterTitle(title)

        viewModelScope.launch {
            _clinicalLiveData.value = newFormDataList
        }



        return ArrayList(newFormDataList)
    }

    fun getClinicalList(title: String): ArrayList<FormData> {
        var items = ArrayList<FormData>()

        viewModelScope.launch {
            _isLoading.value = true
            try {
                items = getEncounterObservationList(title)
                _clinicalLiveData.value = items
            } catch (e: Exception) {
                // Handle error (e.g., log it, or show an error message)
            } finally {
                _isLoading.value = false
            }
        }
        return items
    }

    fun getServiceRequest() = runBlocking {
        getServiceRequestBac()
    }


    private suspend fun getServiceRequestBac(): ArrayList<FormData> {

        val formDataList = ArrayList<FormData>()

        val searchResult =
            fhirEngine.search<ServiceRequest> {
                filter(Resource.RES_ID, { value = of(serviceRequestId) })
            }

        if (searchResult.isNotEmpty()) {
            searchResult.first().let {
                val encounterList = it.resource.supportingInfo

                encounterList.forEach { encounterDetails ->

                    val encounter = encounterDetails.reference
                    val dbFormData = getEncounterDetails(encounter)

                    if (dbFormData != null){
                        formDataList.addAll(listOf(dbFormData))
                    }

                }

            }
        }

        return formDataList

    }

    fun getEncounterObservationDetails(encounter: String)= runBlocking {
        getEncounterDetails(encounter)
    }

    private fun createObservationItem(resource: Observation):DbFormData {

        val id = if (resource.hasId()) resource.id else ""

        val tag = if(resource.hasCode() && resource.code.hasCoding()){
            resource.code.codingFirstRep.display
        }else ""

        val text = if (resource.hasValueStringType()){
            resource.valueStringType.valueAsString
        }else ""

        println("tag $tag")
        println("text $text")
        println("id $id")

        return DbFormData(
            tag, text
        )

    }


    private fun List<FormData>.filterTitle(title: String): List<FormData> {
        return this.filter { it.title == title }
    }

    fun getObservationCode(fhirCode: String) = runBlocking {
        getObservationCodeBac(fhirCode)
    }

    private suspend fun getObservationCodeBac(fhirCode: String): DbFormData? {

        // Search for the code in the fhir engine
        val searchResult =
            fhirEngine.search<Observation> {
                filter(Observation.CODE, { value = of(fhirCode) })
                //Sort by date such that the most recent observation is first
                sort(Observation.DATE, Order.DESCENDING)
            }

        if (searchResult.isEmpty()) {
            return null
        }

        val observation = searchResult.first()
        val observationData = createObservationItem(observation.resource)
        return observationData

    }

    private suspend fun getEncounterDetails(encounter:String):FormData?{

        val observationList = ArrayList<DbFormData>()
        var title = ""
        val encounterId = encounter.replace("Encounter/","")

        fhirEngine
            .search<Observation> {
                filter(Observation.SUBJECT, { value = "Patient/$patientId" })
                filter(Observation.ENCOUNTER, { value = encounter })
                sort(Observation.DATE, Order.ASCENDING)
            }
            .map { createObservationItem(it.resource) }
            .let {observationList.addAll(it)}


        val searchResult =
            fhirEngine.search<Encounter> {
                filter(Resource.RES_ID, { value = of(encounterId) })
            }

        if (searchResult.isNotEmpty()) {
            searchResult.first().let {
                title = if (it.resource.hasReasonCode() && it.resource.reasonCodeFirstRep.hasText()){
                    it.resource.reasonCodeFirstRep.text
                }else ""
            }
        }

        if (title != "" && observationList.isNotEmpty()){
            val formData = FormData(
                title,
                observationList
            )
            return formData
        }
        return null
    }

    fun getEncounterList(carePlanId: String) = runBlocking {
        getEncounterListBac(carePlanId)
    }

    private suspend fun getEncounterListBac(carePlanId: String): ArrayList<DbEncounter> {

        val formDataList = ArrayList<DbEncounter>()

        fhirEngine
            .search<Encounter> {
                filter(Encounter.SUBJECT, { value = "Patient/$patientId" })
                sort(Encounter.DATE, Order.ASCENDING)
            }
            .map { createEncounterItem(it.resource) }
            .let {formDataList.addAll(it)}

        val newFormDataList = formDataList.filterBasedOn(carePlanId)

        return ArrayList(newFormDataList)

    }

    private fun List<DbEncounter>.filterBasedOn(basedOn: String): List<DbEncounter> {
        return this.filter { it.basedOn == basedOn }
    }

    private fun createEncounterItem(resource: Encounter): DbEncounter {

        val id = resource.id
        val status = resource.status.toString()

        val dateCreated = if (resource.hasPeriod() && resource.period.hasStart()) {
            resource.period.start.toString()
        }else ""
        val referralReason = if (resource.hasReasonCode()){
            resource.reasonCodeFirstRep.text
        }else ""

        val date = formatterClass.convertDateFormat(dateCreated) ?: ""

        val basedOn = if (resource.hasBasedOn()) {
            resource.basedOnFirstRep.reference.toString().replace("CarePlan/","")
        }else ""

        return DbEncounter(
            id,
            date,
            status,
            "",
            referralReason,
            basedOn)

    }


}
class ReferralDetailsViewModelFactory(
    private val application: Application,
    private val fhirEngine: FhirEngine,
    private val patientId: String,
    private val serviceRequestId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ReferralDetailsViewModel::class.java)) {
            "Unknown ViewModel class"
        }
        return ReferralDetailsViewModel(application, fhirEngine, patientId, serviceRequestId) as T
    }
}