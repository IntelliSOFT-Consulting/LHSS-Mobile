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

package com.intellisoft.chanjoke.add_patient

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.patient_list.PatientListViewModel
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.android.fhir.datacapture.validation.Invalid
import com.google.android.fhir.datacapture.validation.QuestionnaireResponseValidator
import com.intellisoft.chanjoke.fhir.data.DbPatientData
import com.intellisoft.chanjoke.fhir.data.DbPatientDataAnswer
import com.intellisoft.chanjoke.fhir.data.DbValueCoding
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.Identifiers
import com.intellisoft.chanjoke.fhir.data.Item
import com.intellisoft.chanjoke.fhir.data.ValueCoding
import com.intellisoft.chanjoke.fhir.data.ValueString
import java.util.UUID
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Address
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Enumerations
import org.hl7.fhir.r4.model.HumanName
import org.hl7.fhir.r4.model.Identifier
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.RelatedPerson
import org.hl7.fhir.r4.model.ResourceType
import org.hl7.fhir.r4.model.codesystems.AdministrativeGender
import org.json.JSONObject
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter


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
            val dbPatientDataAnswerName = findCloseMatchAndGetAnswer("7196281948590")
            if (dbPatientDataAnswerName != null){
                val valueData = dbPatientDataAnswerName.valueString ?: dbPatientDataAnswerName.valueCoding?.display
                humanName.family = valueData
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
            val dbPatientDataAnswerCountryResidence = findCloseMatchAndGetAnswer("2298053798366")
            if (dbPatientDataAnswerCountryResidence != null){

                val valueData = dbPatientDataAnswerCountryResidence.valueString ?: dbPatientDataAnswerCountryResidence.valueCoding?.display
                if (valueData != null) {

                    val addressList = ArrayList<Address>()

                    val address = Address()
                    address.country = valueData
                    addressList.add(address)

                    patient.address = addressList
                }
            }
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
        val dob =
            if (hasBirthDateElement()) {
                LocalDate.parse(birthDateElement.valueAsString, DateTimeFormatter.ISO_DATE)
            } else {
                null
            }
        val phone = if (hasTelecom()) telecom[0].value else ""
        val city = if (hasAddress()) address[0].city else ""
        val country = if (hasAddress()) address[0].country else ""
        val isActive = active
        val html: String = if (hasText()) text.div.valueAsString else ""
        val identification: String = if (hasIdentifier()) identifier[0].value else "N/A"

        return PatientListViewModel.PatientItem(
            id = position.toString(),
            resourceId = patientId,
            name = name,
            gender = gender ?: "",
            dob = dob,
            identification = identification,
            phone = phone ?: "",
            city = city ?: "",
            country = country ?: "",
            isActive = isActive,
            html = html,
        )
    }
}
