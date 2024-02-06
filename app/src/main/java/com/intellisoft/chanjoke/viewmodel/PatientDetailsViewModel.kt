package com.intellisoft.chanjoke.viewmodel


import android.app.Application
import android.content.res.Resources
import android.icu.text.DateFormat
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.DbVaccineData
import com.intellisoft.chanjoke.utils.AppUtils
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.common.datatype.asStringValue
import com.google.android.fhir.logicalId
import com.google.android.fhir.search.Order
import com.google.android.fhir.search.search
import com.intellisoft.chanjoke.fhir.data.AdverseEventData
import com.intellisoft.chanjoke.fhir.data.DbAppointmentData
import com.intellisoft.chanjoke.fhir.data.DbAppointmentDetails
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.Identifiers
import com.intellisoft.chanjoke.fhir.data.ObservationDateValue
import com.intellisoft.chanjoke.patient_list.PatientListViewModel
import com.intellisoft.chanjoke.utils.Constants.AEFI_DATE
import com.intellisoft.chanjoke.utils.Constants.AEFI_TYPE
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Appointment
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Encounter
import org.hl7.fhir.r4.model.Immunization
import org.hl7.fhir.r4.model.ImmunizationRecommendation
import org.hl7.fhir.r4.model.Observation
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


    fun getPatientInfo() = runBlocking {
        getPatientDetailDataModel()
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
        var systemId = ""
        searchResult.first().let {

            name =if (it.hasName()){
                it.name[0].family.toString()
            }else ""


            phone = ""
            if (it.hasTelecom()) {
                if (it.telecom.isNotEmpty()) {
                    if (it.telecom.first().hasValue()) {
                        phone = it.telecom.first().value
                    }
                }
            }

            if (it.hasBirthDateElement()) {
                if (it.birthDateElement.hasValue()) dob =
                    LocalDate.parse(it.birthDateElement.valueAsString, DateTimeFormatter.ISO_DATE)
                        .toString()
            }

            if (it.hasContact()) {
                if (it.contactFirstRep.hasName()) contact_name =
                    if (it.hasContact()) {
                        if (it.contactFirstRep.hasName()) {
                            it.contactFirstRep.name.nameAsSingleString
                        } else ""
                    } else ""
                if (it.contactFirstRep.hasTelecom()) contact_phone =
                    if (it.hasContact()) {
                        if (it.contactFirstRep.hasTelecom()) {
                            if (it.contactFirstRep.telecomFirstRep.hasValue()) {
                                it.contactFirstRep.telecomFirstRep.value
                            } else ""
                        } else ""
                    } else ""
                if (it.contactFirstRep.hasGenderElement()) contact_gender =
                    if (it.hasContact()) AppUtils().capitalizeFirstLetter(it.contactFirstRep.genderElement.valueAsString) else ""
            }

            if (it.hasGenderElement()) gender = it.genderElement.valueAsString

            if (it.hasIdentifier()) {
                it.identifier.forEach { identifier ->
                    val codeableConceptType = identifier.type
                    if (codeableConceptType.hasText() && codeableConceptType.text.contains(
                            Identifiers.SYSTEM_GENERATED.name
                        )
                    ) {
                        if (identifier.hasValue()) {
                            systemId = identifier.value
                        }
                    }
                }
            }
        }

        FormatterClass().saveSharedPref(
            "patientId",
            patientId,
            getApplication<Application>().applicationContext
        )

        return PatientData(
            name,
            phone,
            dob,
            gender,
            contact_name = contact_name,
            contact_phone = contact_phone,
            contact_gender = contact_gender,
            systemId
        )
    }

    data class PatientData(
        val name: String,
        val phone: String,
        val dob: String,
        val gender: String,
        val contact_name: String?,
        val contact_phone: String?,
        val contact_gender: String?,
        val systemId: String?,
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




    private suspend fun generateObservationByCode(encounterId: String, codeValue: String): String? {
        var data = ""
        fhirEngine
            .search<Observation> {
                filter(Observation.SUBJECT, { value = "Patient/$patientId" })
                filter(Observation.ENCOUNTER, { value = "Encounter/$encounterId" })
                filter(
                    Observation.CODE,
                    {
                        value = of(Coding().apply {
                            system = "http://loinc.org"
                            code = codeValue
                        })
                    })
                sort(Observation.DATE, Order.ASCENDING)
            }
            .map { createObservationItem(it, getApplication<Application>().resources) }
            .firstOrNull()?.let {
                data = it.value
            }
        return data
    }

    fun getObservationByCode(
        patientId: String,
        encounterId: String?,
        code: String
    ) = runBlocking {
        getObservationDataByCode(patientId, encounterId, code)
    }


    private suspend fun getObservationDataByCode(
        patientId: String,
        encounterId: String?,
        codeValue: String
    ): ObservationDateValue {
        var date = ""
        var dataValue = ""
        fhirEngine
            .search<Observation> {
                filter(Observation.SUBJECT, { value = "Patient/$patientId" })
                if (encounterId != null) filter(
                    Observation.ENCOUNTER,
                    { value = "Encounter/$encounterId" })
                filter(
                    Observation.CODE,
                    {
                        value = of(Coding().apply {
                            system = "http://loinc.org"
                            code = codeValue
                        })
                    })
                sort(Observation.DATE, Order.ASCENDING)
            }
            .map { createObservationItem(it, getApplication<Application>().resources) }
            .firstOrNull()?.let {
                date = it.effective
                dataValue = it.value
            }

        return ObservationDateValue(
            date,
            dataValue,
        )

    }

    private fun createObservationItem(
        observation: Observation,
        resources: Resources
    ): PatientListViewModel.ObservationItem {
        val observationCode = observation.code.codingFirstRep.code ?: ""


        // Show nothing if no values available for datetime and value quantity.
        val value =
            when {
                observation.hasValueQuantity() -> {
                    observation.valueQuantity.value.toString()
                }

                observation.hasValueCodeableConcept() -> {
                    observation.valueCodeableConcept.coding.firstOrNull()?.display ?: ""
                }

                observation.hasNote() -> {
                    observation.note.firstOrNull()?.author
                }

                observation.hasValueDateTimeType() -> {
                    formatDateToHumanReadable(observation.valueDateTimeType.value.toString())

                }

                observation.hasValueStringType() -> {
                    observation.valueStringType.value.toString()
                }

                else -> {
                    observation.code.text ?: observation.code.codingFirstRep.display
                }
            }
        val valueUnit =
            if (observation.hasValueQuantity()) {
                observation.valueQuantity.unit ?: observation.valueQuantity.code
            } else {
                ""
            }
        val valueString = "$value $valueUnit"
        val dateTimeString = if (observation.hasIssued()) observation.issued.toString() else ""


        return PatientListViewModel.ObservationItem(
            observation.logicalId,
            observationCode,
            "$dateTimeString",
            "$valueString",
        )
    }

    private fun formatDateToHumanReadable(date: String): String? {
        return FormatterClass().convertDateFormat(date)

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
