package com.intellisoft.chanjoke.fhir.data

import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.vaccine.validations.BasicVaccine
import org.hl7.fhir.r4.model.ImmunizationRecommendation
enum class UrlData(var message: Int) {
    BASE_URL(R.string.base_url),
}
data class DbVaccineData(
    val logicalId: String,
    val vaccineName: String,
    val doseNumber: String,
    val dateAdministered: String,
    val status: String,
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
    EDIT_CLIENT
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