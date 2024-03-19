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

package com.intellisoft.lhss.add_patient

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.android.fhir.datacapture.validation.Invalid
import com.google.android.fhir.datacapture.validation.QuestionnaireResponseValidator
import com.google.gson.Gson
import com.intellisoft.lhss.fhir.FhirApplication
import com.intellisoft.lhss.fhir.data.CustomPatient
import com.intellisoft.lhss.fhir.data.DbAdministrative
import com.intellisoft.lhss.fhir.data.DbPatientDataAnswer
import com.intellisoft.lhss.fhir.data.FormatterClass
import com.intellisoft.lhss.patient_list.PatientListViewModel
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Address
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.ContactPoint
import org.hl7.fhir.r4.model.Enumerations
import org.hl7.fhir.r4.model.HumanName
import org.hl7.fhir.r4.model.Identifier
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.model.StringType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID


/** ViewModel for patient registration screen {@link AddPatientFragment}. */
class AddPatientViewModel(application: Application, private val state: SavedStateHandle) :
    AndroidViewModel(application) {

    private var _questionnaireJson: String? = null
    val questionnaireJson: String
        get() = fetchQuestionnaireJson()

    val isPatientSaved = MutableLiveData<Boolean>()

    private val questionnaire: Questionnaire
        get() =
            FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
                .parseResource(questionnaireJson)
                    as Questionnaire

    private var fhirEngine: FhirEngine = FhirApplication.fhirEngine(application.applicationContext)

    /**
     * Saves patient registration questionnaire response into the application database.
     *
     * @param questionnaireResponse patient registration questionnaire response
     */
    fun savePatient(questionnaireResponse: QuestionnaireResponse, context: Context) {
        viewModelScope.launch {
            if (
                QuestionnaireResponseValidator.validateQuestionnaireResponse(
                    questionnaire,
                    questionnaireResponse,
                    getApplication(),
                )
                    .values
                    .flatten()
                    .any { it is Invalid }
            ) {
                isPatientSaved.value = false
                return@launch
            }

            val entry =
                ResourceMapper.extract(
                    questionnaire,
                    questionnaireResponse,
                )
                    .entryFirstRep
//            if (entry.resource !is Patient) {
//                return@launch
//            }

            val formatterClass = FormatterClass()
            val patientId = generateUuid()

            val patient = Patient()
            val cc = FhirContext.forR4()
            val questionnaire = cc.newJsonParser().encodeResourceToString(questionnaireResponse).trimIndent()

            val dbPatientDataList = FormatterClass().parseJson(questionnaire)
            fun findCloseMatchAndGetAnswer(searchString: String): DbPatientDataAnswer? {
                val matchingPatientData = dbPatientDataList.find { it.linkId.contains(searchString, ignoreCase = true) }
                return matchingPatientData?.answer?.takeIf { it.valueString != null || it.valueCoding != null }
            }

            //Patient Name
            val humanNameList = ArrayList<HumanName>()
            val humanName = HumanName()

            //First name
            var givenName = ""
            val dbPatientDataAnswerFirst = findCloseMatchAndGetAnswer("2015872619398")
            if (dbPatientDataAnswerFirst != null){
                givenName = (dbPatientDataAnswerFirst.valueString ?: dbPatientDataAnswerFirst.valueCoding?.display).toString()

            }

            //Other name


            //Surname
            val dbPatientDataAnswerSurname = findCloseMatchAndGetAnswer("7196281948590")
            if (dbPatientDataAnswerSurname != null){
                val valueData = dbPatientDataAnswerSurname.valueString ?: dbPatientDataAnswerSurname.valueCoding?.display
                humanName.family = "$valueData $givenName"
                humanNameList.add(humanName)
                patient.name = humanNameList
            }





            //Birth Date
            val dbPatientDataAnswerDob = findCloseMatchAndGetAnswer("4725705580511")
            if (dbPatientDataAnswerDob != null){
                val valueData = dbPatientDataAnswerDob.valueString ?: dbPatientDataAnswerDob.valueCoding?.display
                if (valueData != null) {
                    val dobValue = formatterClass.convertDateFormat(valueData)
                    val date = dobValue?.let { FormatterClass().convertStringToDate(it, "MMM d yyyy") }
                    patient.birthDate = date
                }
            }

            //Gender
            val dbPatientDataAnswerGender = findCloseMatchAndGetAnswer("3747594711636")
            if (dbPatientDataAnswerGender != null){
                val valueData = dbPatientDataAnswerGender.valueString ?: dbPatientDataAnswerGender.valueCoding?.display
                if (valueData != null){
                    val gender = if (valueData.contains("Male")){
                        Enumerations.AdministrativeGender.MALE
                    }else{
                        Enumerations.AdministrativeGender.FEMALE
                    }
                    patient.setGender(gender)
                }
            }

            //Country of Residence
            val dbPatientDataAnswerCountryOrigin = findCloseMatchAndGetAnswer("8503926458873")
            val dbPatientDataAnswerCountryResidence = findCloseMatchAndGetAnswer("2298053798366")

            val dbPatientDataDistrict1 = findCloseMatchAndGetAnswer("7880710415088")
            val dbPatientDataDistrict2 = findCloseMatchAndGetAnswer("78807104150881")

            var valueDataDistrict:String? = null
            if (dbPatientDataDistrict1 != null){
                valueDataDistrict = dbPatientDataDistrict1.valueString ?: dbPatientDataDistrict1.valueCoding?.display
            }
            if (dbPatientDataDistrict2 != null){
                valueDataDistrict = dbPatientDataDistrict2.valueString ?: dbPatientDataDistrict2.valueCoding?.display
            }

            val dbPatientDataRegion1 = findCloseMatchAndGetAnswer("8945109187038")
            val dbPatientDataRegion2 = findCloseMatchAndGetAnswer("89451091870381")

            var valueDataRegion:String? = null
            if (dbPatientDataRegion1 != null){
                valueDataRegion = dbPatientDataRegion1.valueString ?: dbPatientDataRegion1.valueCoding?.display
            }
            if (dbPatientDataRegion2 != null){
                valueDataRegion = dbPatientDataRegion2.valueString ?: dbPatientDataRegion2.valueCoding?.display
            }

            if (dbPatientDataAnswerCountryResidence != null &&
                valueDataDistrict != null &&
                valueDataRegion != null &&
                dbPatientDataAnswerCountryOrigin != null){

                val valueData = dbPatientDataAnswerCountryResidence.valueString ?: dbPatientDataAnswerCountryResidence.valueCoding?.display
                val valueDataOrigin = dbPatientDataAnswerCountryOrigin.valueString ?: dbPatientDataAnswerCountryOrigin.valueCoding?.display

                if (valueData != null && valueDataOrigin != null) {

                    val addressList = ArrayList<Address>()

                    val addressResidence = Address()
                    addressResidence.district = valueDataDistrict
                    addressResidence.city = valueDataRegion
                    addressResidence.country = valueData
                    addressResidence.text = "Country of Residence"
                    addressList.add(addressResidence)

                    val addressOrigin = Address()
                    addressOrigin.country = valueDataOrigin
                    addressOrigin.text = "Country of Origin"
                    addressList.add(addressOrigin)

                    patient.address = addressList

                }
            }

            //Phone number
            val dbPatientDataPhone = findCloseMatchAndGetAnswer("9997006999334")
            if (dbPatientDataPhone != null){
                val valueData = dbPatientDataPhone.valueString ?: dbPatientDataPhone.valueCoding?.display
                if (valueData != null) {
                    val contactList = ArrayList<Patient.ContactComponent>()
                    val contactComponent = Patient.ContactComponent()

                    val contactPointList = ArrayList<ContactPoint>()

                    val contactPoint = ContactPoint()
                    contactPoint.value = valueData
                    contactPointList.add(contactPoint)

                    contactComponent.telecom = contactPointList

                    contactList.add(contactComponent)

                    patient.contact = contactList
                }
            }

            //Identifier
            val identifierList = ArrayList<Identifier>()

            // Document Type
            val dbPatientDataType = findCloseMatchAndGetAnswer("9218063457934")
            if (dbPatientDataType != null){
                val valueDataValue = dbPatientDataType.valueString ?: dbPatientDataType.valueCoding?.display
                if (valueDataValue != null) {
                    val identifier = Identifier()

                    val codeableConcept = CodeableConcept()
                    codeableConcept.text = "Identification Type"
                    identifier.type = codeableConcept
                    identifier.value = valueDataValue
                    identifierList.add(identifier)
                }
            }
            // Document id
            val dbPatientDataAnswerId = findCloseMatchAndGetAnswer("2485233829669")
            if (dbPatientDataAnswerId != null){
                val valueData = dbPatientDataAnswerId.valueString ?: dbPatientDataAnswerId.valueCoding?.display
                if (valueData != null){
                    val identifier = Identifier()

                    val codeableConcept = CodeableConcept()
                    codeableConcept.text = "Identification Number"
                    identifier.type = codeableConcept
                    identifier.value = valueData
                    identifierList.add(identifier)
                }
            }

            //Add Created at
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val dateString = dateFormat.format(Date())

            val identifierCreatedAt = Identifier()
            val codeableConcept = CodeableConcept()
            codeableConcept.text = "createdAt"
            identifierCreatedAt.type = codeableConcept
            identifierCreatedAt.value = dateString

            identifierList.add(identifierCreatedAt)

            //Occupation
            val dbPatientDataOccupation = findCloseMatchAndGetAnswer("3257046516350")
            if (dbPatientDataOccupation != null){
                val valueData = dbPatientDataOccupation.valueString ?: dbPatientDataOccupation.valueCoding?.display
                if (valueData != null) {

                    val identifierOccupation = Identifier()

                    val codeableConceptOccupation = CodeableConcept()
                    codeableConceptOccupation.text = "Occupation"
                    identifierOccupation.type = codeableConceptOccupation
                    identifierOccupation.value = valueData

                    identifierList.add(identifierOccupation)

                }
            }

            patient.identifier = identifierList


            /**
             * Add the other Patient details
             */

            patient.id = patientId

            fhirEngine.create(patient)

            /**
             * Utilized patient's id for navigation
             * */



            FormatterClass().saveSharedPref("patientId", patientId, context)
            FormatterClass().saveSharedPref("isRegistration", "true", context)

            isPatientSaved.value = true
        }
    }


    suspend fun createManualPatient(){

        val gson = Gson()
        val formatterClass = FormatterClass()

        val registrationFlowPersonal = formatterClass.getSharedPref("registrationFlowPersonal", getApplication())
        val registrationFlowAdministrative = formatterClass.getSharedPref("registrationFlowAdministrative", getApplication())

        val customPatient = gson.fromJson(registrationFlowPersonal, CustomPatient::class.java)
        val dbAdministrative = gson.fromJson(registrationFlowAdministrative, DbAdministrative::class.java)

        val firstName = customPatient.firstname
        val middlename = customPatient.middlename
        val lastname = customPatient.lastname
        val age = customPatient.age
        val genderValue = customPatient.gender
        val dateOfBirth = customPatient.dateOfBirth
        val phoneNumber = customPatient.phoneNumber

        val identificationType = dbAdministrative.identificationType
        val identificationNumber = dbAdministrative.identificationNumber
        val occupationType = dbAdministrative.occupationType
        val originCountry = dbAdministrative.originCountry
        val residenceCountry = dbAdministrative.residenceCountry
        val region = dbAdministrative.region
        val district = dbAdministrative.district

        val patient = Patient()

        //Name
        val humanNameList = ArrayList<HumanName>()

        val humanName = HumanName()
        val fullName = "$firstName $middlename $lastname"
        humanName.family = lastname
        val givenList = ArrayList<StringType>()
        givenList.add(StringType(firstName))
        givenList.add(StringType(middlename))
        humanName.given = givenList
        humanName.text = fullName
        humanNameList.add(humanName)



        patient.name = humanNameList

        //Dob
        val dobValue = formatterClass.convertDateFormat(dateOfBirth)
        val date = dobValue?.let { FormatterClass().convertStringToDate(it, "MMM d yyyy") }
        patient.birthDate = date

        //Gender
        val gender = if (genderValue.contains("Male")){
            Enumerations.AdministrativeGender.MALE
        }else{
            Enumerations.AdministrativeGender.FEMALE
        }
        patient.setGender(gender)

        //Address
        val addressList = ArrayList<Address>()

        val addressResidence = Address()
        addressResidence.district = district
        addressResidence.city = region
        addressResidence.country = residenceCountry
        addressResidence.text = "Country of Residence"
        addressList.add(addressResidence)

        val addressOrigin = Address()
        addressOrigin.country = originCountry
        addressOrigin.text = "Country of Origin"
        addressList.add(addressOrigin)

        patient.address = addressList

        //Identifier
        val identifierList = ArrayList<Identifier>()

        val identifierOccupation = Identifier()
        val codeableConceptOccupation = CodeableConcept()
        codeableConceptOccupation.text = "Occupation"
        identifierOccupation.type = codeableConceptOccupation
        identifierOccupation.value = occupationType
        identifierList.add(identifierOccupation)

        //Add Created at
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val dateString = dateFormat.format(Date())
        val identifierCreatedAt = Identifier()
        val codeableConceptCreatedAt = CodeableConcept()
        codeableConceptCreatedAt.text = "createdAt"
        identifierCreatedAt.type = codeableConceptCreatedAt
        identifierCreatedAt.value = dateString
        identifierList.add(identifierCreatedAt)

        val identifierId = Identifier()
        val codeableConceptId = CodeableConcept()
        codeableConceptId.text = "Identification Number"
        identifierId.type = codeableConceptId
        identifierId.value = identificationNumber
        identifierList.add(identifierId)

        val identifierIdType = Identifier()
        val codeableConcept = CodeableConcept()
        codeableConcept.text = "Identification Type"
        identifierIdType.type = codeableConcept
        identifierIdType.value = identificationType
        identifierList.add(identifierIdType)

        patient.identifier = identifierList

        //Phone number
        val telecomList = ArrayList<ContactPoint>()
        val contactPoint = ContactPoint()
        contactPoint.value = phoneNumber
        telecomList.add(contactPoint)

        patient.telecom = telecomList

        /**
         * Add the other Patient details
         */
        var patientId:String? = ""
        val isPatientUpdate = formatterClass.getSharedPref("isPatientUpdate", getApplication())
        if(isPatientUpdate != null){
            patientId = formatterClass.getSharedPref("patientId", getApplication())
            if (patientId != null){
                patient.id = patientId
                fhirEngine.update(patient)
            }

        }else{
            patientId = generateUuid()
            patient.id = patientId
            fhirEngine.create(patient)
        }


        /**
         * Utilized patient's id for navigation
         * */


        formatterClass.saveSharedPref("patientId", patientId.toString(), getApplication())
        formatterClass.saveSharedPref("isRegistration", "true", getApplication())

        formatterClass.deleteSharedPref("registrationFlowPersonal", getApplication())
        formatterClass.deleteSharedPref("registrationFlowAdministrative", getApplication())
        formatterClass.deleteSharedPref("isPatientUpdate", getApplication())

    }




    private fun fetchQuestionnaireJson(): String {
        _questionnaireJson?.let {
            return it
        }
        _questionnaireJson =
            readFileFromAssets(state[AddPatientFragment.QUESTIONNAIRE_FILE_PATH_KEY]!!)
        return _questionnaireJson!!
    }

    private fun readFileFromAssets(filename: String): String {
        return getApplication<Application>().assets.open(filename).bufferedReader().use {
            it.readText()
        }
    }

    private fun generateUuid(): String {
        return UUID.randomUUID().toString()
    }


    internal fun Patient.toPatientItem(position: Int): PatientListViewModel.PatientItem {
        // Show nothing if no values available for gender and date of birth.
        val patientId = if (hasIdElement()) idElement.idPart else ""
        val name = if (hasName()) name[0].nameAsSingleString else ""
        val gender = if (hasGenderElement()) genderElement.valueAsString else ""
        val dob = if (hasBirthDate()) {
            birthDateElement.valueAsString
        } else {
            ""
        }

        var parsedDate: Date? = null
        if (dob != ""){
            val dobFormat = FormatterClass().convertDateFormat(dob)
            // Parse the input date
            if (dobFormat != null) {
                val dateFormat = SimpleDateFormat("MMM d yyyy", Locale.getDefault())
                parsedDate = dateFormat.parse(dobFormat)

            }

        }
        val phone = if (hasTelecom()) telecom[0].value else ""
        val city = if (hasAddress()) address[0].city else ""
        val country = if (hasAddress()) address[0].country else ""
        val isActive = active
        val html: String = if (hasText()) text.div.valueAsString else ""
        val identification: String = if (hasIdentifier()) identifier[0].value else "N/A"
        val createdAt: String = if (hasIdentifier()) identifier[0].value else "N/A"

        return PatientListViewModel.PatientItem(
            encounterId = null,
            id = position.toString(),
            resourceId = patientId,
            name = name,
            gender = gender ?: "",
            dob = parsedDate,
            identification = identification,
            phone = phone ?: "",
            city = city ?: "",
            country = country ?: "",
            isActive = isActive,
            html = html,
            createdAt = createdAt
        )
    }
}
