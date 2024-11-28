package com.intellisoft.lhss.refer_patient.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.android.fhir.FhirEngine
import com.intellisoft.lhss.clinical_info.viewmodel.ClinicalInfoDetailsViewModel
import com.intellisoft.lhss.fhir.Constants
import com.intellisoft.lhss.fhir.FhirApplication
import com.intellisoft.lhss.shared.DbCommunication
import com.intellisoft.lhss.shared.DbFormData
import com.intellisoft.lhss.shared.DbNavigationDetails
import com.intellisoft.lhss.shared.FormData
import com.intellisoft.lhss.shared.FormatterClass
import com.intellisoft.lhss.shared.NotificationServiceViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.CarePlan
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Communication
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.Encounter
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.Period
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.ServiceRequest
import org.hl7.fhir.r4.model.StringType
import java.util.Date

class ReviewReferViewModel (
    application: Application,
    ): AndroidViewModel(application) {
    // TODO: Implement the ViewModel

        private var formatterClass = FormatterClass(application.applicationContext)

    private var fhirEngine: FhirEngine =
        FhirApplication.fhirEngine(application.applicationContext)

    private var viewModel =  NotificationServiceViewModel(application)



    fun createServiceRequest(formDataList: List<FormData>,
                             patientId: String,
                             requesterId: String?)= runBlocking {
        generateServiceRequest(formDataList,patientId, requesterId )
    }

    fun getReasonForReferral(formDataList: List<FormData>): String? {
        // Find the FormData with the title "REFERRAL_INFO"
        val referralInfo = formDataList.find { it.title == "REFERRAL_INFO" }

        // If found, find the DbFormData with the tag "Reason for Referral"
        val reasonForReferral = referralInfo?.formDataList?.find { it.tag == "Reason for Referral" }

        // Return the text (i.e., the value) of the "Reason for Referral", or null if not found
        return reasonForReferral?.text
    }

    private suspend fun generateServiceRequest(
        formDataList: List<FormData>,
        patientId: String,
        requesterId: String?):List<String> {

        // Create a ServiceRequest
        val serviceRequest = ServiceRequest()
        serviceRequest.id = formatterClass.generateUuid()  // Set your unique ID
        serviceRequest.status = ServiceRequest.ServiceRequestStatus.ACTIVE
        serviceRequest.subject = Reference("Patient/$patientId")
        if (requesterId != null){
            serviceRequest.requester = Reference("Practitioner/$requesterId")
        }
        serviceRequest.authoredOn = Date()


        val reasonCodeList = ArrayList<CodeableConcept>()

        val referralReason = getReasonForReferral(formDataList)
        if (referralReason != null){
            val codeableConcept = CodeableConcept()
            codeableConcept.text = "REASON_FOR_REFERRAL"

            val codingList = ArrayList<Coding>()

            val coding = Coding()
            coding.id = formatterClass.generateUuid()
            coding.display = referralReason
            coding.code = generateRandomLoincCode()
            codingList.add(coding)

            codeableConcept.coding = codingList
            codeableConcept.id = formatterClass.generateUuid()

            reasonCodeList.add(codeableConcept)
        }

        val codeableConcept = CodeableConcept()
        codeableConcept.text = "REFERRAL_MODULE"
        codeableConcept.id = formatterClass.generateUuid()
        reasonCodeList.add(codeableConcept)


        serviceRequest.reasonCode = reasonCodeList

        serviceRequest.occurrence = DateTimeType.now()
        serviceRequest.supportingInfo = ArrayList()

        // Iterate through formDataList and create Encounters and Observations
        formDataList.forEach { formData ->
            // Create an Encounter based on the title
            val encounter = createEncounter(
                formData.title,
                patientId,
                serviceRequest.id,
                null,
                Encounter.EncounterStatus.FINISHED)

            serviceRequest.supportingInfo.add(Reference("Encounter/${encounter.id}"))
            saveResourceToDatabase(encounter, "Encounter")

            // Create Observations from formDataList and link them to the Encounter
            formData.formDataList.forEach { dbFormData ->
                val observation = createObservation(
                    dbFormData, patientId, encounter.id)
                saveResourceToDatabase(observation, "Observation")
            }
            // Add Observation to supportingInfo of ServiceRequest
        }

        return saveResourceToDatabase(serviceRequest, "ServiceRequest")
//        serviceRequest.note = listOf(Annotation().setText("ServiceRequest for referral created"))
    }

    private suspend fun saveResourceToDatabase(resource: Resource, type: String) :List<String>{
        Log.e("----", "----$type")
        return fhirEngine.create(resource)
    }

    fun createClinicalInfo(
        formDataList: ArrayList<FormData>,
        workflowTitles: String,
        clinicalViewModel: ClinicalInfoDetailsViewModel,
        status: CarePlan.CarePlanStatus
    ){
        CoroutineScope(Dispatchers.IO).launch {
            createClinicalInfoBac(formDataList, workflowTitles, clinicalViewModel, status)
        }
    }

    private suspend fun createClinicalInfoBac(
        formDataList: ArrayList<FormData>,
        workflowTitles: String,
        clinicalViewModel: ClinicalInfoDetailsViewModel,
        status: CarePlan.CarePlanStatus
    ) {

        val patientId = formatterClass.getSharedPref(
            "",
            "patientId")
        val patientName = formatterClass.getSharedPref(
            "",
            "patientName")

        val carePlanId = formatterClass.getSharedPref(
            DbNavigationDetails.CARE_PLAN.name,
            "carePlanId")

        if (patientId != null && carePlanId != null) {

            val supportingInfoList = ArrayList<Reference>()

            // Iterate through formDataList and create Encounters and Observations
            formDataList.forEach { formData ->
                // Create an Encounter based on the title
                val encounter = createEncounter(
                    workflowTitles,
                    patientId,
                    null,
                    carePlanId,
                    Encounter.EncounterStatus.PLANNED
                )

                saveResourceToDatabase(encounter, "Encounter")

                // Create Observations from formDataList and link them to the Encounter
                formData.formDataList.forEach { dbFormData ->
                    val observation = createObservation(
                        dbFormData, patientId, encounter.id)
                    saveResourceToDatabase(observation, "Observation")
                }
                val encounterReference = Reference("Encounter/${encounter.id}")
                supportingInfoList.add(encounterReference)

            }

            //Update the CarePlan status
            clinicalViewModel.updateCarePlanStatus(
                status,
                carePlanId,
                supportingInfoList
            )

            // Create a notification
            var title = ""
            var content = ""
            var navigationId = 0

            title = convertToTitleCase(workflowTitles)
            "A new $title Form has been submitted for patient $patientName".also { content = it }

            val basedOnReference = Reference("CarePlan/$carePlanId")
            val subjectReference = Reference("Patient/$patientId")

            /**
             * TODO: Update the subject reference to the CarePlan in the notification
             */

            val dbCommunication = DbCommunication(
                Communication.CommunicationStatus.INPROGRESS,
                subjectReference,
                subjectReference,
                subjectReference,
                supportingInfoList,
                title,
                content,
            )
            viewModel.createNotification(dbCommunication)

        }


    }
    private fun convertToTitleCase(input: String): String {
        return input.split('_').joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }  // Join the words back together with spaces
    }

    private suspend fun updateResourceToDatabase(resource: Resource, s: String) {
        Log.e("----Upd", "----$s")
        fhirEngine.update(resource)
    }


    // Function to create an Encounter
    private fun createEncounter(
        title: String,
        patientId: String,
        serviceRequestId: String?,
        carePlanId: String?,
        status: Encounter.EncounterStatus,
    ): Encounter {
        val encounter = Encounter()
        encounter.id =  formatterClass.generateUuid()  // Use timestamp as a unique ID
        encounter.subject = Reference("Patient/$patientId")
        encounter.reasonCode = listOf(CodeableConcept().setText(title))

        encounter.period = Period().setStart(Date())

        encounter.status = status

        val basedOnList = ArrayList<Reference>()
        if (serviceRequestId != null) {
            basedOnList.add(Reference("ServiceRequest/$serviceRequestId"))
        }
        if (carePlanId!= null) {
            basedOnList.add(Reference("CarePlan/$carePlanId"))
        }
        encounter.basedOn = basedOnList


        return encounter
    }

    // Function to create an Observation
    private fun createObservation(
        dbFormData: DbFormData,
        patientId: String,
        encounterId: String): Observation {

        val observation = Observation()
        observation.id = formatterClass.generateUuid() // Use timestamp as a unique ID
        observation.status = Observation.ObservationStatus.FINAL
        observation.subject = Reference("Patient/$patientId")


        val fhirCode = dbFormData.fhirCode
            ?: when (dbFormData.tag) {
                "TB Registration Number" -> { Constants.TB_YOUR_REGISTRATION_CODE }
                "Our TB Registration No" -> { Constants.TB_OUR_REGISTRATION_CODE }
                "Name of Receiving facility" -> { Constants.RECEIVING_FACILITY_NAME }
                "Date of Referral" -> { Constants.REFERRAL_DATE }
                else -> { generateRandomLoincCode() }
            }


//        val fhirCode = dbFormData.fhirCode ?: generateRandomLoincCode()


        // Generate a random LOINC code for the observation
        observation.code = CodeableConcept()
            .addCoding(
                Coding().setSystem("http://loinc.org")
                    .setCode(fhirCode)
                    .setDisplay(dbFormData.tag)
            )

        // Set the encounter for the observation
        observation.encounter = Reference("Encounter/$encounterId")

        //set the value
        val type = StringType()
        type.id = formatterClass.generateUuid()
        type.value = dbFormData.text

        observation.value = type

        // Add the observation note from the form data
        val noteList = ArrayList<org.hl7.fhir.r4.model.Annotation>()
        val annotation = org.hl7.fhir.r4.model.Annotation()
        annotation.setText(dbFormData.tag)
        annotation.id = formatterClass.generateUuid()
        noteList.add(annotation)

        observation.note = noteList

        return observation
    }

    // Function to generate a random LOINC code (for example purposes)
    private fun generateRandomLoincCode(): String {
        val random = (10000..99999).random()
        return "LOINC-$random"
    }

}