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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.intellisoft.lhss.databinding.FragmentReferralDetailsBinding
import com.intellisoft.lhss.detail.PatientDetailActivity
import com.intellisoft.lhss.detail.ui.main.adapters.PatientDetailDataAdapter
import com.intellisoft.lhss.fhir.FhirApplication
import com.intellisoft.lhss.fhir.data.DbPatientDataDetails
import com.intellisoft.lhss.fhir.data.FormatterClass
import com.intellisoft.lhss.patient_list.PatientListViewModel
import com.intellisoft.lhss.shared.Splash
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
    private var encounterId: String? = null
    private lateinit var layoutManager: RecyclerView.LayoutManager


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
        encounterId = formatterClass.getSharedPref("encounterId", requireContext())

        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = "Referrals"
            setDisplayHomeAsUpEnabled(false)
            setHomeButtonEnabled(false)
            setHomeAsUpIndicator(null)
        }

        layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)

        fhirEngine = FhirApplication.fhirEngine(requireContext())
        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModelFactory(requireContext().applicationContext as Application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        binding.btnPrevious.setOnClickListener {
            onBackPressed()
        }

        binding.btnReceivePatient.setOnClickListener {

            if (encounterId != null){
                patientDetailsViewModel.updateEncounter(encounterId!!)

                onBackPressed()
            }


        }

        getReferralsDetails()
    }

    private fun onBackPressed() {
        val intent = Intent(requireContext() , MainActivity::class.java)
        startActivity(intent)
    }

    private fun getReferralsDetails() {

        CoroutineScope(Dispatchers.IO).launch {
            val dbPatientDataDetailsList = ArrayList<DbPatientDataDetails>()

            if (encounterId != null){
                println(encounterId)

                val observationList = patientDetailsViewModel.getReferralDetails(encounterId!!)

                if (observationList.isNotEmpty()){
                    observationList.forEach {

                        val text = it.text
                        val name = it.name
                        val dbPatientDataDetails = DbPatientDataDetails(text, name)
                        dbPatientDataDetailsList.add(dbPatientDataDetails)
                    }
                }
            }

            CoroutineScope(Dispatchers.Main).launch {

                if (dbPatientDataDetailsList.isEmpty()){
                    binding.progressBar.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    binding.btnReceivePatient.visibility = View.GONE
                }else{
                    binding.progressBar.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    binding.btnReceivePatient.visibility = View.VISIBLE
                }

                val visitHistoryAdapter = PatientDetailDataAdapter(dbPatientDataDetailsList, requireContext())
               recyclerView.adapter = visitHistoryAdapter
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}