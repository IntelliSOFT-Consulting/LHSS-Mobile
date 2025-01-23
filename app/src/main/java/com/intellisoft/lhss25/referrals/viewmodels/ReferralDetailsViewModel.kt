package com.intellisoft.lhss25.referrals.viewmodels

import android.app.Application
import android.util.Log
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.search.Order
import com.google.android.fhir.search.search
import com.intellisoft.lhss25.LocationViewModel
import com.intellisoft.lhss25.fhir.Constants
import com.intellisoft.lhss25.shared.DbEncounter
import com.intellisoft.lhss25.shared.DbFormData
import com.intellisoft.lhss25.shared.DbFormsData
import com.intellisoft.lhss25.shared.DbNavigationDetails
import com.intellisoft.lhss25.shared.FormData
import com.intellisoft.lhss25.shared.FormatterClass
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.DocumentReference
import org.hl7.fhir.r4.model.Encounter
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.ServiceRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReferralDetailsViewModel(
    application: Application,
    private val fhirEngine: FhirEngine,
    private val patientId: String,
    private val serviceRequestId: String,
    private val encounterId: String? = null,
    private val locationViewModel: LocationViewModel? = null
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

        val formName = formatterClass.getSharedPref("", "FORM_NAME").orEmpty()

        when (formName) {
            "END_TREATMENT_FORM" -> getEncounterRequestBac()
            "" -> getServiceRequestBac()
            else -> getServiceRequestBac()
        }

    }

    private suspend fun getEncounterRequestBac(): ArrayList<FormData>{

        val formDataList = ArrayList<FormData>()
        val dbFormData = encounterId?.let { getEncounterDetails(it) }
        if (dbFormData != null) {
            formDataList.add(dbFormData)
        }
        return formDataList
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

        var textValue = text ?: ""
        if (tag == "Name of Receiving Facility" && text.isDigitsOnly()) {
            textValue = locationViewModel?.getLocationById(text)?.name ?: text
        }


        return DbFormData(
            tag, textValue
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

    fun getFilledFormList(type:String) = runBlocking {

        when (type) {
            "END_TREATMENT_FORM" -> {
                getBacFilledFormList(type)
            }
            "ACKNOWLEDGEMENT_FORM" -> {
                getBacAcknowledgementFilledFormList(type)
            }
            else -> {
                emptyList()
            }
        }

    }

    private suspend fun getBacAcknowledgementFilledFormList(type:String): ArrayList<DbFormsData>{

        val formDataList = ArrayList<DbFormsData?>()

        fhirEngine
            .search<DocumentReference> {
                filter(Encounter.SUBJECT, { value = "Patient/$patientId" })
                sort(Encounter.DATE, Order.ASCENDING)
            }
            .map { createDocumentReferenceItem(it.resource, type) }
            .let {formDataList.addAll(it)}

        return ArrayList(formDataList.filterNotNull())

    }

    private suspend fun createDocumentReferenceItem(resource: DocumentReference, type: String):DbFormsData? {

        val id = if (resource.hasId()) resource.id else ""
        val status = if (resource.hasStatus()) resource.statusElement else ""
        val filledOn = if (resource.hasDate()) resource.date else null
        val filledConverted = if (filledOn != null) formatterClass.convertDateFormat(filledOn.toString()) else ""
        val serviceRequestReference = if (resource.hasContext() && resource.context.hasRelated()) resource.context.relatedFirstRep.reference else ""

        val serviceRequestId = serviceRequestReference.replace("ServiceRequest/","")

        val searchResult =
            fhirEngine.search<ServiceRequest> {
                filter(Resource.RES_ID, { value = of(serviceRequestId) })
            }

        if (searchResult.isEmpty()) {
            return null
        }

        var codingDisplayValue = ""
        var authoredOnValue = ""

        searchResult.firstOrNull()?.let { serviceRequestValue ->
            val serviceRequest = serviceRequestValue.resource

            val referralReasonList = serviceRequest.reasonCode.takeIf { serviceRequest.hasReasonCode() } ?: emptyList()

            referralReasonList.forEach { reason ->
                val referralReasonText = reason.text.takeIf { reason.hasText() } ?: ""

                if (referralReasonText == "REASON_FOR_REFERRAL") {
                    val codingDisplay = reason.codingFirstRep.display.takeIf { reason.hasCoding() && reason.codingFirstRep.hasDisplay() } ?: ""
                    codingDisplayValue = codingDisplay
                }
            }



            val authoredOn = if(serviceRequest.hasAuthoredOn()) serviceRequest.authoredOn else null
            authoredOnValue = if (authoredOn != null) formatterClass.convertDateFormat(authoredOn.toString()) ?: "" else ""

        }

        if (codingDisplayValue != "" && authoredOnValue != "" && filledConverted != null){
            return DbFormsData(
                serviceRequestId,
                authoredOnValue,
                codingDisplayValue,
                filledConverted.toString()
            )
        }

        return null




    }

    private suspend fun getBacFilledFormList(type:String): ArrayList<DbFormsData>{

        val formDataList = ArrayList<DbFormsData?>()

        fhirEngine
            .search<Encounter> {
                filter(Encounter.SUBJECT, { value = "Patient/$patientId" })
                sort(Encounter.DATE, Order.ASCENDING)
            }
            .map { createEncounterFormItem(it.resource, type) }
            .let {formDataList.addAll(it)}


        return ArrayList(formDataList.filterNotNull())

    }

    private suspend fun createEncounterFormItem(resource: Encounter, type:String):DbFormsData? {

        val reportingDateCode = Constants.PATIENT_REPORTING_DATE
        val contactPerson = Constants.CONTACT_PERSON

        val encounterId = if (resource.hasId()) resource.id else ""
        val filledOn = if(resource.hasPeriod() && resource.period.hasStart()) resource.period.start.time else 0
        val reasonCode = if (resource.hasReasonCode() &&
            resource.reasonCodeFirstRep.hasText()) resource.reasonCodeFirstRep.text else ""

        if (reasonCode != type){
            return null
        }

        val reportingData = getObservationByCode(encounterId, reportingDateCode).firstOrNull()
        val contactPersonData = getObservationByCode(encounterId, contactPerson).firstOrNull()

        val patientDate = reportingData?.text ?: ""
        val contactPersonInfo = contactPersonData?.text ?: ""

        if (patientDate != "" && contactPersonInfo != ""){

            //Convert time in long format to dd/MM/yyyy
            val date = Date(filledOn)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dateStr = dateFormat.format(date)
            val filledData = formatterClass.convertDateFormat(dateStr) ?: ""
            val patientDateConverted = formatterClass.convertDateFormat(patientDate) ?: ""

            val dbFormsData = DbFormsData(
                encounterId,
                patientDateConverted,
                contactPersonInfo,
                filledData
            )

            return dbFormsData
        }


        return null
    }

    private suspend fun getObservationByCode(encounterId: String, fhirCode: String):ArrayList<DbFormData>{

        val observationList = ArrayList<DbFormData>()

        fhirEngine
            .search<Observation> {
                filter(Observation.SUBJECT, { value = "Patient/$patientId" })
                filter(Observation.ENCOUNTER, { value = encounterId })
                filter(Observation.CODE, { value = of(fhirCode) })
                sort(Observation.DATE, Order.ASCENDING)
            }
            .map { createObservationItem(it.resource) }
            .let {observationList.addAll(it)}

        return observationList
    }


}
class ReferralDetailsViewModelFactory(
    private val application: Application,
    private val fhirEngine: FhirEngine,
    private val patientId: String,
    private val serviceRequestId: String,
    private val encounterId: String? = null,
    private val locationViewModel: LocationViewModel? = null,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ReferralDetailsViewModel::class.java)) {
            "Unknown ViewModel class"
        }
        return ReferralDetailsViewModel(application, fhirEngine, patientId, serviceRequestId, encounterId, locationViewModel) as T
    }
}