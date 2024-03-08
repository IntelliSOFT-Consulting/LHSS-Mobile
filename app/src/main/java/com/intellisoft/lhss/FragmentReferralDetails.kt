package com.intellisoft.lhss

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.intellisoft.lhss.detail.PatientDetailActivity
import com.intellisoft.lhss.fhir.FhirApplication
import com.intellisoft.lhss.fhir.data.FormatterClass
import com.intellisoft.lhss.patient_list.PatientListViewModel
import com.intellisoft.lhss.viewmodel.PatientDetailsViewModel
import com.intellisoft.lhss.viewmodel.PatientDetailsViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class FragmentReferralDetails : Fragment() {

    private var formatterClass = FormatterClass()
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var topBanner: LinearLayout
    private var _binding: FragmentReferralDetailsBinding? = null
    private val binding
        get() = _binding!!
    private var isSearched = false

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    private lateinit var patientId: String


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

        patientId = formatterClass.getSharedPref("patientId", requireContext()).toString()

        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = "Referrals"
            setDisplayHomeAsUpEnabled(false)
            setHomeButtonEnabled(false)
            setHomeAsUpIndicator(null)
        }
        recyclerView = binding.recyclerView

        fhirEngine = FhirApplication.fhirEngine(requireContext())
        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModelFactory(requireContext().applicationContext as Application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        binding.btnPrevious.setOnClickListener {
            onBackPressed()
        }

        getReferralsDetails()
    }

    private fun onBackPressed() {
        val intent = Intent(requireContext() , MainActivity::class.java)
        startActivity(intent)
    }

    private fun getReferralsDetails() {

        CoroutineScope(Dispatchers.IO).launch {
            val encounterId = formatterClass.getSharedPref("encounterId", requireContext())
            if (encounterId != null){
                val observationList = patientDetailsViewModel.getReferralDetails(encounterId)
                if (observationList.isNotEmpty()){
                    Log.e("---->","<----")
                    println(observationList)
                    Log.e("---->","<----")
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}