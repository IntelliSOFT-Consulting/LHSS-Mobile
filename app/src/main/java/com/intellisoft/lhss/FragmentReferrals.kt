package com.intellisoft.lhss

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.intellisoft.lhss.databinding.FragmentReferralsBinding
import com.intellisoft.lhss.fhir.FhirApplication
import com.intellisoft.lhss.fhir.data.FormatterClass
import com.intellisoft.lhss.patient_list.PatientListViewModel

class FragmentReferrals : Fragment() {

    private var formatterClass = FormatterClass()
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientListViewModel: PatientListViewModel
    private lateinit var searchView: SearchView
    private lateinit var topBanner: LinearLayout
    private var _binding: FragmentReferralsBinding? = null
    private val binding
        get() = _binding!!
    private var isSearched = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentReferralsBinding.inflate(inflater, container, false)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isSearched = false

        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
//            title = resources.getString(R.string.title_patient_list)
            setDisplayHomeAsUpEnabled(false)
            setHomeButtonEnabled(false)
            setHomeAsUpIndicator(null)
        }
        searchView = binding.patientListContainer.search

        fhirEngine = FhirApplication.fhirEngine(requireContext())
        patientListViewModel =
            ViewModelProvider(
                this,
                PatientListViewModel.PatientListViewModelFactory(
                    requireActivity().application,
                    fhirEngine
                ),
            )[PatientListViewModel::class.java]
        clearSharedPref()

        val recyclerView: RecyclerView = binding.patientListContainer.patientList

        searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String): Boolean {
                    isSearched = true
                    patientListViewModel.searchPatientsByName(newText)
                    return true
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    isSearched = true
                    patientListViewModel.searchPatientsByName(query)
                    return true
                }
            },
        )
        searchView.setOnQueryTextFocusChangeListener { view, focused ->
            if (!focused) {
                // hide soft keyboard
                (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(view.windowToken, 0)
            }
        }

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (searchView.query.isNotEmpty()) {
                            searchView.setQuery("", true)
                        } else {
                            isEnabled = false
                            activity?.onBackPressed()
                        }
                    }
                },
            )
    }
    private fun clearSharedPref() {
        //Clear the vaccines
        formatterClass.clearVaccineShared(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()

        formatterClass.deleteSharedPref("selectedVaccinationVenue", requireContext())
        formatterClass.deleteSharedPref("isSelectedVaccinationVenue", requireContext())

        _binding = null
    }



}