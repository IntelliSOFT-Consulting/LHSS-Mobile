package com.intellisoft.lhss25.fhir

object Constants {
    const val BASE_FHIR_URL="https://hiedhs.intellisoftkenya.com/hapi/fhir/"
    const val BASE_URL="https://hiedhs.intellisoftkenya.com/auth/"

    // System URIs
    const val SYSTEM_TB_REGISTRATION = "http://your-organization.org/tb-registration-number"
    const val SYSTEM_MEDICAL_RECORD_NUMBER = "http://terminology.hl7.org/CodeSystem/v2-0203"

    // Custom Codes for your TB system (You can define other relevant codes)
    const val TB_YOUR_REGISTRATION_CODE = "TB123456"
    const val TB_OUR_REGISTRATION_CODE = "TB9687686"

    const val REFERRAL_DATE = "RD20220101"
    const val RECEIVING_FACILITY_NAME = "RFN4355345355"
    const val TOPIC_NUMBER = "RFN54655474574"
    const val TOPIC_NUMBER_REASON_C0DE = "RFN54655474574967868"

    const val COUNTRY_RECEIVING = "RFN54655474574967868YGAD"
    const val REGION_COUNTY_RECEIVING = "RFN546554745749EWF"
    const val DISTRICT_SUB_COUNTY_RECEIVING = "RFN546554745749GEGWEF"
    const val WARD_RECEIVING = "RFN546554745749GWR"
    const val FACILITY_RECEIVING = "RFN54655474574GWGWW8778"



}