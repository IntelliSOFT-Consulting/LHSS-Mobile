package com.intellisoft.lhss.shared

import android.view.View
import androidx.annotation.DrawableRes
import com.intellisoft.lhss.R
import org.hl7.fhir.r4.model.Communication
import org.hl7.fhir.r4.model.Reference

data class DbCommunication(
    val status: Communication.CommunicationStatus,
    val subject: Reference? = null,
    val recipient: Reference? = null,
    val sender: Reference? = null,
    val basedOn: ArrayList<Reference>,
    val title: String,
    val content: String,
    )

data class DbCommunicationData(
    val id: String,
    val patientId: String,
    val navigationId: Int,
    val title: String,
    val content: String,
    val dateTime: String,
    val status: String,
    val basedOn: ArrayList<String>
)

data class DbField(
    val widgets: String,
    val label: String,
    val isMandatory: Boolean = false,

    val inputType: Int? = null, // InputType for EditText (e.g., InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
    val optionList : List<String> = emptyList(),

    val isEnabled: Boolean = true,
    val fhirCode: String? = null,
    val isPastDate: Boolean = true,
    val startDate: String? = null,
    val endDate: String? = null,
    )

data class DbEncounterDetails(
    val id: String,
    val date: String,
    val reasonCode: String,
    val status: String
)

data class DbCarePlan(
    val id: String,
    val status: String,
    val fileNumber: String,
    val dateCreated: String,
    val supportingInfoList: ArrayList<Reference>
)

data class DbCounty(
    val id: Int,
    val name: String
)
data class DbSubCounty(
    val id: Int,
    val name: String
)

enum class DbWidgets{
    EDIT_TEXT,
    SPINNER,
    RADIO_BUTTON,
    DATE_PICKER,
    CHECK_BOX
}

data class Item(
    val title: String,
    val description: String,
    val imageResource: Int // Assuming image is a drawable resource
)


data class FormData(
    val title: String,
    val formDataList: ArrayList<DbFormData>
)

data class DbEncounter(
    val id: String,
    val date: String,
    val status: String,
    val filledBy: String,
    val referralReason: String,
    val basedOn: String,
)


data class DbFormData(
    val tag: String,
    var text: String,
    val fhirCode: String? = null,
)
data class DbPatientItem(
    val id: String,
    val name: String,
    val crossBorderId:String,
    val dob:String,
    val dateCreated: String?
)
data class DbServiceRequest(
    val id:String,
    val patientId: String,
    val status: String,
    val dateRecorded: String,
    val encounterList: ArrayList<Reference>,
    val referralReason: String?
)
enum class DbNavigationDetails{
    PATIENT_REGISTRATION,
    REFER_PATIENT,
    REFERRALS,
    CARE_PLAN
}
enum class DbClasses{
    DEMOGRAPHICS,
    ADDRESS,
    NEXT_OF_KIN,

    REFERRING_FACILITY_INFO,
    REFERRAL_INFO,

    TB_TREATMENT,
    HIV_STATUS_TREATMENT,
    LABORATORY_RESULTS,
    DST,
    DR_TB_FOLLOW_UP_TEST,

//    CLINICAL_REFERRAL_I,
//    CLINICAL_REFERRAL_II,
//    CLINICAL_REFERRAL_III,
//    CLINICAL_REFERRAL_IV,
//    CLINICAL_REFERRAL_V,

    ACKNOWLEDGEMENT_FORM,
    END_TREATMENT_FORM,
}
data class DbWorkFlow(
    val view: View?,
    val value: String
)
enum class WorkflowTitles(
    @DrawableRes val iconId: Int,
    val textId: String,
    val text: String,
) {
    DEMOGRAPHICS(R.drawable.ic_action_demographics, DbClasses.DEMOGRAPHICS.name, "Demographics"),
    ADDRESS(R.drawable.ic_action_address, DbClasses.ADDRESS.name, "Address"),
    NEXT_OF_KIN(R.drawable.ic_action_kin, DbClasses.NEXT_OF_KIN.name,  "Next Of Kin"),

    REFERRING_FACILITY_INFO(R.drawable.ic_referring_facility_info, DbClasses.REFERRING_FACILITY_INFO.name,  "Referring Facility Information"),
    REFERRAL_INFO(R.drawable.ic_referrals, DbClasses.REFERRAL_INFO.name,  "Referral Information"),

    TB_TREATMENT(R.drawable.ic_action_next_kin, DbClasses.TB_TREATMENT.name,  "Tb Treatment"),
    HIV_STATUS_TREATMENT(R.drawable.ic_action_next_kin, DbClasses.HIV_STATUS_TREATMENT.name,  "HIV Status and Treatment"),
    LABORATORY_RESULTS(R.drawable.ic_action_next_kin, DbClasses.LABORATORY_RESULTS.name,  "Laboratory Results"),
    DST(R.drawable.ic_action_next_kin, DbClasses.DST.name,  "DST/Cultures/LPA"),
    DR_TB_FOLLOW_UP_TEST(R.drawable.ic_action_next_kin, DbClasses.DR_TB_FOLLOW_UP_TEST.name,  "DR TB Follow Up Test"),

    ACKNOWLEDGEMENT_FORM(R.drawable.ic_action_ackno, DbClasses.ACKNOWLEDGEMENT_FORM.name,  "Acknowledgement Form"),
    END_TREATMENT_FORM(R.drawable.ic_action_ackno, DbClasses.END_TREATMENT_FORM.name,  "End of Treatment Form"),
}

data class DbSignIn(
    val idNumber: String,
    val password: String,
)

data class DbSignInResponse(
    val access_token: String,
    val expires_in: String,
    val refresh_expires_in: String,
    val refresh_token: String,
)
data class DbUserInfoResponse(
    val status: String,
    val user: DbUserInfo,
)
data class DbUserInfo(
    val id: String,
    val fhirPractitionerId: String,
    val fullNames: String,
    val idNumber: String,
    val phone: String,
    val email: String,
    val facility: String,
    val role: String)

data class DbResponseError(
    val status: String,
    val error: String,
)

