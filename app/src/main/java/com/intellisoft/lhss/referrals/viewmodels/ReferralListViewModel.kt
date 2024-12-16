package com.intellisoft.lhss.referrals.viewmodels

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
import com.intellisoft.lhss.patient_details.viewmodel.PatientListViewModel
import com.intellisoft.lhss.shared.DbPatientItem
import com.intellisoft.lhss.shared.DbServiceRequest
import com.intellisoft.lhss.shared.FormatterClass
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.ServiceRequest

class ReferralListViewModel(
    application: Application,
    private val fhirEngine: FhirEngine,
    patientId: String
) : AndroidViewModel(application) {

    val liveSearchedPatients = MutableLiveData<List<DbServiceRequest?>>()
    val patientCount = MutableLiveData<Long>()
    val formatterClass = FormatterClass(application.applicationContext)
    val patientIdValue = patientId

    init {
        updatePatientListAndPatientCount({ getSearchResults("") }, { count() })
    }

    private fun updatePatientListAndPatientCount(
        search: suspend () -> List<DbServiceRequest?>,
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

    private suspend fun getSearchResults(nameQuery: String = ""):
            ArrayList<DbServiceRequest?> {

        var patients: MutableList<DbServiceRequest?> = mutableListOf()

        fhirEngine
            .search<ServiceRequest> {
                if (patientIdValue != ""){
                    filter(ServiceRequest.SUBJECT, { value = "Patient/$patientIdValue" })
                }
            }
            .mapIndexed { index, fhirPatient -> createServiceRequest(fhirPatient.resource) }
            .let { patients.addAll(it) }

        //Remove status with value COMPLETED
        patients = patients.filter { it?.status!= "COMPLETED" } as MutableList<DbServiceRequest?>

        return ArrayList(patients)
    }

    private fun createServiceRequest(resource: ServiceRequest):DbServiceRequest? {

        val id = resource.id.replace("ServiceRequest/","")
        val patient = if (resource.hasSubject())
            resource.subject.referenceElement_
                .toString().replace("Patient/","")
        else ""
        val status = if (resource.hasStatus()) resource.status.toString() else ""
        val occurrenceDateTime = if (resource.hasOccurrenceDateTimeType()) resource.occurrenceDateTimeType.toString().replace("DateTimeType[", "") else null
        val supportingInfo = if (resource.hasSupportingInfo()) resource.supportingInfo else emptyList()
        val reasonCodeList = if (resource.hasReasonCode()) resource.reasonCode else emptyList()
        var isReferral = false
        var display = ""

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

        if (isReferral){

            var convertedDate = ""
            if (occurrenceDateTime != null){

                val convertedDateString = formatterClass
                    .convertDateFormat(occurrenceDateTime.toString().replace("]",""))

                if (convertedDateString != null){
                    convertedDate = convertedDateString.toString()
                }
            }

            val dbServiceRequest = DbServiceRequest(
                id,
                patient,
                status,
                convertedDate,
                ArrayList(supportingInfo),
                display
            )
            return dbServiceRequest
        }

        return null
    }

    class PatientListViewModelFactory(
        private val application: Application,
        private val fhirEngine: FhirEngine,
        private val patientId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ReferralListViewModel::class.java)) {
                return ReferralListViewModel(application, fhirEngine, patientId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}