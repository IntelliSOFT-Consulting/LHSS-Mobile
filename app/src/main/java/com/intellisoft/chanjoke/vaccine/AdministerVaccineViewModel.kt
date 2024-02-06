/*
 * Copyright 2022-2023 Google LLC
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

package com.intellisoft.chanjoke.vaccine

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.graphics.Paint.FontMetrics
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.DbCodeValue
import com.intellisoft.chanjoke.patient_list.PatientListViewModel
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.android.fhir.logicalId
import com.google.android.fhir.search.search
import com.intellisoft.chanjoke.fhir.data.DbAppointmentData
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.vaccine.validations.ImmunizationHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.util.UUID
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.AllergyIntolerance
import org.hl7.fhir.r4.model.Appointment
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Condition
import org.hl7.fhir.r4.model.Encounter
import org.hl7.fhir.r4.model.Immunization
import org.hl7.fhir.r4.model.Immunization.ImmunizationStatus
import org.hl7.fhir.r4.model.ImmunizationRecommendation
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.SimpleQuantity
import org.hl7.fhir.r4.model.StringType
import java.math.BigDecimal
import java.util.Date

/** ViewModel for patient registration screen {@link AddPatientFragment}. */
class AdministerVaccineViewModel(
    application: Application,
    private val state: SavedStateHandle
) :
    AndroidViewModel(application) {

    val questionnaire: String
        get() = getQuestionnaireJson()

    val isResourcesSaved = MutableLiveData<Boolean>()

    private val questionnaireResource: Questionnaire
        get() =
            FhirContext.forCached(FhirVersionEnum.R4).newJsonParser().parseResource(questionnaire)
                    as Questionnaire

    private var questionnaireJson: String? = null
    private var fhirEngine: FhirEngine = FhirApplication.fhirEngine(application.applicationContext)

    fun saveScreenerEncounter(questionnaireResponse: QuestionnaireResponse, patientId: String) {
        viewModelScope.launch {
            val bundle = ResourceMapper.extract(questionnaireResource, questionnaireResponse)
            val subjectReference = Reference("Patient/$patientId")
            val encounterId = generateUuid()
//      if (isRequiredFieldMissing(bundle)) {
//        isResourcesSaved.value = false
//        return@launch
//      }

            Log.e("-----", "hhhhhhhh")

            val context = FhirContext.forR4()
            val questionnaire =
                context.newJsonParser().encodeResourceToString(questionnaireResponse)

            println(questionnaire)

            saveResources(bundle, subjectReference, encounterId, patientId)

            isResourcesSaved.value = true
        }
    }

    private suspend fun saveResources(
        bundle: Bundle,
        subjectReference: Reference,
        encounterId: String,
        patientId: String,
    ) {

        val encounterReference = Reference("Encounter/$encounterId")
        bundle.entry.forEach {

            when (val resource = it.resource) {
                is Observation -> {
                    if (resource.hasCode()) {
                        val uuid = generateUuid()
                        resource.id = uuid
                        resource.subject = subjectReference
                        resource.encounter = encounterReference
                        resource.issued = Date()
                        saveResourceToDatabase(resource, "Obs " + uuid)
                    }
                }

                is Condition -> {
                    if (resource.hasCode()) {
                        val uuid = generateUuid()
                        resource.id = uuid
                        resource.subject = subjectReference
                        resource.encounter = encounterReference
                        saveResourceToDatabase(resource, "cond " + uuid)
                    }
                }

                is Encounter -> {
                    resource.subject = subjectReference
                    resource.id = encounterId
                    /**
                     * Check for AEFIs should be partOf
                     * */
                    if (FormatterClass().getSharedPref("vaccinationFlow",
                            getApplication<Application>().applicationContext)=="addAefi"){
                        val ref = FormatterClass().getSharedPref(
                            "encounter_logical_id",
                            getApplication<Application>().applicationContext
                        )
                        val parentReference = Reference("Encounter/$ref")
                        resource.partOf=parentReference

                        //Create Adverse effects
                        createAdverseEffects(encounterId, patientId)
                    }

                    saveResourceToDatabase(resource, "enc " + encounterId)

                    val vaccinationFlow = FormatterClass().getSharedPref("vaccinationFlow", getApplication<Application>().applicationContext)
                    if (
                        vaccinationFlow == "createVaccineDetails" ||
                        vaccinationFlow == "updateVaccineDetails" ||
                        vaccinationFlow == "recommendVaccineDetails"){
                        generateImmunizationRecord(encounterId, patientId)
                    }
                }
            }
        }
    }

    private suspend fun createAdverseEffects(encounterId: String, patientId: String) {

        val encounterReference = Reference("Encounter/$encounterId")
        val patientReference = Reference("Patient/$patientId")

        val allergyIntolerance = AllergyIntolerance()
        allergyIntolerance.id = generateUuid()
        allergyIntolerance.encounter = encounterReference
        allergyIntolerance.patient = patientReference

        /**
         * TODO: Add more details for the allergy intolerance
         */

        saveResourceToDatabase(allergyIntolerance, "intolerance ")


    }

    //Create an immunization resource
    private fun createImmunizationResource(
        encounterId: String,
        patientId: String,
        immunisationStatus: ImmunizationStatus,
        date: Date
    ): Immunization{

        val immunization = Immunization()
        val immunizationId = generateUuid()
        val encounterReference = Reference("Encounter/$encounterId")
        val patientReference = Reference("Patient/$patientId")

        immunization.encounter = encounterReference
        immunization.patient = patientReference
        immunization.id = immunizationId

        FormatterClass().saveSharedPref("immunizationId",immunizationId,getApplication<Application>().applicationContext)

        immunization.status = immunisationStatus

        //Date administered

        /**
         * TODO: Set to pick the saved data not the current date
         */

        immunization.occurrenceDateTimeType.value = date

        //Target Disease
        val targetDisease = FormatterClass().getSharedPref(
            "vaccinationTargetDisease",
            getApplication<Application>().applicationContext)
        val protocolList = Immunization().protocolApplied
        val immunizationProtocolAppliedComponent = Immunization.ImmunizationProtocolAppliedComponent()

        val diseaseTargetCodeableConceptList = immunizationProtocolAppliedComponent.targetDisease
        val diseaseTargetCodeableConcept = CodeableConcept()
        diseaseTargetCodeableConcept.text = targetDisease
        diseaseTargetCodeableConceptList.add(diseaseTargetCodeableConcept)
        immunizationProtocolAppliedComponent.targetDisease = diseaseTargetCodeableConceptList

        //Series - Name of vaccine series
        val vaccinationSeries = FormatterClass().getSharedPref(
            "vaccinationSeriesDoses",
            getApplication<Application>().applicationContext)
        if (vaccinationSeries != null) {
            immunizationProtocolAppliedComponent.series = vaccinationSeries
        }

        //Dose number - Recommended number of doses for immunity
        val vaccinationDoseNumber = FormatterClass().getSharedPref(
            "vaccinationDoseNumber",
            getApplication<Application>().applicationContext)
        if (vaccinationDoseNumber != null){
            val stringType = StringType()
            stringType.value = vaccinationDoseNumber
            immunizationProtocolAppliedComponent.seriesDoses = stringType
        }

        protocolList.add(immunizationProtocolAppliedComponent)

        immunization.protocolApplied = protocolList

        //Dose Quantity is the amount of vaccine administered
        val dosage = FormatterClass().getSharedPref("vaccinationDoseQuantity",
            getApplication<Application>().applicationContext)
        if (dosage != null){
            val nonDosage = FormatterClass().removeNonNumeric(dosage)
            val bigDecimalValue = BigDecimal(nonDosage)
            val simpleQuantity = SimpleQuantity()
            simpleQuantity.value = bigDecimalValue
            immunization.doseQuantity = simpleQuantity
        }

        //Administration Method
        val vaccinationAdministrationMethod = FormatterClass().getSharedPref(
            "vaccinationAdministrationMethod", getApplication<Application>().applicationContext)
        if (vaccinationAdministrationMethod != null) {
            val codeableConcept = CodeableConcept()
            codeableConcept.text = vaccinationAdministrationMethod
            codeableConcept.id = generateUuid()

            immunization.site = codeableConcept
        }

        //Batch number
        val vaccinationBatchNumber = FormatterClass().getSharedPref(
            "vaccinationBatchNumber", getApplication<Application>().applicationContext)
        if (vaccinationBatchNumber != null) {
            immunization.lotNumber = vaccinationBatchNumber
        }

        //Expiration date
        val vaccinationExpirationDate = FormatterClass().getSharedPref(
            "vaccinationExpirationDate", getApplication<Application>().applicationContext)
        if (vaccinationExpirationDate != null) {
            val dateExp = FormatterClass().convertStringToDate(
                vaccinationExpirationDate, "YYYY-MM-DD")
            if (dateExp != null) {
                immunization.expirationDate = dateExp
            }

        }
        //Vaccine code
        val administeredProduct = FormatterClass().getSharedPref(
            "administeredProduct", getApplication<Application>().applicationContext)
        //Get info on the vaccine
        if (administeredProduct != null){
            val vaccineCode = ImmunizationHandler().getVaccineDetailsByBasicVaccineName(administeredProduct)
            if (vaccineCode != null){
                val vaccineCodeConcept = CodeableConcept()
                vaccineCodeConcept.text = vaccineCode.vaccineName

                val codingList = ArrayList<Coding>()
                val vaccineCoding = Coding()
                vaccineCoding.code = vaccineCode.vaccineCode
                vaccineCoding.display = vaccineCode.vaccineName
                codingList.add(vaccineCoding)
                vaccineCodeConcept.coding = codingList

                immunization.vaccineCode = vaccineCodeConcept
            }
        }

        return immunization


    }

    fun createImmunizationRecommendation(context: Context){
        CoroutineScope(Dispatchers.IO).launch {
            val immunizationId = FormatterClass().getSharedPref("immunizationId", context)
            createNextImmunization(immunizationId)
        }
    }

    private suspend fun createNextImmunization(immunizationId: String?) {

        val formatterClass = FormatterClass()
        val date = Date()
        /**
         * TODO: Check if the Vaccine exists
         */

        //Vaccine code
        val administeredProduct = formatterClass.getSharedPref(
            "administeredProduct", getApplication<Application>().applicationContext)
        val patientId = formatterClass.getSharedPref(
            "patientId", getApplication<Application>().applicationContext)
        val currentDate = Date()

        /**
         * Get the current administered product and generate the next vaccine
         */
        if (administeredProduct != null && patientId != null) {
            val immunizationHandler = ImmunizationHandler()
            val vaccineBasicVaccine = ImmunizationHandler().getVaccineDetailsByBasicVaccineName(administeredProduct)

            val nextBasicVaccine = vaccineBasicVaccine?.let {
                immunizationHandler.getNextDoseDetails(
                    it
                )
            }

            val seriesVaccine = vaccineBasicVaccine?.let { immunizationHandler.getRoutineSeriesByBasicVaccine(it) }

            val targetDisease = seriesVaccine?.targetDisease
            val vaccineName = nextBasicVaccine?.vaccineName

            val job = Job()
            CoroutineScope(Dispatchers.IO + job).launch {
                //Save resources to Shared preference
                if (vaccineName != null && targetDisease != null) {
                    FormatterClass().saveStockValue(vaccineName, targetDisease, getApplication<Application>().applicationContext)
                }
            }.join()

            //Generate the next immunisation recommendation
            if (nextBasicVaccine != null){
                val administrativeWeeksSincePreviousList = nextBasicVaccine.administrativeWeeksSincePrevious
                val administrativeWeeksSinceDob = nextBasicVaccine.administrativeWeeksSinceDOB
                //Check if the above list is more than one.
                val nextImmunizationDate = if (administrativeWeeksSincePreviousList.isNotEmpty()){
                    //This is not the first vaccine, check on administrative weeks after birth
                    val weeksToAdd = administrativeWeeksSincePreviousList[0]
                    formatterClass.getNextDate(date, weeksToAdd)
                }else{
                    formatterClass.getNextDate(date, administrativeWeeksSinceDob.toDouble())
                }

                /**
                 * Check for the ones that have multiple dates
                 */
                val recommendation = createImmunizationRecommendationResource(
                    patientId,
                    nextImmunizationDate,
                    "Due",
                    "Next Immunization date",
                    immunizationId)
                saveResourceToDatabase(recommendation, "ImmRec")

            }


        }



    }

    //Create an immunizationRecommendation resource
    private fun createImmunizationRecommendationResource(
        patientId: String,
        recommendedDate: Date?,
        status:String,
        statusReason:String?,
        immunizationId:String?,
        ):ImmunizationRecommendation{

        val immunizationHandler = ImmunizationHandler()
        val immunizationRecommendation = ImmunizationRecommendation()
        val patientReference = Reference("Patient/$patientId")
        val id = generateUuid()
        immunizationRecommendation.patient = patientReference
        immunizationRecommendation.id = id
        immunizationRecommendation.date = Date()

        //Recommendation
        val recommendationList = ArrayList<ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent>()
        val immunizationRequest = ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent()

        //Target Disease
        val targetDisease = FormatterClass().getSharedPref(
            "vaccinationTargetDisease",
            getApplication<Application>().applicationContext)
        val codeableConceptTargetDisease = CodeableConcept()
        codeableConceptTargetDisease.text = targetDisease
        immunizationRequest.targetDisease = codeableConceptTargetDisease

        //Status
        val codeableConceptStatus = CodeableConcept()
        codeableConceptStatus.text = status
        immunizationRequest.forecastStatus = codeableConceptStatus

        //Status Reason
        if (statusReason != null){
            val codeableConceptStatusReason = CodeableConcept()
            codeableConceptStatusReason.text = statusReason
            val forecastList = ArrayList<CodeableConcept>()
            forecastList.add(codeableConceptStatusReason)

            immunizationRequest.forecastReason = forecastList
        }

        //Recommended date
        if (recommendedDate != null){
            val dateCriterionList = ArrayList<ImmunizationRecommendation.ImmunizationRecommendationRecommendationDateCriterionComponent>()
            val dateCriterion = ImmunizationRecommendation.ImmunizationRecommendationRecommendationDateCriterionComponent()

            val codeableConcept = CodeableConcept()
            codeableConcept.text = "Earliest date to give"

            dateCriterion.code = codeableConcept
            dateCriterion.value = recommendedDate

            dateCriterionList.add(dateCriterion)

            immunizationRequest.dateCriterion = dateCriterionList
        }

        //Dose Number
        val vaccinationDoseNumber = FormatterClass().getSharedPref(
            "vaccinationDoseNumber",
            getApplication<Application>().applicationContext)
        if (vaccinationDoseNumber != null){
            val doseNumberType = StringType()
            doseNumberType.value = vaccinationDoseNumber
            immunizationRequest.doseNumber = doseNumberType
        }

        //SeriesDoses
        val vaccinationSeriesDoses = FormatterClass().getSharedPref(
            "vaccinationSeriesDoses",
            getApplication<Application>().applicationContext)
        if (vaccinationSeriesDoses != null){
            val seriesDoseType = StringType()
            seriesDoseType.value = vaccinationSeriesDoses
            immunizationRequest.seriesDoses = seriesDoseType
        }

        //Supporting immunisation
        if (immunizationId != null){
            val immunizationReferenceList = ArrayList<Reference>()
            val immunizationReference = Reference()
            immunizationReference.reference = "Immunization/$immunizationId"
            immunizationReference.display = "Immunization"
            immunizationReferenceList.add(immunizationReference)
            immunizationRequest.supportingImmunization = immunizationReferenceList
        }

        if (status == "Contraindicated" || status == "Due"){
            //Administered vaccine
            val administeredProduct = FormatterClass().getSharedPref(
                "administeredProduct",
                getApplication<Application>().applicationContext)
            if (administeredProduct != null){
                val baseVaccineDetails = immunizationHandler.getVaccineDetailsByBasicVaccineName(administeredProduct)
                if (baseVaccineDetails != null){
                    val contraindicationCodeableConceptList = ArrayList<CodeableConcept>()
                    val codeableConceptContraindicatedVaccineCode = CodeableConcept()
                    codeableConceptContraindicatedVaccineCode.text = administeredProduct
                    contraindicationCodeableConceptList.add(codeableConceptContraindicatedVaccineCode)
                    immunizationRequest.contraindicatedVaccineCode = contraindicationCodeableConceptList
                }
            }


        }

        //Supporting Patient Information
        /**
         * TODO: Check on this
         */

        recommendationList.add(immunizationRequest)
        immunizationRecommendation.recommendation = recommendationList
        Log.e("----->","------6")
        return immunizationRecommendation
    }

    //Generate immunisation record
    suspend fun generateImmunizationRecord(
        encounterId: String,
        patientId: String){

        val fomatterClass = FormatterClass()
        val immunizationHandler = ImmunizationHandler()

        //Check if request is for creating immunisation
        val vaccinationFlow = FormatterClass().getSharedPref("vaccinationFlow",
            getApplication<Application>().applicationContext)
        if (vaccinationFlow == "createVaccineDetails"){
            //Request is to create immunisation record

            //Get the vaccines informations
            val vaccinationTargetDisease = fomatterClass.getSharedPref("vaccinationTargetDisease",
                getApplication<Application>().applicationContext)
            val administeredProduct = fomatterClass.getSharedPref("administeredProduct",
                getApplication<Application>().applicationContext)

            if (administeredProduct != null && vaccinationTargetDisease != null){

                val job = Job()
                CoroutineScope(Dispatchers.IO + job).launch {
                    //Save resources to Shared preference
                    FormatterClass().saveStockValue(administeredProduct, vaccinationTargetDisease, getApplication<Application>().applicationContext)
                }.join()

                //Check answer to If user wants to administer vaccine
                val status = observationFromCode(
                    "11-1122",
                    patientId,
                    encounterId)
                val value = status.value.replace(" ","")
                if (value == "Yes" || value == "No"){
                    /**
                     * User choose to administer vaccine, send immunisation status as Completed
                     * Generate immunization resource
                     * No need to create a Recommendation. It's only created for the next vaccine in the series
                     */
                    val date = Date()

                    var immunizationStatus = ImmunizationStatus.COMPLETED
                    if (value == "No") immunizationStatus = ImmunizationStatus.NOTDONE

                    val immunization = createImmunizationResource(encounterId,
                        patientId,
                        immunizationStatus,date)

                    saveResourceToDatabase(immunization, "Imm")

//                    //Generate the next immunization
//                    createNextImmunization(immunization)

                    if (value == "Yes"){
                        //Navigate to Stock Management
                        FormatterClass().saveSharedPref(
                            "isVaccineAdministered",
                            "stockManagement",
                            getApplication<Application>().applicationContext)
                    }



                }else{
                    /**
                     * User did not select Yes administer vaccine
                     * This could be contraindications
                     * No need to create an immunization
                     * Generate Recommendation
                     */

                    //If it was a contraindication

                    //Get Next Date
                    val dateTime = observationFromCode(
                        "833-23",
                        patientId,
                        encounterId)
                    val nextDateStr = dateTime.dateTime

                    if (nextDateStr != null){
                        val nextDate = FormatterClass().convertStringToDate(nextDateStr, "yyyy-MM-dd'T'HH:mm:ssXXX")

                        //Contraindication reasons
                        val statusReason = observationFromCode(
                            "321-12",
                            patientId,
                            encounterId)
                        val statusReasonStr = statusReason.value

                        val recommendation = createImmunizationRecommendationResource(patientId,
                            nextDate,
                            "Contraindicated",
                            statusReasonStr,
                            null)

                        saveResourceToDatabase(recommendation, "ImmRec")
                    }



                }
            }



        }else if (vaccinationFlow == "updateVaccineDetails"){
            /**
             * The request is from the update history -> vaccine details screens,
             * Get the type of vaccine from observation , save to shared pref and create immunisation
             */

            /**
             * TODO: Check on the new Vaccines
             */

            //Administered Product
            val vaccineType = observationFromCode(
                "222-11",
                patientId,
                encounterId)
            val administeredProduct = vaccineType.value.trim()

            //Target Disease
            val disease = observationFromCode(
                "882-22",
                patientId,
                encounterId)
            val targetDisease = disease.value.trim()

            val job = Job()
            CoroutineScope(Dispatchers.IO + job).launch {
                //Save resources to Shared preference
                FormatterClass().saveStockValue(administeredProduct, targetDisease, getApplication<Application>().applicationContext)
            }.join()

            //Date of last dose
            val lastDoseDate = observationFromCode(
                "111-8",
                patientId,
                encounterId)
            val lastDateDose = lastDoseDate.dateTime
            if(lastDateDose != null){
                val convertedDate = fomatterClass.convertDateFormat(lastDateDose)
                if (convertedDate != null){
                    val date = FormatterClass().convertStringToDate(convertedDate, "MMM d yyyy")

                    //Vaccine details have been saved
                    val immunization = date?.let {
                        createImmunizationResource(
                            encounterId,
                            patientId,
                            ImmunizationStatus.COMPLETED,
                            it
                        )
                    }
                    if (immunization != null) {
                        saveResourceToDatabase(immunization, "update")
                    }
                }


            }




            /**
             * TODO: Check if you should create recommendations from the historical data
             */

//            //Generate the next immunization
//            createNextImmunization(immunization)

        } else if (vaccinationFlow == "recommendVaccineDetails"){

//            /**
//             * TODO: UPDATE THIS FLOW TO USE THE NEW FLOW
//             */
//
//            val patientDetailsViewModel = PatientDetailsViewModel(getApplication(),fhirEngine, patientId)
//            val missingVaccineList = FormatterClass().getEligibleVaccines(getApplication<Application>().applicationContext, patientDetailsViewModel)
//            Log.e("***","*** missingVaccine 1 $missingVaccineList")
//
//            missingVaccineList.forEach {
//                FormatterClass().saveSharedPref("vaccinationTargetDisease", it, getApplication<Application>().applicationContext)
//                val vaccineDetails = VaccinationManager().getVaccineDetails(it)
//
//                if (vaccineDetails != null){
//                    FormatterClass().generateStockValue(vaccineDetails, getApplication<Application>().applicationContext)
//
//                    //Get weeks after Dob that we should create
//                    val dob = FormatterClass().getSharedPref("patientDob", getApplication<Application>().applicationContext)
//                    val weeksAfterDob = vaccineDetails.timeToAdminister
//                    val dobDate = FormatterClass().convertStringToDate(dob.toString(), "yyyy-MM-dd")
//
//                    val dobLocalDate = dobDate?.let { it1 ->
//                        FormatterClass().convertDateToLocalDate(
//                            it1
//                        )
//                    }
//
//                    val nextDateStr = dobLocalDate?.let { it1 ->
//                        FormatterClass().calculateDateAfterWeeksAsString(
//                            it1, weeksAfterDob)
//                    }
//
//                    val nextDate = FormatterClass().convertStringToDate(nextDateStr.toString(), "yyyy-MM-dd")
//
//
//                    //Vaccine details have been saved
//                    val recommendation = createImmunizationRecommendationResource(patientId,
//                        nextDate,
//                        "Due",
//                        null,
//                        null)
//                    saveResourceToDatabase(recommendation, "RecImm")
//
//                }
//            }


        }


    }



    private suspend fun observationFromCode(
        codeValue: String,
        patientId: String,
        encounterId: String
    ): DbCodeValue {

        val observations = mutableListOf<PatientListViewModel.ObservationItem>()
        fhirEngine
            .search<Observation> {
                filter(Observation.CODE, {
                    value = of(Coding().apply {
                        code = codeValue
                    })
                })
                filter(Observation.SUBJECT, { value = "Patient/$patientId" })
                filter(Observation.ENCOUNTER, { value = "Encounter/$encounterId" })
            }
            .take(1)
            .map { createObservationItem(it, getApplication<Application>().resources) }
            .let { observations.addAll(it) }

        //Return limited results
        var code = ""
        var value = ""
        var dateTime = ""
        observations.forEach {
            code = it.code
            value = it.value
            if (it.dateTime != null){
                dateTime = it.dateTime
            }
        }


        return DbCodeValue(code, value, dateTime)

    }

    fun createObservationItem(
        observation: Observation,
        resources: Resources
    ): PatientListViewModel.ObservationItem {


        // Show nothing if no values available for datetime and value quantity.
        var issuedDate = ""
        if (observation.hasValueDateTimeType()) {
            issuedDate = observation.valueDateTimeType.valueAsString
        }

        val id = observation.logicalId
        val text = observation.code.text ?: observation.code.codingFirstRep.display
        val code = observation.code.coding[0].code
        val value =
            if (observation.hasValueQuantity()) {
                observation.valueQuantity.value.toString()
            } else if (observation.hasValueCodeableConcept()) {
                observation.valueCodeableConcept.coding.firstOrNull()?.display ?: ""
            } else if (observation.hasValueStringType()) {
                observation.valueStringType.asStringValue().toString() ?: ""
            } else {
                ""
            }
        val valueUnit =
            if (observation.hasValueQuantity()) {
                observation.valueQuantity.unit ?: observation.valueQuantity.code
            } else {
                ""
            }
        val valueString = "$value $valueUnit"


        return PatientListViewModel.ObservationItem(
            id,
            code,
            text,
            valueString,
            issuedDate
        )
    }

    //Create an appointment
    fun createAppointment(dbAppointmentData: DbAppointmentData){
        CoroutineScope(Dispatchers.IO).launch { generateAppointment(dbAppointmentData) }

    }
    private suspend fun generateAppointment(dbAppointmentData: DbAppointmentData){

        val immunizationHandler = ImmunizationHandler()
        val formatterClass = FormatterClass()
        val patientId = formatterClass.getSharedPref("patientId",getApplication<Application>().applicationContext)

        val title = dbAppointmentData.title
        val description = dbAppointmentData.description
        val dateScheduled = dbAppointmentData.dateScheduled
        val vaccineName = dbAppointmentData.vaccineName

        val dobFormat = FormatterClass().convertDateFormat(dateScheduled)
        val selectedDate = if (dobFormat != null) {
            FormatterClass().convertStringToDate(dobFormat, "MMM d yyyy")
        }else{
            null
        }

        Log.e("----->","<------")
        println("dateScheduled $dateScheduled")
        println("dobFormat $dobFormat")
        println("selectedDate $selectedDate")
        Log.e("----->","<------")
        /**
         * TODO: Create a recommendation
         */

        var recommendationId = ""
        //1. Get the basic vaccine from the vaccineName

        if (vaccineName != null && vaccineName != ""){
            val basicVaccine = immunizationHandler.getVaccineDetailsByBasicVaccineName(vaccineName)
            if (basicVaccine != null){

                //This works for Routine and non-routine alone
                val seriesVaccine = immunizationHandler.getSeriesByBasicVaccine(basicVaccine)
                if (seriesVaccine != null && patientId != null && selectedDate != null){
                    val targetDisease = seriesVaccine.targetDisease
                    val administeredProduct = basicVaccine.vaccineName

                    val job = Job()
                    CoroutineScope(Dispatchers.IO + job).launch {
                        //Save resources to Shared preference
                        FormatterClass().saveStockValue(administeredProduct, targetDisease, getApplication<Application>().applicationContext)

                        val recommendation = createImmunizationRecommendationResource(
                            patientId,
                            selectedDate,
                            "Due",
                            "Next Immunization date",
                            null)

                        recommendationId = recommendation.id
                        saveResourceToDatabase(recommendation, "ImmRec")
                    }.join()

                }


            }
        }

        val patientReference = Reference("Patient/$patientId")

        val appointment = Appointment()

        val id = generateUuid()
        appointment.id = id

        //Status
        appointment.setStatus(Appointment.AppointmentStatus.BOOKED)

        //description
        val newText = "Title: $title Description:$description"
        appointment.description = newText

        //List of based on references
        if (recommendationId != ""){
            val recommendationReference = Reference("ImmunizationRecommendation/$recommendationId")
            val referenceList = ArrayList<Reference>()
            referenceList.add(recommendationReference)
            appointment.basedOn = referenceList
        }

        /**
         * TODO: Change the patient resource from this
         */
        val supportingInfoList = ArrayList<Reference>()
        supportingInfoList.add(patientReference)
        appointment.supportingInformation = supportingInfoList

        //Created
        appointment.created = Date()

        //start and end
        if (selectedDate != null){
            appointment.start = selectedDate
            saveResourceToDatabase(appointment, "appointment")
        }

        /**
         * TODO: Add Participants
         */



    }


    private suspend fun saveResourceToDatabase(resource: Resource, type: String) {

        Log.e("----", "----$type")
        fhirEngine.create(resource)

    }


    //  private fun isRequiredFieldMissing(bundle: Bundle): Boolean {
//    bundle.entry.forEach {
//      val resource = it.resource
//      when (resource) {
//        is Observation -> {
//          if (resource.hasValueQuantity() && !resource.valueQuantity.hasValueElement()) {
//            return true
//          }
//        }
//        // TODO check other resources inputs
//      }
//    }
//    return false
//  }
    private fun getQuestionnaireJson(): String {
        questionnaireJson?.let {
            return it!!
        }
        questionnaireJson =
            readFileFromAssets(state[AdministerVaccineFragment.QUESTIONNAIRE_FILE_PATH_KEY]!!)
        return questionnaireJson!!
    }

    private fun readFileFromAssets(filename: String): String {
        return getApplication<Application>().assets.open(filename).bufferedReader().use {
            it.readText()
        }
    }

    private fun generateUuid(): String {
        return UUID.randomUUID().toString()
    }
}
