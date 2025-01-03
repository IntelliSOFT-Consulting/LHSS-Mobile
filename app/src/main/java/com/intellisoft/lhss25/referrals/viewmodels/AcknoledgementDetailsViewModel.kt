package com.intellisoft.lhss25.referrals.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.search.search
import com.intellisoft.lhss25.fhir.FhirApplication
import com.intellisoft.lhss25.shared.FormData
import com.intellisoft.lhss25.shared.FormatterClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Attachment
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.DocumentReference
import org.hl7.fhir.r4.model.Enumerations
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.ServiceRequest

class AcknoledgementDetailsViewModel(
    application: Application,
    private val patientId: String,
    private val serviceRequestId: String,
) : AndroidViewModel(application) {

    private var formatterClass = FormatterClass(application.applicationContext)

    private var fhirEngine: FhirEngine =
        FhirApplication.fhirEngine(application.applicationContext)

    fun createAcknowledgementDocumentResource(formDataList: ArrayList<FormData>){
        CoroutineScope(Dispatchers.IO).launch {
            createAcknowledgementDocumentResourceBac(formDataList)
        }
    }

    private suspend  fun createAcknowledgementDocumentResourceBac(formDataList: List<FormData>) {
        val documentReference = DocumentReference()

        // Set the required fields for the DocumentReference
        documentReference.id = formatterClass.generateUuid()
        documentReference.status = Enumerations
            .DocumentReferenceStatus.CURRENT

        // Set document type
        val typeCodeableConcept = CodeableConcept()
        typeCodeableConcept.text = "Acknowledgement Form"
        documentReference.type = typeCodeableConcept

        // Add subject (patient reference)
        documentReference.subject = Reference("Patient/$patientId")

        // Add description
        documentReference.description = "Acknowledgement form for patient: $patientId"

        formDataList.forEach { it ->
            val formDataList1 = it.formDataList
            // Create an attachment to hold form data
            val attachment = Attachment()

            val formDataText = formDataList1
                .joinToString("\n") { "${it.tag}: ${it.text}" }
            attachment.contentType = "text/plain"
            attachment.data = formDataText.toByteArray()

            // Add the attachment to the document
            documentReference.content.add(
                DocumentReference.DocumentReferenceContentComponent(attachment))
        }

        val relatedList = ArrayList<Reference>()

        val documentReferenceContextComponent =
            DocumentReference.DocumentReferenceContextComponent()
        relatedList.add(Reference("ServiceRequest/$serviceRequestId"))

        documentReferenceContextComponent.related = relatedList

        documentReference.context = documentReferenceContextComponent

        // Add optional metadata, like author or date, if available
        documentReference.date = java.util.Date()

        saveResourceToDatabase(documentReference, "DocumentReference")

        /**
         * Update the service
         */
        val searchResult =
            fhirEngine.search<ServiceRequest> {
                filter(Resource.RES_ID, { value = of(serviceRequestId) })
            }
        var serviceRequest: ServiceRequest

        if (searchResult.isNotEmpty()) {
            searchResult.first().let {
                serviceRequest = it.resource
                serviceRequest.setStatus(ServiceRequest.ServiceRequestStatus.COMPLETED)
                updateResourceToDatabase(serviceRequest, "update service request")

            }
        }

    }

    private suspend fun updateResourceToDatabase(resource: Resource, type: String){
        Log.e("----", "----$type")
        fhirEngine.update(resource)
    }
    private suspend fun saveResourceToDatabase(resource: Resource, type: String) :List<String>{
        Log.e("----", "----$type")
        return fhirEngine.create(resource)
    }

    class AcknoledgementDetailsViewModelFactory(
        private val application: Application,
        private val patientId: String,
        private val serviceRequestId: String,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AcknoledgementDetailsViewModel::class.java)) {
                return AcknoledgementDetailsViewModel(application, patientId, serviceRequestId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}