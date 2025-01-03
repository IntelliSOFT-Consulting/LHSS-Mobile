package com.intellisoft.lhss25.patient_details.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.search.search
import com.intellisoft.lhss25.shared.DbClasses
import com.intellisoft.lhss25.shared.DbFormData
import com.intellisoft.lhss25.shared.FormData
import com.intellisoft.lhss25.shared.FormatterClass
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Resource

class PatientCardViewModel(
    application: Application,
    private val fhirEngine: FhirEngine,
    private val patientId: String,
) : AndroidViewModel(application) {

    private val formatterClass = FormatterClass(application.applicationContext)

    fun getPatientInfo() = runBlocking {
        getPatientDetailDataModel()
    }

    private suspend fun getPatientDetailDataModel(): ArrayList<FormData> {


        val formDataList = ArrayList<FormData>()

        val searchResult =
            fhirEngine.search<Patient> {
                filter(Resource.RES_ID, { value = of(patientId) })
            }

        if (searchResult.isNotEmpty()){
            searchResult.first().let {
                val patient = it.resource

                /**
                 * 1. Demographics
                 */
                val demographicFormDataList =  ArrayList<DbFormData>()

                //Name
                if (patient.hasName()){
                    var name = ""
                    patient.name.forEach {humanName->
                        val tag = if (humanName.hasText()) humanName.text else ""
                        val text = if (humanName.hasGiven()){
                            humanName.givenAsSingleString
                        } else if (humanName.hasFamily()){
                            humanName.family
                        }else{
                            ""
                        }
                        if (tag != "" && text != ""){
                            name = "$name $text"
                            demographicFormDataList.add(
                                DbFormData(tag, text)
                            )
                        }

                    }
                    formatterClass.saveSharedPref("", "patientName", name)

                }

                //Gender
                if (patient.hasGenderElement()) {
                    val gender = it.resource.genderElement.valueAsString
                    demographicFormDataList.add(
                        DbFormData("Sex", gender)
                    )
                }

                //Phone
                if (patient.hasTelecom()) {
                    if (it.resource.telecom.isNotEmpty()) {

                        val telecomList = it.resource.telecom
                        telecomList.forEach {contactPoint ->
                            val rank = if (contactPoint.hasRank()) contactPoint.rank else 0
                            val contactPointValue = if (contactPoint.hasValue()) contactPoint.value else ""

                            val tag = when (rank) {
                                1 -> { "Telephone in referring country" }
                                2 -> { "Telephone in receiving country" }
                                else -> { "Telephone number" }
                            }

                            val dbFormData = DbFormData(tag, contactPointValue)
                            demographicFormDataList.add(dbFormData)

                        }

                    }
                }

                //DOB
                if (patient.hasBirthDateElement()) {
                    if (patient.birthDateElement.hasValue()) {
                        val birthDateElement = patient.birthDateElement.valueAsString
                        demographicFormDataList.add(
                            DbFormData("Date of Birth", birthDateElement)
                        )
                    }
                }

                formDataList.add(
                    FormData(
                        DbClasses.DEMOGRAPHICS.name,
                        demographicFormDataList
                    )
                )

                /**
                 * 2. Next Of Kin
                 */
                val kinFormDataList =  ArrayList<DbFormData>()

                if (patient.hasContact()) {
                    patient.contact.forEach { k ->

                        val name = if (k.hasName()) {
                            k.name.nameAsSingleString.toString()
                        } else ""
                        val phone = if (k.hasTelecom()) k.telecomFirstRep.value else ""
                        val rshp = if (k.hasRelationship()) k.relationshipFirstRep.text else ""

                        kinFormDataList.add(
                            DbFormData("Full Name", name)
                        )

                        val formattedPhone = if (phone.startsWith("+") || phone.startsWith("0")) phone else "+$phone"

                        kinFormDataList.add(
                            DbFormData("Phone Number", formattedPhone)
                        )
                        kinFormDataList.add(
                            DbFormData("Relationship", rshp)
                        )


                    }
                }
                formDataList.add(
                    FormData(
                        DbClasses.NEXT_OF_KIN.name,
                        kinFormDataList
                    )
                )

                /**
                 * 3. Address Information
                 */
                val addressFormDataList =  ArrayList<DbFormData>()

                if (patient.hasAddress()) {

                    val addressList = patient.address
                    addressList.forEach { address ->

                        val country = if (address.hasCountry()) address.country else ""
                        val state = if (address.hasState()) address.state else ""
                        val city = if (address.hasCity()) address.city else ""

                        val text = if (address.hasText()) address.text else ""

                        if (country != "" && text != ""){
                            //Country
                             addressFormDataList.add(
                                DbFormData(text, country)
                            )

                        }
                        if (state != "" && text!= ""){
                            addressFormDataList.add(
                                DbFormData(text, state)
                            )
                        }
                        if (city!= "" && text!= ""){
                            addressFormDataList.add(
                                DbFormData(text, city)
                            )
                        }
                    }

                }
                formDataList.add(
                    FormData(
                        DbClasses.ADDRESS.name,
                        addressFormDataList
                    )
                )

            }

        }


        return formDataList
    }


}

class PatientDetailsViewModelFactory(
    private val application: Application,
    private val fhirEngine: FhirEngine,
    private val patientId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(PatientCardViewModel::class.java)) {
            "Unknown ViewModel class"
        }
        return PatientCardViewModel(application, fhirEngine, patientId) as T
    }
}