package com.intellisoft.lhss.fhir.data

data class DbVaccineData(
    val vaccineName:String,
    val vaccineDosage:String
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
    val value: String
)
data class DbObservation(
    val text: String,
    val answer: String
)