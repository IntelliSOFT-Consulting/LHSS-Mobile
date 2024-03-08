package com.intellisoft.lhss

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.intellisoft.lhss.databinding.FragmentReferralDetailsBinding
import com.intellisoft.lhss.databinding.FragmentReferralsBinding
import com.intellisoft.lhss.fhir.FhirApplication
import com.intellisoft.lhss.fhir.data.FormatterClass
import com.intellisoft.lhss.patient_list.PatientListViewModel


class FragmentReferralDetails : Fragment() {

    private var formatterClass = FormatterClass()
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientListViewModel: PatientListViewModel
    private lateinit var topBanner: LinearLayout
    private var _binding: FragmentReferralDetailsBinding? = null
    private val binding
        get() = _binding!!
    private var isSearched = false

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentReferralDetailsBinding.inflate(inflater, container, false)

        return binding.root    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isSearched = false

        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
//            title = resources.getString(R.string.title_patient_list)
            setDisplayHomeAsUpEnabled(false)
            setHomeButtonEnabled(false)
            setHomeAsUpIndicator(null)
        }
        recyclerView = binding.patientList

        fhirEngine = FhirApplication.fhirEngine(requireContext())
        patientListViewModel =
            ViewModelProvider(
                this,
                PatientListViewModel.PatientListViewModelFactory(
                    requireActivity().application,
                    fhirEngine
                ),
            )[PatientListViewModel::class.java]

        getReferralsDetails()
    }

    private fun getReferralsDetails() {



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}