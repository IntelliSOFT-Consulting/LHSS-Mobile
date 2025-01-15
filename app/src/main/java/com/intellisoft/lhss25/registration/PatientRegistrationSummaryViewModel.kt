package com.intellisoft.lhss25.registration

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import com.google.android.fhir.FhirEngine
import com.intellisoft.lhss25.fhir.FhirApplication
import com.intellisoft.lhss25.shared.DbClasses
import com.intellisoft.lhss25.shared.DbFormData
import com.intellisoft.lhss25.shared.FormData
import com.intellisoft.lhss25.shared.FormatterClass
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Address
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.ContactPoint
import org.hl7.fhir.r4.model.Enumerations
import org.hl7.fhir.r4.model.HumanName
import org.hl7.fhir.r4.model.Identifier
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Resource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PatientRegistrationSummaryViewModel(
    application: Application,
    private val state: SavedStateHandle
) : AndroidViewModel(application) {
    // TODO: Implement the ViewModel

    private var fhirEngine: FhirEngine =
        FhirApplication.fhirEngine(application.applicationContext)

    data class DocumentDetails(
        val documentType: String,
        val documentName: String
    )

    // Function to create FHIR Patient resource from FormDataList
    private suspend fun createPatientResourceBac(formDataList: ArrayList<FormData>):List<String> {
        val patient = Patient()
        val formatterClass = FormatterClass(getApplication<Application>().applicationContext)

        val identifier: MutableList<Identifier> = mutableListOf()

        val documentList = ArrayList<DocumentDetails>()

        formDataList.forEach { formData ->
            when (formData.title) {
                DbClasses.DEMOGRAPHICS.name -> {
                    val humanNameList = ArrayList<HumanName>()
                    formData.formDataList.forEach { dbFormData ->
                        when (dbFormData.tag) {
                            "First Name", "Middle Name", "Last Name", "NickName" -> {
                                val humanName = addNameToPatient(dbFormData)
                                humanNameList.add(humanName)
                            }
                            "Sex" -> {
                                patient.gender = mapGender(dbFormData.text)
                            }
                            "DOB" -> {
                                patient.birthDate = parseDate(dbFormData.text)
                            }
                            "Telephone in referring country" -> {
                                val contactPoint = ContactPoint()
                                contactPoint.id = formatterClass.generateUuid()
                                contactPoint.rank = 1
                                contactPoint.value = dbFormData.text
                                patient.addTelecom(contactPoint)
                            }
                            "Telephone in receiving country" -> {
                                val contactPoint = ContactPoint()
                                contactPoint.id = formatterClass.generateUuid()
                                contactPoint.rank = 2
                                contactPoint.value = dbFormData.text
                                patient.addTelecom(contactPoint)

                            }
                            "Telephone" -> {
                                val contactPoint = ContactPoint()
                                contactPoint.id = formatterClass.generateUuid()
                                contactPoint.rank = 3
                                contactPoint.value = dbFormData.text
                                patient.addTelecom(contactPoint)
                            }
                            "Document Type","Document Number" -> {
                                var documentType = ""
                                var documentNumber = ""

                                if (dbFormData.tag == "Document Type") {
                                    documentType = dbFormData.text
                                }
                                if (dbFormData.tag == "Document Number") {
                                    documentNumber = dbFormData.text
                                }
                                documentList.add(DocumentDetails(documentType, documentNumber))
                            }

                        }
                    }
                    patient.name = humanNameList
                }
                DbClasses.ADDRESS_ORIGIN.name -> {

                    formData.formDataList.forEach { dbFormData ->

                        val address = Address()

                        val tag = dbFormData.tag
                        val text = dbFormData.text

                        address.id = formatterClass.generateUuid()
                        address.text = tag

                        if (tag == "Country of Origin" || tag == "Country of Residence"){
                            address.country = text
                        }
                        if (tag == "Residential Address in Referring Country"){
                            address.state = text
                        }
                        if (tag == "Residential Address in Receiving Country"){
                            address.city = text
                        }

                        patient.addAddress(address)
                    }
                }

                DbClasses.NEXT_OF_KIN.name -> {
                    addNextOfKinToPatient(patient, formData.formDataList)
                }
            }
        }

        val id = formatterClass.generateUuid()
        patient.id = id

        if (documentList.isNotEmpty()){

            documentList.forEach {

                val identifierSystem0 = Identifier()

                val typeCodeableConcept0 = CodeableConcept()

                val codingList0 = ArrayList<Coding>()
                val coding0 = Coding()
                coding0.system = "document-type"
                coding0.code = "document-type"
                coding0.display = "Document Type"
                codingList0.add(coding0)
                typeCodeableConcept0.coding = codingList0
                typeCodeableConcept0.text = it.documentType

                identifierSystem0.value = it.documentName
                identifierSystem0.system = "document-number"
                identifierSystem0.type = typeCodeableConcept0

                identifier.add(identifierSystem0)

            }

        }

        val identifierSystem0 = Identifier()

        val typeCodeableConcept0 = CodeableConcept()

        val codingList0 = ArrayList<Coding>()
        val coding0 = Coding()
        coding0.system = "system-creation"
        coding0.code = "system_creation"
        coding0.display = "System Creation"
        codingList0.add(coding0)
        typeCodeableConcept0.coding = codingList0
        typeCodeableConcept0.text = formatterClass.formatCurrentDateTime(Date())

        identifierSystem0.value = formatterClass.formatCurrentDateTime(Date())
        identifierSystem0.system = "system-creation"
        identifierSystem0.type = typeCodeableConcept0
        identifier.add(identifierSystem0)

        patient.identifier = identifier

        formatterClass.saveSharedPref("","patientId",id)

       return saveResourceToDatabase(patient, "Patient")
    }

    fun createPatientResource(formDataList: ArrayList<FormData>) = runBlocking{
        createPatientResourceBac(formDataList)
    }

    private suspend fun saveResourceToDatabase(resource: Resource, type: String) :List<String>{
        Log.e("----", "----$type")
        return fhirEngine.create(resource)
    }

    // Helper function to add name to Patient resource
    private fun addNameToPatient(dbFormData: DbFormData):HumanName {

        val humanName = HumanName()
        humanName.text = dbFormData.tag
        when (dbFormData.tag) {
            "First Name","Middle Name" -> {
                humanName.use = HumanName.NameUse.OFFICIAL
                humanName.addGiven(dbFormData.text)
            }
            "NickName" -> {
                humanName.use = HumanName.NameUse.NICKNAME
                humanName.addGiven(dbFormData.text)
            }
            "Last Name" -> {
                humanName.use = HumanName.NameUse.USUAL
                humanName.family = dbFormData.text
            }
        }
        return humanName
    }

    // Helper function to map gender
    private fun mapGender(genderText: String): Enumerations.AdministrativeGender {
        return when (genderText) {
            "Male" -> Enumerations.AdministrativeGender.MALE
            "Female" -> Enumerations.AdministrativeGender.FEMALE
            else -> Enumerations.AdministrativeGender.UNKNOWN
        }
    }

    // Helper function to parse date string
    private fun parseDate(dateText: String): Date {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.parse(dateText) ?: Date()
    }

    // Helper function to add Next of Kin to Patient resource
    private fun addNextOfKinToPatient(patient: Patient, nextOfKinList: ArrayList<DbFormData>) {
        val contactComponent = Patient.ContactComponent()
        var name = HumanName()

        nextOfKinList.forEach { dbFormData ->
            when (dbFormData.tag) {
                "Full Name" -> {
                    name.family = dbFormData.text
                }
                "Relationship" -> {
                    contactComponent.relationship = listOf(
                        // Use proper relationship code from FHIR (e.g., 'family')
                        CodeableConcept().apply {
                            text = dbFormData.text
                        }
                    )
                }
                "Telephone in referring country" , "Telephone", "Telephone in receiving country" -> {
                    contactComponent.addTelecom(ContactPoint().apply {
                        system = ContactPoint.ContactPointSystem.PHONE
                        value = dbFormData.text
                    })
                }
            }
        }

        contactComponent.name = name
        patient.addContact(contactComponent)
    }}