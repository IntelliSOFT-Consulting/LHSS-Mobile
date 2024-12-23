package com.intellisoft.lhss

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.search.search
import com.intellisoft.lhss.shared.DbLocationResponse
import com.intellisoft.lhss.shared.DbPatientItem
import com.intellisoft.lhss.shared.FormatterClass
import com.intellisoft.lhss.shared.LocationDetails
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.IdType.of
import org.hl7.fhir.r4.model.Location
import org.hl7.fhir.r4.model.Resource

class LocationViewModel(
    application: Application, 
    private val fhirEngine: FhirEngine) :
    AndroidViewModel(application) {

    private var formatterClass = FormatterClass(application.applicationContext)
    val locationList = MutableLiveData<List<DbPatientItem>>()

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


