package com.intellisoft.lhss.viewmodel


import android.app.Application
import android.icu.text.DateFormat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope

import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.common.datatype.asStringValue
import com.google.android.fhir.search.Order
import com.google.android.fhir.search.search
import com.intellisoft.lhss.R
import com.intellisoft.lhss.fhir.data.DbVaccineData
import com.intellisoft.lhss.utils.AppUtils
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Encounter
import org.hl7.fhir.r4.model.Immunization
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.RiskAssessment

/**
 * The ViewModel helper class for PatientItemRecyclerViewAdapter, that is responsible for preparing
 * data for UI.
 */
class PatientDetailsViewModel(
    application: Application,
    private val fhirEngine: FhirEngine,
    private val patientId: String,
) : AndroidViewModel(application) {
    val livePatientData = MutableLiveData<PatientData>()

    /** Emits list of [PatientDetailData]. */
    fun getPatientDetailData() {
        viewModelScope.launch { livePatientData.value = getPatientDetailDataModel() }
    }

    private suspend fun getPatientDetailDataModel(): PatientData {
        val searchResult =
            fhirEngine.search<Patient> {
                filter(Resource.RES_ID, { value = of(patientId) })
            }
        var name = ""
        var phone = ""
        var dob = ""
        var gender = ""
        var contact_name = ""
        var contact_phone = ""
        var contact_gender = ""
        searchResult.first().let {
            name = it.name[0].nameAsSingleString
            phone = it.telecom.first().value
            dob = LocalDate.parse(it.birthDateElement.valueAsString, DateTimeFormatter.ISO_DATE)
                .toString()
            gender = it.genderElement.valueAsString
            contact_name = if (it.hasContact()) it.contactFirstRep.name.nameAsSingleString else ""
            contact_phone = if (it.hasContact()) it.contactFirstRep.telecomFirstRep.value else ""
            contact_gender =
                if (it.hasContact()) AppUtils().capitalizeFirstLetter(it.contactFirstRep.genderElement.valueAsString) else ""
        }

        return PatientData(
            name,
            phone,
            dob,
            gender,
            contact_name = contact_name,
            contact_phone = contact_phone,
            contact_gender = contact_gender
        )
    }


    data class PatientData(
        val name: String,
        val phone: String,
        val dob: String,
        val gender: String,
        val contact_name: String?,
        val contact_phone: String?,
        val contact_gender: String?
    ) {
        override fun toString(): String = name
    }


    private val LocalDate.localizedString: String
        get() {
            val date = Date.from(atStartOfDay(ZoneId.systemDefault())?.toInstant())
            return if (isAndroidIcuSupported()) {
                DateFormat.getDateInstance(DateFormat.DEFAULT).format(date)
            } else {
                SimpleDateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault())
                    .format(date)
            }
        }

    // Android ICU is supported API level 24 onwards.
    private fun isAndroidIcuSupported() = true

    private fun getString(resId: Int) = getApplication<Application>().resources.getString(resId)

    private fun getLastContactedDate(riskAssessment: RiskAssessment?): String {
        riskAssessment?.let {
            if (it.hasOccurrence()) {
                return LocalDate.parse(
                    it.occurrenceDateTimeType.valueAsString,
                    DateTimeFormatter.ISO_DATE_TIME,
                )
                    .localizedString
            }
        }
        return getString(R.string.none)
    }


    fun getEncounterList()= runBlocking{
        getEncounterDetails()
    }
    private suspend fun getEncounterDetails():ArrayList<DbVaccineData>{

        val encounterList = ArrayList<DbVaccineData>()

        fhirEngine
            .search<Immunization> {
                filter(Immunization.PATIENT, { value = "Patient/$patientId" })
                sort(Encounter.DATE, Order.DESCENDING)
            }
            .map { createEncounterItem(it) }
            .let { encounterList.addAll(it) }


        return encounterList
    }

    fun createEncounterItem(immunization: Immunization): DbVaccineData{

        var targetDisease = ""
        var doseNumberValue = ""

        val protocolList = immunization.protocolApplied
        protocolList.forEach {

            //Target Disease
            val targetDiseaseList = it.targetDisease
            if (targetDiseaseList.isNotEmpty()) targetDisease = targetDiseaseList[0].text

            //Dose number
            val doseNumber = it.doseNumber
            if (doseNumber != null) doseNumberValue = doseNumber.asStringValue()


        }


        return DbVaccineData(
            targetDisease,
            doseNumberValue
        )
    }


}

class PatientDetailsViewModelFactory(
    private val application: Application,
    private val fhirEngine: FhirEngine,
    private val patientId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(PatientDetailsViewModel::class.java)) {
            "Unknown ViewModel class"
        }
        return PatientDetailsViewModel(application, fhirEngine, patientId) as T
    }
}
