package com.intellisoft.lhss25.referrals.viewmodels

import android.app.Application
import android.util.Log
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
import com.intellisoft.lhss25.fhir.FhirApplication
import com.intellisoft.lhss25.shared.DbPatientItem
import com.intellisoft.lhss25.shared.DbServiceRequest
import com.intellisoft.lhss25.shared.FormatterClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.ServiceRequest
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

class ReferralPatientListViewModel(
    application: Application
) : AndroidViewModel(application) {
    // TODO: Implement the ViewModel

    private var formatterClass = FormatterClass(application.applicationContext)

    private var fhirEngine: FhirEngine =
        FhirApplication.fhirEngine(application.applicationContext)

    val liveSearchedPatients = MutableLiveData<List<DbPatientItem>>()
    val patientCount = MutableLiveData<Long>()
    val allReferralNumber = MutableLiveData<Int>()
    val completeReferralNumber = MutableLiveData<Int>()
    val activeReferralNumber = MutableLiveData<Int>()

    init {
        updatePatientListAndPatientCount({ getSearchResults("") }, { count() }, {getReferralCount()} )
    }

    private fun updatePatientListAndPatientCount(
        search: suspend () -> List<DbPatientItem>,
        count: suspend () -> Long,
        getReferralCount: suspend () -> Triple<Int, Int, Int>
    ) {
        viewModelScope.launch {
            liveSearchedPatients.value = search()
            patientCount.value = count()
            val (allReferralNumbers, completeReferralNumbers, activeReferralNumbers) = getReferralCount()
            allReferralNumber.value = allReferralNumbers
            completeReferralNumber.value = completeReferralNumbers
            activeReferralNumber.value = activeReferralNumbers
        }
    }

    private suspend fun getReferralCount():Triple<Int, Int, Int> {
        //Get the list of patients and then get the list of referrals
        val patients = getSearchResults()

        val serviceList: MutableList<ServiceRequest?> = mutableListOf()

        patients.forEach { dbPatientItem ->

            val patientId = dbPatientItem.id

            fhirEngine
                .search<ServiceRequest> {
                    filter(ServiceRequest.SUBJECT, {value = "Patient/$patientId"})
                }
                .mapIndexed { index, serviceRequest -> createServiceRequestBac(serviceRequest.resource) }
                .let { serviceList.addAll(it) }

        }

        val completeStatusList = serviceList.filterNotNull().filter { it.status.toString() == "completed" || it.status.toString() == "COMPLETED" }.distinctBy { it.id }
        val activeStatusList = serviceList.filterNotNull().filter { it.status.toString() == "active" || it.status.toString() == "ACTIVE" }.distinctBy { it.id }

        val allReferralNumbers = serviceList.size
        val completeReferralNumbers = completeStatusList.size
        val activeReferralNumbers = activeStatusList.size

        allReferralNumber.postValue(allReferralNumbers)
        completeReferralNumber.postValue(completeReferralNumbers)
        activeReferralNumber.postValue(activeReferralNumbers)

        formatterClass.saveSharedPref("","allReferralNumbers", allReferralNumbers.toString())
        formatterClass.saveSharedPref("","completeReferralNumbers", completeReferralNumbers.toString())
        formatterClass.saveSharedPref("","activeReferralNumbers", activeReferralNumbers.toString())

        return Triple(allReferralNumbers, completeReferralNumbers, activeReferralNumbers )
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



    fun getTotalReferrals()= runBlocking {
        getTotalReferralsBac()
    }

    private suspend fun getTotalReferralsBac(): ArrayList<DbServiceRequest> {

        var serviceList: MutableList<DbServiceRequest?> = mutableListOf()
        var serviceList1: MutableList<ServiceRequest?> = mutableListOf()

        fhirEngine
            .search<ServiceRequest> {
            sort(ServiceRequest.AUTHORED, Order.ASCENDING)
            }
            .mapIndexed { index, serviceRequest -> createServiceRequestBac(serviceRequest.resource) }
            .let { serviceList1.addAll(it) }

        return ArrayList(serviceList.filterNotNull())

    }

    private fun createServiceRequestBac(resource: ServiceRequest):ServiceRequest {

        val status = if (resource.hasStatus()) resource.status.toString() else ""
        var isUsersFacility = false
        var savedReference = ""

        val userFacility = formatterClass.getSharedPref("","userFacility")
        val locationReferenceList = if (resource.hasLocationReference()) resource.locationReference else null
        locationReferenceList?.forEach { reference ->
            if (reference.hasReference() && reference.hasReferenceElement()){
                if (userFacility == reference.referenceElement_.valueAsString){
                    savedReference = reference.referenceElement_.valueAsString
                    isUsersFacility = true
                }
            }
        }

        return resource
    }

    private suspend fun createServiceRequest(resource: ServiceRequest):DbServiceRequest? {


        val id = resource.id.replace("ServiceRequest/","")


        val patientId = if (resource.hasSubject())
            resource.subject.referenceElement_
                .toString().replace("Patient/","")

        else ""
        val status = if (resource.hasStatus()) resource.status.toString() else ""
        val occurrenceDateTime = if (resource.hasOccurrenceDateTimeType()) resource.occurrenceDateTimeType.toString().replace("DateTimeType[", "") else null
        val supportingInfo = if (resource.hasSupportingInfo()) resource.supportingInfo else emptyList()
        val reasonCodeList = if (resource.hasReasonCode()) resource.reasonCode else emptyList()
        var isReferral = false

        var isUsersFacility = false
        var display = ""

        val userFacility = formatterClass.getSharedPref("","userFacility")
        val locationReferenceList = if (resource.hasLocationReference()) resource.locationReference else null
        locationReferenceList?.forEach { reference ->
            if (reference.hasReference() && reference.hasReferenceElement()){
                if (userFacility == reference.referenceElement_.valueAsString){
                    isUsersFacility = true
                }
            }
        }


        reasonCodeList.forEach {

            val text = if (it.hasText()) it.text else ""
            if (text == "REFERRAL_MODULE") isReferral = true
            if (text == "REASON_FOR_REFERRAL"){
                val coding = if (it.hasCoding()) it.codingFirstRep else null
                if (coding != null){
                    display = if (coding.hasDisplay()) coding.displayElement.toString() else ""
                }
            }
        }

        if (isReferral && isUsersFacility){

            if (status == "ACTIVE"){
                return DbServiceRequest(
                    id,
                    patientId,
                    status,
                    occurrenceDateTime.toString(),
                    ArrayList(supportingInfo),
                    reasonCodeList.firstOrNull()?.text
                )
            }

        }




        return null

    }


    fun searchPatientsByName(nameQuery: String) {
        updatePatientListAndPatientCount({ getSearchResults(nameQuery) }, { count(nameQuery) }, {getReferralCount()})
    }

    private suspend fun getSearchResults(nameQuery: String = ""): ArrayList<DbPatientItem> {

        val dbPatientItemList = ArrayList<DbPatientItem?>()

        var patients: MutableList<DbPatientItem> = mutableListOf()

        fhirEngine
            .search<ServiceRequest> {}
            .mapIndexed { index, fhirPatient -> createServiceRequestPatient(fhirPatient.resource) }
            .let { dbPatientItemList.addAll(it) }

        val sortedPatients = sortByMostRecentDateCreated(dbPatientItemList)

        patients = sortedPatients.filterNotNull().toMutableList()

        return ArrayList(patients)
    }



    fun sortByMostRecentDateCreated(patientList: ArrayList<DbPatientItem?>): List<DbPatientItem?> {
        val dateFormat = SimpleDateFormat("MMM d yyyy", Locale.ENGLISH)

        return patientList.sortedByDescending { patient ->
            // Parse dateCreated, return null if date is not valid
            patient?.dateCreated?.let { dateString ->
                try {
                    dateFormat.parse(dateString)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }


    private suspend fun createServiceRequestPatient(resource: ServiceRequest):DbPatientItem? {

        val id = resource.id.replace("ServiceRequest/","")

        val patientId = if (resource.hasSubject())
            resource.subject.referenceElement_
                .toString().replace("Patient/","")

        else ""
        val status = if (resource.hasStatus()) resource.status.toString() else ""
        val occurrenceDateTime = if (resource.hasOccurrenceDateTimeType()) resource.occurrenceDateTimeType.toString().replace("DateTimeType[", "") else null
        val supportingInfo = if (resource.hasSupportingInfo()) resource.supportingInfo else emptyList()
        val reasonCodeList = if (resource.hasReasonCode()) resource.reasonCode else emptyList()
        var isReferral = false
        var isUsersFacility = false
        var display = ""

        val userFacility = formatterClass.getSharedPref("","userFacility")
        val locationReferenceList = if (resource.hasLocationReference()) resource.locationReference else null
        val locationReference = locationReferenceList?.find { it.reference == userFacility }
        if (locationReference != null){
            isUsersFacility = true
        }

        reasonCodeList.forEach {

            val text = if (it.hasText()) it.text else ""
            if (text == "REFERRAL_MODULE") isReferral = true
            if (text == "REASON_FOR_REFERRAL"){
                val coding = if (it.hasCoding()) it.codingFirstRep else null
                if (coding != null){
                    display = if (coding.hasDisplay()) coding.displayElement.toString() else ""
                }
            }
        }

        if (isReferral && isUsersFacility){

            val searchResult =
                fhirEngine.search<Patient> {
                    filter(Resource.RES_ID, { value = of(patientId) })
                }

            if (searchResult.isNotEmpty()){
                searchResult.first().let {
                    val patient = it.resource
                    //Name
                    var name = ""
                    if (patient.hasName()){
                        patient.name.forEach {humanName->
                            val tag = if (humanName.hasText()) humanName.text else ""
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
                    var dob = ""
                    if (patient.hasBirthDateElement()) {
                        if (patient.birthDateElement.hasValue()) {
                            val birthDateElement = patient.birthDateElement.valueAsString
                            dob = birthDateElement.toString()
                        }
                    }
                    //DateCreated
                    var dateCreated = ""
                    if (resource.hasAuthoredOn()) {
                        val dateAuthoredOn = formatterClass.convertDateFormat(resource.authoredOn.toString())
                        if (dateAuthoredOn != null) dateCreated = dateAuthoredOn
                    }

                    return DbPatientItem(
                        patientId,
                        name,
                        patientId.substring(0..6),
                        dob,
                        dateCreated
                    )

                }

            }

        }
        return null

    }

    class ReferralPatientListViewModelFactory(
        private val application: Application,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ReferralPatientListViewModel::class.java)) {
                return ReferralPatientListViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}