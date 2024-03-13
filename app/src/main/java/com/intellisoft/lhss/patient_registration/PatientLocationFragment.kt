package com.intellisoft.lhss.patient_registration

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.intellisoft.lhss.R
import com.intellisoft.lhss.databinding.ActivityPatientRegistrationBinding
import com.intellisoft.lhss.databinding.FragmentPatientLocationBinding
import com.intellisoft.lhss.fhir.data.FormatterClass

class PatientLocationFragment : Fragment() {

    private lateinit var binding: FragmentPatientLocationBinding
    private var formatterClass = FormatterClass()

//    val identificationTypeList = resources.getStringArray(R.array.identification_type)
//    val occupationList = resources.getStringArray(R.array.occupation_type)
//    val countryList = resources.getStringArray(R.array.country)
//    val djiboutiRegionList = resources.getStringArray(R.array.djibouti_region)
//    val ethiopiaRegionList = resources.getStringArray(R.array.ethiopia_region)
//    val djiboutiDistrictList = resources.getStringArray(R.array.djibouti_district)
//    val ethiopiaDistrictList = resources.getStringArray(R.array.ethiopia_district)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentPatientLocationBinding.inflate(layoutInflater)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isUpdate = formatterClass.getSharedPref("isUpdate", requireContext())

        if (isUpdate != null) {
//            displayInitialData()
        }







    }

}