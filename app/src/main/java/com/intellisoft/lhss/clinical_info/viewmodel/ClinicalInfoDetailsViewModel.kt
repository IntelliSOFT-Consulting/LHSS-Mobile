package com.intellisoft.lhss.clinical_info.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.search.Order
import com.google.android.fhir.search.search
import com.intellisoft.lhss.fhir.FhirApplication
import com.intellisoft.lhss.shared.DbCarePlan
import com.intellisoft.lhss.shared.DbEncounterDetails
import com.intellisoft.lhss.shared.DbNavigationDetails
import com.intellisoft.lhss.shared.DbServiceRequest
import com.intellisoft.lhss.shared.FormatterClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.CarePlan
import org.hl7.fhir.r4.model.CarePlan.CarePlanStatus
import org.hl7.fhir.r4.model.Encounter
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.ServiceRequest
import java.util.Date

class ClinicalInfoDetailsViewModel(
    application: Application,
    private val patientId: String,
) : AndroidViewModel(application) {

    private var formatterClass = FormatterClass(application.applicationContext)

    private var fhirEngine: FhirEngine =
        FhirApplication.fhirEngine(application.applicationContext)


    fun getCarePlanAndEncounters(carePlanId: String) = runBlocking {
        getEncountersBac(carePlanId)
    }



    private suspend fun getEncountersBac(carePlanId: String): ArrayList<DbEncounterDetails> {
        val dbEncounterDetailsList = ArrayList<DbEncounterDetails>()
        fhirEngine
            .search<Encounter> {
                filter(Encounter.SUBJECT, { value = "Patient/$patientId" })
                filter(Encounter.BASED_ON, { value = "CarePlan/$carePlanId" })
                sort(Encounter.DATE, Order.ASCENDING)
            }
            .map { createEncounterItem(it.resource) }
            .let {dbEncounterDetailsList.addAll(it)}
        return dbEncounterDetailsList
    }

    private fun createEncounterItem(resource: Encounter):DbEncounterDetails {
        val id = resource.id
        val reasonCode = if (resource.hasReasonCode()) {
            resource.reasonCodeFirstRep.text
        }else ""
        val date = if (resource.hasPeriod()) {
            formatterClass.convertDateFormat(resource.period.start.toString()) ?: ""
        }else ""
        val status = if (resource.hasStatus()) {
            resource.status.toString()
        }else ""

        return DbEncounterDetails(
            id,
            reasonCode,
            date,
            status
        )

    }

    fun closeCarePlan(carePlanId: String) {
        val supportingInfo = ArrayList<Reference>()
        updateCarePlanStatus(CarePlanStatus.COMPLETED, carePlanId, supportingInfo)
    }

    fun getServiceRequests() = runBlocking {
        getServiceRequestsBac()
    }

    private suspend fun getServiceRequestsBac(): ArrayList<DbServiceRequest> {

        val serviceRequestList = ArrayList<DbServiceRequest>()
        fhirEngine
            .search<ServiceRequest> {
                filter(CarePlan.SUBJECT, { value = "Patient/$patientId" })
                sort(CarePlan.DATE, Order.ASCENDING)
            }
            .map { createServiceRequestItem(it.resource) }
            .let {serviceRequestList.addAll(it)}

        return serviceRequestList
    }

    private fun createServiceRequestItem(resource: ServiceRequest): DbServiceRequest {

        val formatterClass = FormatterClass(getApplication())

        val id = if (resource.hasId()) {resource.id} else ""
        val status = if (resource.hasStatus()) {resource.status.toString()} else ""
        val dateRecorded = if (resource.hasAuthoredOn()){
            formatterClass.convertDateFormat(resource.authoredOn.toString())?: ""
        } else ""
        val encounterList = if (resource.hasSupportingInfo()) {
            resource.supportingInfo
        }else emptyList()
        val referralReason = if (resource.hasReasonCode() && resource.reasonCodeFirstRep.hasCoding()) {
            resource.reasonCodeFirstRep.codingFirstRep.display
        }else ""

        return DbServiceRequest(
            id,
            patientId,
            status,
            dateRecorded,
            ArrayList(encounterList),
            referralReason
        )


    }

    fun getCarePlanDetails() = runBlocking {
        getCarePlanDetailsBac()
    }

    private suspend fun getCarePlanDetailsBac(): ArrayList<DbCarePlan> {

        val carePlanList = ArrayList<DbCarePlan>()

        fhirEngine
            .search<CarePlan> {
                filter(CarePlan.SUBJECT, { value = "Patient/$patientId" })
                sort(CarePlan.DATE, Order.ASCENDING)
            }
            .map { createCarePlanItem(it.resource) }
            .let {carePlanList.addAll(it)}

        val activeCarePlans = carePlanList.filterActive("ACTIVE")
        val completedCarePlans = carePlanList.filterActive("COMPLETED")

        //Combine active and completed care plans into a single list
        val combinedCarePlans = activeCarePlans + completedCarePlans

        return ArrayList(combinedCarePlans)

    }

    private fun List<DbCarePlan>.filterActive(status: String): List<DbCarePlan> {
        return this.filter { it.status == status }
    }

    private fun createCarePlanItem(resource: CarePlan): DbCarePlan {

        val id = resource.id
        val date = resource.created.toString()
        val status = resource.status.toString()
        val supportingInfoList = resource.supportingInfo

        val dateCreated = formatterClass.convertDateFormat(date) ?: ""

        return DbCarePlan(
            id,
            status,
            "",
            dateCreated,
            ArrayList(supportingInfoList)
        )

    }

    fun createCarePlan(){
        CoroutineScope(Dispatchers.IO).launch {
            createCarePlanBac()
        }
    }

    fun updateCarePlanStatus(
        status: CarePlanStatus,
        carePlanId: String,
        supportingInfoList: ArrayList<Reference>){

        CoroutineScope(Dispatchers.IO).launch {
            updateCarePlanStatusBac(status,carePlanId, supportingInfoList)
        }
    }

    private suspend fun createCarePlanBac(){

        /**
         * 1. Create a new CarePlan resource, with a status of Draft
         * 2. Once a clinical information is entered, update the status to active
         */

        val id = formatterClass.generateUuid()

        val carePlan = CarePlan()
        carePlan.id = id
        carePlan.status = CarePlanStatus.DRAFT
        carePlan.created = Date()
        carePlan.subject = Reference("Patient/$patientId")
        carePlan.title = "Clinical Information"
        carePlan.description = "Clinical information for patient: $patientId"
        saveResourceToDatabase(carePlan, "CarePlan")

        formatterClass.saveSharedPref(
            sharedPrefName = DbNavigationDetails.CARE_PLAN.name,
            "carePlanId",
            id
        )

    }

    private suspend fun updateCarePlanStatusBac(
        status: CarePlanStatus,
        carePlanId: String,
        supportingInfoList: ArrayList<Reference>){

        val searchResult =
            fhirEngine.search<CarePlan> {
                filter(Resource.RES_ID, { value = of(carePlanId) })
            }

        if (searchResult.isNotEmpty()) {
            searchResult.first().let {
                val carePlan = it.resource
                carePlan.status = status

                supportingInfoList.forEach { supportingInfo ->
                    carePlan.supportingInfo.add(supportingInfo)
                }

                updateResourceToDatabase(carePlan, "CarePlan")
            }

        }

    }

    private suspend fun updateResourceToDatabase(resource: Resource, s: String) {
        Log.e("----Upd", "----$s")
        fhirEngine.update(resource)
    }

    private suspend fun saveResourceToDatabase(resource: Resource, type: String) :List<String>{
        Log.e("----", "----$type")
        return fhirEngine.create(resource)
    }

    class ClinicalInfoDetailsViewModelFactory(
        private val application: Application,
        private val patientId: String

    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ClinicalInfoDetailsViewModel::class.java)) {
                return ClinicalInfoDetailsViewModel(application, patientId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}