package com.intellisoft.lhss.screening

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.android.fhir.logicalId
import com.google.android.fhir.search.Order
import com.google.android.fhir.search.search
import com.intellisoft.lhss.fhir.FhirApplication
import com.intellisoft.lhss.fhir.data.DbObservation
import com.intellisoft.lhss.fhir.data.FormatterClass
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.CarePlan
import org.hl7.fhir.r4.model.Encounter
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.Resource
import java.util.UUID

class ScreeningViewModel(application: Application, private val state: SavedStateHandle)
    :AndroidViewModel(application) {

    val formatterClass = FormatterClass()
    val questionnaire: String
        get() = getQuestionnaireJson()

    val isResourcesSaved = MutableLiveData<Boolean>()

    private val questionnaireResource: Questionnaire
        get() =
            FhirContext.forCached(FhirVersionEnum.R4).newJsonParser().parseResource(questionnaire)
                    as Questionnaire

    private var questionnaireJson: String? = null
    private var fhirEngine: FhirEngine = FhirApplication.fhirEngine(application.applicationContext)

    private fun getQuestionnaireJson(): String {
        questionnaireJson?.let {
            return it!!
        }
        val questionnaire = formatterClass.getSharedPref("questionnaire", getApplication<Application>().applicationContext)
        questionnaireJson = readFileFromAssets(questionnaire.toString())
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
    fun saveScreenerEncounter(questionnaireResponse: QuestionnaireResponse, patientId: String) {
        viewModelScope.launch {
            val bundle = ResourceMapper.extract(questionnaireResource, questionnaireResponse)
            val subjectReference = Reference("Patient/$patientId")

            var encounterId = FormatterClass().getSharedPref("encounterId", getApplication<Application>().applicationContext)
            if (encounterId == null){
                encounterId = generateUuid()
            }

//            if (isRequiredFieldMissing(bundle)) {
//                isResourcesSaved.value = false
//                return@launch
//            }
            saveResources(bundle, subjectReference, encounterId)
            isResourcesSaved.value = true
        }
    }
    private suspend fun saveResources(
        bundle: Bundle,
        subjectReference: Reference,
        encounterId: String,
    ) {
        val encounterReference = Reference("Encounter/$encounterId")
        bundle.entry.forEach {
            when (val resource = it.resource) {
                is Observation -> {
                    if (resource.hasCode()) {
                        resource.id = generateUuid()
                        resource.subject = subjectReference
                        resource.encounter = encounterReference
                        saveResourceToDatabase(resource, "obs")
                    }
                }
                is Encounter -> {
                    resource.subject = subjectReference
                    resource.id = encounterId
                    saveResourceToDatabase(resource, "enc")
                }
                is CarePlan -> {
                    resource.id = generateUuid()
                    resource.subject = subjectReference
                    resource.encounter = encounterReference
                    saveResourceToDatabase(resource, "care")
                }
            }
        }
    }
    private suspend fun saveResourceToDatabase(resource: Resource, s: String) {

        fhirEngine.create(resource)
    }

    fun getCarePlan(patientId: String, encounterId: String) = runBlocking {
        getCarePlanDetails(patientId, encounterId)
    }

    private suspend fun getCarePlanDetails(patientId: String, encounterId: String):ArrayList<DbObservation> {

        var encounterList = ArrayList<DbObservation>()
        val carePlanList = ArrayList<DbObservation>()
        fhirEngine
            .search<CarePlan> {
                filter(CarePlan.SUBJECT, { value = "Patient/$patientId" })
                filter(CarePlan.ENCOUNTER, { value = "Encounter/$encounterId" })
                sort(CarePlan.DATE, Order.DESCENDING)
            }
            .map { createCarePlan(it) }
            .let { carePlanList.addAll(it) }

        println("-------")
        println("$carePlanList")
        println("$patientId")
        println("$encounterId")
        println("-------")

        if (carePlanList.isEmpty()){
            return encounterList
        }

//        encounterList = getObservationsDetails(patientId, encounterId)
        return encounterList
    }



    fun getEncounterList(patientId: String)= runBlocking{
        getEncounterDetails(patientId)
    }
    private suspend fun getEncounterDetails(patientId: String):ArrayList<DbObservation>{

        val encounterList = ArrayList<DbObservation>()

        fhirEngine
            .search<Encounter> {
                filter(Encounter.SUBJECT, { value = "Patient/$patientId" })
                sort(Encounter.DATE, Order.DESCENDING)
            }
            .map { createEncounter(it) }
            .let { encounterList.addAll(it) }


        return encounterList
    }
    private fun createEncounter(it: Encounter):DbObservation {

        val id = it.logicalId
        return DbObservation(
            id.toString(),
            "Encounter "
        )

    }
    private fun createCarePlan(it: CarePlan):DbObservation {

        val id = it.logicalId
        return DbObservation(
            id.toString(),
            "Encounter "
        )

    }

    fun getObservations(patientId: String,encounterId: String) = runBlocking {
        getObservationsDetails(patientId, encounterId)
    }
    private suspend fun getObservationsDetails(patientId: String, encounterId: String):ArrayList<DbObservation>{
        val observationList = ArrayList<DbObservation>()

        fhirEngine
            .search<Observation> {
                filter(Observation.SUBJECT, { value = "Patient/$patientId" })
                filter(Observation.ENCOUNTER, { value = "Encounter/$encounterId" })
                sort(Observation.DATE, Order.DESCENDING)
            }
            .map { createObservationItem(it) }
            .let { observationList.addAll(it) }


        return observationList
    }

    private fun createObservationItem(observation: Observation): DbObservation {

        val id = observation.logicalId
        val text = observation.code.text ?: observation.code.codingFirstRep.display
        val value =
            if (observation.hasValueQuantity()) {
                observation.valueQuantity.value.toString()
            } else if (observation.hasValueCodeableConcept()) {
                observation.valueCodeableConcept.coding.firstOrNull()?.display ?: ""
            }else if (observation.hasValueStringType()) {
                observation.valueStringType.asStringValue().toString() ?: ""
            }else {
                ""
            }
        val valueUnit =
            if (observation.hasValueQuantity()) {
                observation.valueQuantity.unit ?: observation.valueQuantity.code
            } else {
                ""
            }
        val valueString = "$value $valueUnit"

        return DbObservation(
            text,
            valueString)
    }


}