package com.intellisoft.lhss25

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.search.search
import com.intellisoft.lhss25.shared.DbLocationResponse
import com.intellisoft.lhss25.shared.DbPatientItem
import com.intellisoft.lhss25.shared.FormatterClass
import com.intellisoft.lhss25.shared.LocationDetails
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Location
import org.hl7.fhir.r4.model.Resource

class LocationViewModel(
    application: Application, 
    private val fhirEngine: FhirEngine) :
    AndroidViewModel(application) {

    private var formatterClass = FormatterClass(application.applicationContext)
    val locationList = MutableLiveData<List<DbPatientItem>>()

    // Function to list counties or regions based on the country

    fun getHierarchyDetails(requestParam: String, code: String)= runBlocking {
        getHierarchyDetailsBac(requestParam, code)
    }

    private suspend fun getHierarchyDetailsBac(requestParam: String, code: String): List<DbLocationResponse>{

        val locationList = when (code) {
            "REGION" -> {
                getCountiesOrRegions(requestParam)
            }
            "SUB_COUNTY" -> {
                fetchLocationsByName("COUNTY",requestParam)
            }
            "WARD" -> {
                getWards(requestParam)
            }
            "FACILITY" -> {
                getFacilities(requestParam)
            }
            else -> {
                fetchLocationsByCodeAndPartOf("", requestParam)
            }
        }

        return locationList

    }

    private suspend fun getCountiesOrRegions(country: String): List<DbLocationResponse> {
        return fetchLocationsByCodeAndPartOf("REGION", country) // "REGION" for Uganda or "COUNTY" for Kenya
    }

    // Function to list sub-counties or districts based on the county/region
    private suspend fun getSubCountiesOrDistricts(countyOrRegion: String): List<DbLocationResponse> {
        return fetchLocationsByName("SUB_COUNTY", countyOrRegion) // "SUB_COUNTY" or "DISTRICT"
    }

    // Function to list wards based on the sub-county/district
    private suspend fun getWards(subCountyOrDistrict: String): List<DbLocationResponse> {
        return fetchLocationsByCodeAndPartOf("WARD", subCountyOrDistrict)
    }

    // Function to list facilities based on the ward
    private suspend fun getFacilities(ward: String): List<DbLocationResponse> {
        return fetchLocationsByCodeAndPartOf("FACILITY", ward)
    }

    // Helper function to fetch locations dynamically based on a type code and parent reference
    private suspend fun fetchLocationsByCodeAndPartOf(code: String, parentReference: String): List<DbLocationResponse> {
        return fhirEngine.search<Location> {
            // Filter by partOf reference
            filter(Location.PARTOF, {value = parentReference })
//            filter(Location.NAME, {value = parentReference})
        }.map { createLocationDataItem(it.resource) }
    }

    fun getFacilityByName(name: String) = runBlocking {
        fetchLocationsByName("FACILITY", name)
    }

    private suspend fun fetchLocationsByName(code: String, parentReference: String): List<DbLocationResponse> {
        return fhirEngine.search<Location> {
            // Filter by partOf reference
            filter(Location.TYPE, {value = of(code)})
            filter(Location.NAME, {value = parentReference})
        }.map { createLocationDataItem(it.resource) }
    }

    // Function to create a location item (same as before)
    private fun createLocationDataItem(resource: Location): DbLocationResponse {
        val name = if (resource.hasName()) resource.name else ""
        val code = resource.typeFirstRep.codingFirstRep.code ?: ""
        val partOf = resource.partOf?.reference
        val id = if (resource.hasId()) resource.id else null

        return DbLocationResponse(
            name = name,
            code = code,
            partOf = partOf,
            id = id
        )
    }

    fun getLocationDetails(locationReference: String) = runBlocking {
        getLocationHierarchy(locationReference)
    }

    private suspend fun getLocationDetailsBac(locationReference: String) {

        val locationId = locationReference.replace("Location/", "")

        val countryCodeList = listOf(
            "0", //Kenya
            "TANZANIA",
            "UGANDA"
        )

        val locationResList = ArrayList<DbLocationResponse>()

        fhirEngine.search<Location> {
            filter(Resource.RES_ID, { value = of(locationId) })
        }.mapIndexed{index, searchResult -> createLocationItem(searchResult.resource, countryCodeList) }
            .let { locationResList.addAll(it) }


    }

    private fun createLocationItem(resource: Location, countryCodeList: List<String>): DbLocationResponse {

        val name = if (resource.hasName()) resource.name else ""
        var code = ""
        if(resource.hasType()){
            if (resource.typeFirstRep.hasCoding()){
                if (resource.typeFirstRep.codingFirstRep.hasCode()){
                    code = resource.typeFirstRep.codingFirstRep.code
                }
            }
        }
        val partOf = if (resource.hasPartOf()) resource.partOf.reference else ""

        return DbLocationResponse(
            name,
            code,
            partOf
        )


    }

    // Function to get the full hierarchy dynamically
    private suspend fun getLocationHierarchy(locationReference: String): List<DbLocationResponse> {
        val locationId = locationReference.replace("Location/", "")
        val hierarchy = mutableListOf<DbLocationResponse>()

        // Recursive function to fetch hierarchy
        var currentLocationId = locationId
        while (currentLocationId.isNotEmpty()) {
            val location = fetchLocation(currentLocationId) ?: break // Stop if location is not found
            val locationDetails = createLocationItem(location)
            hierarchy.add(locationDetails)

            // Update currentLocationId to partOf reference for the next iteration
            currentLocationId = locationDetails.partOf?.replace("Location/", "") ?: ""
        }

        return hierarchy.reversed() // Reverse to display hierarchy from country to facility
    }


    // Function to fetch a location from FHIR engine
    private suspend fun fetchLocation(locationId: String): Location? {
        return fhirEngine.search<Location> {
            filter(Location.RES_ID, { value = of(locationId) })
        }.firstOrNull()?.resource
    }

    // Function to create location item
    private fun createLocationItem(resource: Location): DbLocationResponse {
        val name = if (resource.hasName()) resource.name else ""
        val code = resource.typeFirstRep.codingFirstRep.code ?: ""
        val partOf = resource.partOf?.reference

        return DbLocationResponse(
            name = name,
            code = code,
            partOf = partOf
        )
    }


    fun isCodeInEnum(code: String): Boolean {
        return LocationDetails.values().any { it.name.equals(code, ignoreCase = true) }
    }

    class LocationViewModelFactory(
        private val application: Application,
        private val fhirEngine: FhirEngine,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
                return LocationViewModel(application, fhirEngine) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}


