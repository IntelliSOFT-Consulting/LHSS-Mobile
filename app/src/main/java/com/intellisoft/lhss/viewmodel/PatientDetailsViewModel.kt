package com.intellisoft.lhss.viewmodel


import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.icu.text.DateFormat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.intellisoft.lhss.R
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.logicalId
import com.google.android.fhir.search.Order
import com.google.android.fhir.search.search
import com.intellisoft.lhss.fhir.data.DbEncounterReferrals
import com.intellisoft.lhss.fhir.data.DbObservation
import com.intellisoft.lhss.fhir.data.DbPatientDataDetails
import com.intellisoft.lhss.fhir.data.FormatterClass
import com.intellisoft.lhss.fhir.data.Identifiers
import com.intellisoft.lhss.fhir.data.ObservationDateValue
import com.intellisoft.lhss.patient_list.PatientListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Encounter
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

    fun getReferralDetails(encounterId: String)= runBlocking{
        generateObservationByCode(encounterId)
    }

    fun updateEncounter(encounterId: String){

        CoroutineScope(Dispatchers.IO).launch {
            val encounterList = ArrayList<Encounter>()
            fhirEngine
                .search<Encounter> {
                    sort(Observation.DATE, Order.ASCENDING)
                }
                .map { createWorkflowItemIt(it) }
                .let { encounterList.addAll(it) }

            val encounter = encounterList.firstOrNull()
            if (encounter != null) fhirEngine.update(encounter)
        }

    }

    private fun createWorkflowItemIt(it: Encounter):Encounter {

        it.status = Encounter.EncounterStatus.FINISHED

        return it
    }

    private suspend fun generateObservationByCode(encounterId: String): ArrayList<DbObservation> {
        val observationList = ArrayList<DbObservation>()
        fhirEngine
            .search<Observation> {
                filter(Observation.SUBJECT, { value = "Patient/$patientId" })
                filter(Observation.ENCOUNTER, { value = "Encounter/$encounterId" })

                sort(Observation.DATE, Order.ASCENDING)
            }
            .map { createObservationDataItem(it, encounterId)}
            .let { observationList.addAll(it) }

        return observationList
    }

    fun getUserDetails(context: Context):ArrayList<DbPatientDataDetails>{

        val formatterClass = FormatterClass()
        val dbPatientDataDetailsList = ArrayList<DbPatientDataDetails>()


        val patientData = getPatientInfo()

        val ageValue = FormatterClass().getFormattedAge(patientData.dob, context.resources)
        val dobValue = formatterClass.convertDateFormat(patientData.dob)

        val name = DbPatientDataDetails("Name", patientData.name)
        val dob = DbPatientDataDetails("Date Of Birth", dobValue.toString())
        val gender = DbPatientDataDetails("Gender", patientData.gender)
        val phone = DbPatientDataDetails("Phone", patientData.phone)
        val documentType = DbPatientDataDetails("Document Type", patientData.docType)
        val documentId = DbPatientDataDetails("Document Id", patientData.docId)
        val age = DbPatientDataDetails("Age", ageValue)
        val occupation = DbPatientDataDetails("Occupation", patientData.occupation)

        val id =  "${patientData.crossBorderId.toString().replace("Patient/", "").substring(0, 4)}"
        val crossBorderId = DbPatientDataDetails("CrossBorder Id",id)

        dbPatientDataDetailsList.addAll(listOf(
            name, dob, gender, phone, documentType, documentId, crossBorderId, age, occupation))
        return dbPatientDataDetailsList

    }
    fun getAddressDetails():ArrayList<DbPatientDataDetails>{

        val dbPatientDataDetailsList = ArrayList<DbPatientDataDetails>()

        val patientData = getPatientInfo()
        val originCountry = DbPatientDataDetails("Country of Origin", patientData.originCountry)
        val residenceCountry = DbPatientDataDetails("Country of Residence", patientData.residenceCountry)
        val region = DbPatientDataDetails("Region", patientData.region)
        val district = DbPatientDataDetails("District", patientData.district)

        dbPatientDataDetailsList.addAll(listOf(
            originCountry, residenceCountry, region, district ))
        return dbPatientDataDetailsList

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
        var systemId = ""

        var docType = ""
        var docId = ""
        var crossBorderId = ""

        var occupation = ""
        var residenceCountry = ""
        var originCountry = ""
        var region = ""
        var district = ""

        searchResult.first().let {

            name =if (it.hasName()){
                it.name[0].family.toString()
            }else ""


            phone = ""

            if (it.hasContact()){
                if (it.contactFirstRep.hasTelecom()){
                    if (it.contactFirstRep.telecomFirstRep.hasValue()){
                        phone = it.contactFirstRep.telecomFirstRep.value
                    }
                }
            }


            if (it.hasBirthDateElement()) {
                if (it.birthDateElement.hasValue()) dob =
                    LocalDate.parse(it.birthDateElement.valueAsString, DateTimeFormatter.ISO_DATE)
                        .toString()
            }

            if (it.hasGender()){
                gender = it.genderElement.valueAsString
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

            if (it.hasAddress()){
                val addressList = it.address
                addressList.forEach {address ->
                    if (address.hasText()){
                        val text = address.text
                        val country = address.country

                        if (text == "Country of Residence"){
                            val districtValue = address.district
                            val city = address.city
                            region = city
                            district = districtValue
                            residenceCountry = country
                            FormatterClass().saveSharedPref(
                                "country",
                                country,
                                getApplication<Application>().applicationContext)

                        }
                        if (text == "Country of Origin"){
                            originCountry = country
                        }
                    }

                }

            }

            if (it.hasIdentifier()){
                val identifierList = it.identifier
                identifierList.forEach { id ->
                    if (id.hasType()){
                        val typeText = id.type.text
                        val valueData = id.value

                        if (typeText == "Occupation"){
                            occupation = valueData
                        }
                        if (typeText == "Identification Type"){
                            docType = valueData
                        }
                        if (typeText == "Identification Number"){
                            docId = valueData
                        }
                    }
                }

            }

            crossBorderId = it.id
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
            systemId,

            docType, docId, crossBorderId,

            occupation, residenceCountry, originCountry, region, district
        )
    }

    data class PatientData(
        val name: String,
        val phone: String,
        val dob: String,
        val gender: String,
        val systemId: String?,

        val docType: String,
        val docId: String,
        val crossBorderId: String?,

        val occupation: String,
        val residenceCountry: String,
        val originCountry: String,
        val region: String,
        val district: String,


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

    fun getWorkflowData(workflowName: String, codeValue: String) = runBlocking { getWorkflow(workflowName, codeValue) }

    private suspend fun getWorkflow(workflowName: String, codeValue: String): ArrayList<DbObservation?>{

        val encounterList = ArrayList<DbObservation?>()
        fhirEngine
            .search<Encounter> {
                filter(Encounter.SUBJECT, { value = "Patient/$patientId" })
                sort(Observation.DATE, Order.ASCENDING)
            }
            .map { createWorkflowItem(it, workflowName, codeValue) }
            .let { encounterList.addAll(it) }

        return ArrayList(encounterList)
    }


    private suspend fun createWorkflowItem(it: Encounter, workflowName: String, codeValue: String):DbObservation? {

        val id = it.id.replace("Encounter/","")
        val type = it.type.firstOrNull()
        if (type != null) {
            if (type.hasText()){
                if (type.text == workflowName){
                    val observation = getObservationList(id, codeValue)
                    return observation.firstOrNull()
                }
            }
        }
        return null
    }

    suspend fun getObservationList(encounterId: String, codeValue: String): ArrayList<DbObservation>{

        val observationList = ArrayList<DbObservation>()
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
            .map { createObservationDataItem(it, encounterId) }
            .let { observationList.addAll(it) }

        return observationList
    }

    private fun createObservationDataItem(it: Observation, encounterId: String): DbObservation {

        var logicId = ""
        var text = ""
        var name = ""
        var date = ""

        if (it.hasId()){
            val id = it.id
            logicId = id
        }

        if (it.hasIssued()){
            val issued = it.issued
            date = issued.toString()
        }
        if (it.hasCode()){
            val code = it.code
            if (code.hasText()){
                text = code.text
            }
        }
        if (it.hasValueCodeableConcept()){
            val valueCodeableConcept = it.valueCodeableConcept
            val textValue = valueCodeableConcept.text
            if (valueCodeableConcept.hasCoding()){
                val coding = valueCodeableConcept.coding
            }
            name = textValue
        }
        if (it.hasValueStringType()){
            val textValue = it.valueStringType.value
            name = textValue
        }

        return DbObservation(
            logicId,
            encounterId,
            text,
            name,
            date
        )


    }

    fun generateObservations(encounterId: String)= runBlocking {
        generateObservationsByEncounter(encounterId)
    }


    private suspend fun generateObservationsByEncounter(encounterId: String): ArrayList<DbObservation> {

        val observationList = ArrayList<DbObservation>()
        fhirEngine
            .search<Observation> {
                filter(Observation.SUBJECT, { value = "Patient/$patientId" })
                filter(Observation.ENCOUNTER, { value = "Encounter/$encounterId" })
                sort(Observation.DATE, Order.ASCENDING)
            }
            .map { createObservationDataItem(it, encounterId) }
            .let { observationList.addAll(it) }

        return observationList


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
