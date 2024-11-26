package com.intellisoft.lhss.shared

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.search.Order
import com.google.android.fhir.search.search
import com.intellisoft.lhss.fhir.Constants
import com.intellisoft.lhss.fhir.FhirApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.CarePlan
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Communication
import org.hl7.fhir.r4.model.Encounter
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.Resource
import java.util.Date

class NotificationServiceViewModel(
    application: Application,
): AndroidViewModel(application) {

    private val formatterClass = FormatterClass(application.applicationContext)

    private var fhirEngine: FhirEngine =
        FhirApplication.fhirEngine(application.applicationContext)


    fun updateCommunicationStatus(communicationId: String) {

        CoroutineScope(Dispatchers.IO).launch {
            val communicationDataId = communicationId.replace("Communication/","")
            val searchResult =
                fhirEngine.search<Communication> {
                    filter(Resource.RES_ID, { value = of(communicationDataId) })
                }

            if (searchResult.isNotEmpty()) {
                searchResult.first().let {
                    it.resource.status = Communication.CommunicationStatus.COMPLETED
                    fhirEngine.update(it.resource)
                }
            }
        }

    }

    fun createNotification(dbCommunication: DbCommunication) {

        CoroutineScope(Dispatchers.IO).launch {

            //Build a communication
            val communication = Communication()

            val topicCodeableConcept = CodeableConcept()
            topicCodeableConcept.text = dbCommunication.title
            topicCodeableConcept.id = formatterClass.generateUuid()
            topicCodeableConcept.coding = arrayListOf(
                Coding(
                    Constants.SYSTEM_TB_REGISTRATION,
                    Constants.TOPIC_NUMBER,
                    dbCommunication.content
                )
            )

            communication.id = formatterClass.generateUuid()
            communication.subject = dbCommunication.subject
            communication.recipient = arrayListOf(dbCommunication.recipient)
            communication.sender = dbCommunication.sender
            communication.status = dbCommunication.status
            communication.priority = Communication.CommunicationPriority.ROUTINE
            communication.basedOn = dbCommunication.basedOn
            communication.sent = Date()
            communication.topic = topicCodeableConcept

            fhirEngine.create(communication)

        }

    }
    fun getCommunicationList() = runBlocking {
        getCommunication()
    }
    private suspend fun getCommunication(): ArrayList<DbCommunicationData>{

        val communicationList = ArrayList<DbCommunicationData>()

        fhirEngine
            .search<Communication> {
                sort(Encounter.DATE, Order.ASCENDING)
            }
            .map { createCommunicationItem(it.resource) }
            .let {communicationList.addAll(it)}

        val notificationSize = communicationList.size

        formatterClass.saveSharedPref(
            "",
            "notificationSize",
            notificationSize.toString())

        //Sort the list based on the date such that the latest notification is at the bottom
        communicationList.sortBy { it.dateTime }


        return ArrayList(communicationList)
    }
    private fun createCommunicationItem(resource: Communication): DbCommunicationData {

        var patientId: String = ""
        var navigationId: Int = 0
        var title: String = ""
        var content: String = ""
        var dateTime: String = ""
        var status: String = ""
        var basedOnList = ArrayList<String>()

        val id = resource.id

        //1. This is the Navigation Id
        if (resource.hasReasonCode()){
            if (resource.hasText()){
                val text = resource.reasonCode[0].text
                if (text == "ACTION"){
                    if (resource.reasonCode[0].hasCoding()){
                        if (resource.reasonCode[0].coding[0].hasDisplay()){
                            val reasonCode = resource.reasonCode[0].coding[0].display
                            navigationId = reasonCode.toInt()
                        }
                    }
                }
            }
        }

        //2. This is the Patient Id
        if (resource.hasSubject()){
            val subjectReference = resource.subject.reference
            patientId = subjectReference
        }

        //3. This is the Notification content
        if (resource.hasTopic()){
            if (resource.hasTopic()){
                if (resource.topic.hasText()){
                    //This is the Notification title
                    val text = resource.topic.text
                    title = text

                }
                if (resource.topic.hasCoding()){
                    //This is the Notification content
                    val coding = resource.topic.coding[0].display
                    content = coding
                }
            }
        }

        //4. This is the Date of the Notification
        if (resource.hasSent()){
            val sent = resource.sent
            dateTime = sent.toString()
        }

        //5. This is the Notification status
        if (resource.hasStatus()){
            status = resource.status.toString()
        }

//        val dateCreated = formatterClass.convertDateFormat(dateTime) ?: ""

        //6. This is the Notification basedOn
        if (resource.hasBasedOn()){
            val basedOnReference = resource.basedOn
            basedOnReference.forEach {
                basedOnList.add(it.reference)
            }
        }

//        val annotationList = ArrayList<DbAnnotation>()



        return DbCommunicationData(
            id,
            patientId,
            navigationId,
            title,
            content,
            dateTime,
            status,
            basedOnList
        )

    }
    fun getNotificationDataList(resourceId: String) = runBlocking {
        getNotificationList(resourceId)
    }
    private suspend fun getNotificationList(resourceId: String): FormData {

        var titleInfo = ""
        val formList = ArrayList<DbFormData>()

        val (resourceType, id) = extractParts(resourceId)

        if (resourceType == "CarePlan" || resourceType == "Careplan") {
            val searchResult =
                fhirEngine.search<CarePlan> {
                    filter(Resource.RES_ID, { value = of(id) })
                }

            if (searchResult.isNotEmpty()){
                searchResult.first().let {

                    val carePlan = it.resource
                    if (carePlan.hasSupportingInfo()){
                        carePlan.supportingInfo.forEach {

                            val encounterReferenceList = carePlan.supportingInfo

                            encounterReferenceList.forEach { encounterReference ->
                                val formData = getEncounterDetails(encounterReference.reference)
                                if (formData!= null){
                                    titleInfo = formData.title
                                    val formDataList = formData.formDataList

                                    formList.addAll(formDataList)

                                }
                            }
                        }
                    }
                }
            }
        }
        if (resourceType == "Encounter"){
            val formData = getEncounterDetails(resourceId)
            if (formData!= null){
                titleInfo = formData.title
                formList.addAll(formData.formDataList)
            }
        }
        val titleDataInfo = formatterClass.toSentenceCase(titleInfo)

        return FormData(titleDataInfo, formList)
    }

    private suspend fun getEncounterDetails(encounter:String): FormData?{

        val observationList = ArrayList<DbFormData>()
        var title = ""
        val encounterId = encounter.replace("Encounter/","")

        fhirEngine
            .search<Observation> {
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
    private fun createObservationItem(resource: Observation): DbFormData {

        val tag = if(resource.hasCode() && resource.code.hasCoding()){
            resource.code.codingFirstRep.display
        }else ""

        val text = if (resource.hasValueStringType()){
            resource.valueStringType.valueAsString
        }else ""

        return DbFormData(
            tag, text
        )

    }
    private fun extractParts(input: String): Pair<String, String> {
        val parts = input.split('/') // Split the string at the "/"
        return Pair(parts[0], parts[1]) // Return the two parts as a pair
    }

}