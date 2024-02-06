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
            name = it.name[0].nameAsSingleString

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
            "patientDob",
            dob,
            getApplication<Application>().applicationContext
        )
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

    fun recommendationList() = runBlocking {
        getRecommendationList()
    }


    private suspend fun getRecommendationList(): ArrayList<DbAppointmentDetails> {
        val recommendationList = ArrayList<DbAppointmentDetails>()


        fhirEngine
            .search<ImmunizationRecommendation> {
                filter(ImmunizationRecommendation.PATIENT, { value = "Patient/$patientId" })
                sort(Encounter.DATE, Order.DESCENDING)
            }
            .map { createRecommendation(it) }
            .let { recommendationList.addAll(it) }




        return recommendationList
    }


    private fun createRecommendation(it: ImmunizationRecommendation): DbAppointmentDetails {

        var appointmentId = ""

        if (it.hasId()) appointmentId = it.id.replace("ImmunizationRecommendation/", "")

        var date = ""

        if (it.hasRecommendation() && it.recommendation.isNotEmpty()) {
            if (it.recommendation[0].hasDateCriterion() &&
                it.recommendation[0].dateCriterion.isNotEmpty() &&
                it.recommendation[0].dateCriterion[0].hasValue()
            ) {
                val dateCriterion = it.recommendation[0].dateCriterion[0].value.toString()
                date = dateCriterion
            }

        }
        var targetDisease = ""
        var doseNumber: String? = ""
        var appointmentStatus = ""
        var vaccineName = ""


        if (it.hasRecommendation()) {
            val recommendation = it.recommendation
            if (recommendation.isNotEmpty()) {
                //targetDisease
                val codeableConceptTargetDisease = recommendation[0].targetDisease
                if (codeableConceptTargetDisease.hasText()) {
                    targetDisease = codeableConceptTargetDisease.text
                }

                //appointment status
                val codeableConceptTargetStatus = recommendation[0].forecastStatus
                if (codeableConceptTargetStatus.hasText()) {
                    appointmentStatus = codeableConceptTargetStatus.text
                }

                //Dose number
                if (recommendation[0].hasDoseNumber()) {
                    doseNumber = recommendation[0].doseNumber.asStringValue()
                }

                //Contraindicated vaccine code
                if (recommendation[0].hasContraindicatedVaccineCode()) {
                    vaccineName = recommendation[0].contraindicatedVaccineCode[0].text
                }

            }
        }





        return DbAppointmentDetails(
            appointmentId,
            date,
            doseNumber,
            targetDisease,
            vaccineName,
            appointmentStatus
        )


    }


    fun getAppointmentList() = runBlocking {
        getAppointmentDetails()
    }

    private suspend fun getAppointmentDetails(): ArrayList<DbAppointmentData> {

        val appointmentList = ArrayList<DbAppointmentData>()

        fhirEngine
            .search<Appointment> {
                filter(Appointment.SUPPORTING_INFO, { value = "Patient/$patientId" })
                sort(Appointment.DATE, Order.DESCENDING)
            }
            .map { createAppointment(it) }
            .let { appointmentList.addAll(it) }

        return appointmentList
    }

    private suspend fun createAppointment(it: Appointment): DbAppointmentData {

        val recommendationList = getRecommendationList()

        val id = if (it.hasId()) it.id else ""
        val status = if (it.hasStatus()) it.status else ""
        val input = if (it.hasDescription()) it.description else ""
        val start = if (it.hasStart()) it.start else ""
        val basedOnImmunizationRecommendationList = if (it.hasBasedOn()) {
            it.basedOn
        } else {
            emptyList()
        }
        var title = ""
        var description = ""
        var dateScheduled = ""
        var recommendationSavedList = ArrayList<DbAppointmentDetails>()

        val pattern = Regex("Title: (.*?) Description:(.*)")
        // Match the pattern in the input text
        val matchResult = pattern.find(input)
        matchResult?.let {
            title = it.groupValues[1].trim()
            description = it.groupValues[2].trim()
        }


        val startDate = FormatterClass().convertDateFormat(start.toString())
        if (startDate != null) {
            dateScheduled = startDate
        }

        basedOnImmunizationRecommendationList.forEach { ref ->
            val immunizationRecommendation = ref.reference
            val recommendationId =
                immunizationRecommendation.replace("ImmunizationRecommendation/", "")
            val selectedRecommendation =
                recommendationList.find { it.appointmentId == recommendationId }
            if (selectedRecommendation != null) {
                recommendationSavedList.add(selectedRecommendation)
            }
        }

        return DbAppointmentData(
            id,
            title,
            description,
            null,
            dateScheduled,
            recommendationSavedList,
            status.toString()
        )


    }

    fun getVaccineList() = runBlocking {
        getVaccineListDetails()
    }

    private suspend fun getVaccineListDetails(): ArrayList<DbVaccineData> {

        val vaccineList = ArrayList<DbVaccineData>()

        fhirEngine
            .search<Immunization> {
                filter(Immunization.PATIENT, { value = "Patient/$patientId" })
                sort(Immunization.DATE, Order.DESCENDING)
            }
            .map { createVaccineItem(it) }
            .let { vaccineList.addAll(it) }

        val newVaccineList = vaccineList.filterNot {
            it.status == "NOTDONE"
        }

        return ArrayList(newVaccineList)
    }

    private fun createVaccineItem(immunization: Immunization): DbVaccineData {

        var vaccineName = ""
        var doseNumberValue = ""
        val logicalId = if (immunization.hasEncounter()) immunization.encounter.reference else ""
        var dateScheduled = ""
        var status = ""

        val ref = logicalId.toString().replace("Encounter/", "")

        if (immunization.hasVaccineCode()) {
            if (immunization.vaccineCode.hasText()) vaccineName = immunization.vaccineCode.text
        }

        if (immunization.hasOccurrenceDateTimeType()) {
            val fhirDate = immunization.occurrenceDateTimeType.valueAsString
            val convertedDate = FormatterClass().convertDateFormat(fhirDate)
            if (convertedDate != null) {
                dateScheduled = convertedDate
            }
        }
        if (immunization.hasProtocolApplied()) {
            if (immunization.protocolApplied.isNotEmpty() && immunization.protocolApplied[0].hasSeriesDoses()) doseNumberValue =
                immunization.protocolApplied[0].seriesDoses.asStringValue()
        }
        if (immunization.hasStatus()) {
            status = immunization.statusElement.value.name
        }

        return DbVaccineData(
            ref,
            vaccineName,
            doseNumberValue,
            dateScheduled,
            status
        )
    }

    private suspend fun createEncounterAefiItem(
        encounter: Encounter,
        resources: Resources
    ): AdverseEventData {

        val type = generateObservationByCode(encounter.logicalId, AEFI_TYPE) ?: ""
        val date = generateObservationByCode(encounter.logicalId, AEFI_DATE) ?: ""
        return AdverseEventData(
            encounter.logicalId,
            type,
            date,
        )
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

    fun loadImmunizationAefis(logicalId: String) = runBlocking {
        loadInternalImmunizationAefis(logicalId)
    }

    private suspend fun loadInternalImmunizationAefis(logicalId: String): List<AdverseEventData> {

        val encounterList = ArrayList<AdverseEventData>()
        fhirEngine
            .search<Encounter> {
                filter(
                    Encounter.SUBJECT,
                    { value = "Patient/$patientId" })
                filter(
                    Encounter.PART_OF,
                    { value = "Encounter/$logicalId" })
                sort(Encounter.DATE, Order.DESCENDING)
            }
            .map {
                createEncounterAefiItem(
                    it,
                    getApplication<Application>().resources
                )
            }
            .let { encounterList.addAll(it) }
        return encounterList.reversed()
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
