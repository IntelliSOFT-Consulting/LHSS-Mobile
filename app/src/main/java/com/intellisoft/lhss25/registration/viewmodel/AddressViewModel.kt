package com.intellisoft.lhss25.registration.viewmodel

import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddressViewModel : ViewModel() {

    // LiveData to hold the list of counties or districts
    private val _locations = MutableLiveData<List<String>>()
    val locations: LiveData<List<String>> get() = _locations

    // Function to get counties or districts based on the country name
    private fun getLocationsByCountry(countryName: String) {
        when (countryName) {
            "Kenya" -> {
                // Example counties in Kenya
                _locations.value = listOf(
                    "Baringo", "Bomet", "Bungoma", "Busia", "Elgeyo Marakwet", "Embu", "Garissa", "Homa Bay",
                    "Isiolo", "Kajiado", "Kakamega", "Kericho", "Kiambu", "Kilifi", "Kirinyaga", "Kisii",
                    "Kisumu", "Kitui", "Kwale", "Laikipia", "Lamu", "Machakos", "Makueni", "Mandera",
                    "Marsabit", "Meru", "Migori", "Mombasa", "Murang'a", "Nairobi", "Nakuru", "Nandi",
                    "Narok", "Nyamira", "Nyandarua", "Nyeri", "Samburu", "Siaya", "Taita Taveta",
                    "Tana River", "Tharaka Nithi", "Trans Nzoia", "Turkana", "Uasin Gishu", "Vihiga",
                    "Wajir", "West Pokot"
                )
            }
            "Uganda" -> {
                // Example districts in Uganda
                _locations.value = listOf(
                    "Central Region",
                    "Western Region",
                    "Eastern Region",
                    "Northern Region",
                    "Western Nile Region"
                )
            }
            else -> {
                _locations.value = emptyList() // No locations for other countries
            }
        }
    }

    fun extractFormData(rootLayout: LinearLayout) {
        // Traverse through all child views of rootLayout
        for (i in 0 until rootLayout.childCount) {
            val childView = rootLayout.getChildAt(i)

            when (childView) {
                is Spinner -> {
                    // Extract selected identification type from spinner
                    val text = childView.selectedItem.toString()
                    val tag = childView.tag.toString()

                    // Set the item selected listener
                    childView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                            // Get the selected country
                            val selectedCountry = parent.getItemAtPosition(position) as String
                            // Handle the selection change
                            if (tag == "Country of Residence"){
                                getLocationsByCountry(selectedCountry)
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {
                            // Handle the case when nothing is selected
                        }
                    }


                }

            }
        }
    }

}