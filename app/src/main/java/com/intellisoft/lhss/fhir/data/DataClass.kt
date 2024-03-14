package com.intellisoft.lhss.fhir.data

import com.intellisoft.lhss.R
import com.intellisoft.lhss.vaccine.validations.BasicVaccine

enum class UrlData(var message: Int) {
    BASE_URL(R.string.base_url),
}

data class CustomPatient(
    val firstname: String,
    val middlename: String,
    val lastname: String,
    val gender: String,
    val dateOfBirth: String,
    val age: String
)
data class DbAdministrative(
    val identificationType:String,
    val identificationNumber:String,
    val occupationType:String,
    val originCountry:String,
    val residenceCountry:String,
    val region:String,
    val district:String,
)

data class DbVaccineData(
    val logicalId: String,
    val vaccineName: String,
    val doseNumber: String,
    val dateAdministered: String,
    val status: String,
)
data class DbPatientDataDetails(
    val key:String,
    val value:String
)

data class AdverseEventData(
    val logicalId: String,
    val type: String,
    val date: String,

    )

data class EncounterItem(
    val id: String,
    val code: String,
    val effective: String,
    val value: String
) {
    override fun toString(): String = code
}

data class DbCodeValue(
    val code: String,
    val value: String,
    val dateTime:String? = null
)
data class ObservationDateValue(
    val date: String,
    val value: String,
)

enum class NavigationDetails {
    ADMINISTER_VACCINE,
    UPDATE_CLIENT_HISTORY,
    LIST_VACCINE_DETAILS,
    CLIENT_LIST,
    EDIT_CLIENT,

    VISIT_HISTORY,
    REFERRAL_LIST,
    ADD_VISIT_HISTORY,
    ADD_REFERRAL_LIST,

    REGISTER_VACCINE,

    DETAIL_VIEW,
    REFERRAL_DETAIL_VIEW,

    PRACTITIONER_VIEW,
}
data class DbAppointmentData(
    val id: String? = null,
    val title:String,
    val description:String,
    val vaccineName: String?,
    val dateScheduled: String,

    val recommendationList: ArrayList<DbAppointmentDetails>? = null,
    val status:String = ""
)

enum class Identifiers{
    SYSTEM_GENERATED
}

data class DbVaccineStockDetails(
    val name: String,
    val value: String
)

//This is the recommendation
data class DbAppointmentDetails(
    val appointmentId:String,
    val dateScheduled: String,
    val doseNumber: String?,
    val targetDisease: String,
    val vaccineName: String,
    val appointmentStatus: String
)
data class DbSignIn(
    val idNumber: String,
    val password: String
)
data class DbSignInResponse(
    val access_token:String,
    val expires_in:String,
    val refresh_expires_in:String,
    val refresh_token:String,
)
data class DbUserInfoResponse(
    val user:DbUser?,
)
data class DbUser(
    val fullNames:String,
    val idNumber:String,
    val practitionerRole:String,
    val fhirPractitionerId:String,
    val email:String,
    val phone:String?,
    val id:String,
    val facility:String,
)

data class DbVaccinationSchedule(
    val scheduleTime: String,
    val scheduleStatus: String,
    val scheduleVaccinationList: ArrayList<DbScheduleVaccination>
)
data class DbScheduleVaccination(
    val vaccineName: String,
    val vaccineDate:String,
    val vaccineStatus:String
)
data class DbVaccineSchedule(
    val scheduleTime: String,
    val scheduleStatus: String?,
    val scheduleVaccinationList: List<BasicVaccine>
)


data class QuestionnaireResponse(
    val item: List<Item>
)


data class Item(
    val linkId: String,
    val text: String,
    val answer: List<Answer>,
    val item: List<Item>?
)


sealed class Answer


data class ValueString(val valueString: String) : Answer()


data class ValueCoding(
    val valueCoding: ValueCodingDetails
) : Answer()


data class ValueCodingDetails(
    val system: String,
    val code: String,
    val display: String
)

data class DbPatientData(
    val linkId: String,
    val text: String,
    val answer: DbPatientDataAnswer
)

data class DbPatientDataAnswer(
    val valueString: String?,
    val valueCoding: DbValueCoding?
)

data class DbValueCoding(
    val system: String,
    val code: String,
    val display: String
)

data class DbEncounter(
    val id:String,
    val name: String,
    val date: String
)
data class DbObservation(
    val id:String,
    val encounterId:String? = null,
    val text: String,
    val name: String,
    val date: String,
    val status: String? = null,
)
data class DbEncounterReferrals(
    val encounterId: String? = null,
    val patientId:String,
    val type:String,
    val status:String,

)