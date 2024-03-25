package com.intellisoft.lhss.detail

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.intellisoft.lhss.R
import com.intellisoft.lhss.databinding.FragmentDetailBinding
import com.intellisoft.lhss.databinding.FragmentRecommendationBinding
import com.intellisoft.lhss.detail.ui.main.adapters.PatientDetailDataAdapter
import com.intellisoft.lhss.fhir.FhirApplication
import com.intellisoft.lhss.fhir.data.DbPatientDataDetails
import com.intellisoft.lhss.fhir.data.FormatterClass
import com.intellisoft.lhss.viewmodel.PatientDetailsViewModel
import com.intellisoft.lhss.viewmodel.PatientDetailsViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
/**
 * A simple [Fragment] subclass.
 * Use the [DetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetailFragment : Fragment() {

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine
    private val formatterClass = FormatterClass()
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var binding: FragmentDetailBinding
    private var encounterId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDetailBinding.inflate(inflater, container, false)

        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientId = formatterClass.getSharedPref("patientId", requireContext()).toString()

        layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setHasFixedSize(true)

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModelFactory(requireContext().applicationContext as Application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setHasFixedSize(true)
        binding.btnPrevious.setOnClickListener { onBackPressed() }

        getDetails()



        binding.btnReceivePatient.setOnClickListener {
            if (encounterId != null) {
                patientDetailsViewModel.updateEncounter(encounterId!!)
                formatterClass.deleteSharedPref("workflowName", requireContext())
            }
            onBackPressed()
        }

        return binding.root
    }

    private fun onBackPressed() {
        val intent = Intent(requireContext() , PatientDetailActivity::class.java)
        startActivity(intent)
    }

    private fun getDetails() {

        //Origin Should not have ability to receive
        CoroutineScope(Dispatchers.IO).launch {

            var loggedInFacility = ""
//            var referralOriginFacility = ""
            var referralDestinationFacility = ""

//            val referralOrigin = formatterClass.getSharedPref("referralOrigin", requireContext())
//            if (referralOrigin != null){
//                referralOriginFacility = referralOrigin.replace("-", " ")
//            }
            val referralDestination = formatterClass.getSharedPref("referralDestination", requireContext())
            if (referralDestination != null){
                referralDestinationFacility = referralDestination.replace("-", " ")
            }

            val practitionerFacility = formatterClass.getSharedPref("practitionerFacility", requireContext())
            if (practitionerFacility != null){
                loggedInFacility = practitionerFacility.replace("-", " ")
            }





            val workflowName = formatterClass.getSharedPref("workflowName", requireContext())
            if (workflowName != null){
                if (workflowName == "REFERRALS"){
                    if (loggedInFacility == referralDestinationFacility){
                        CoroutineScope(Dispatchers.Main).launch {
                            binding.btnReceivePatient.visibility = View.VISIBLE
                        }
                    }
                }
            }

            encounterId = formatterClass.getSharedPref("encounterId", requireContext())
            if (encounterId != null){
                val patientDataList = ArrayList<DbPatientDataDetails>()
                val observations = patientDetailsViewModel.generateObservations(encounterId!!)
                observations.forEach {
                    val key = it.text
                    val name = it.name

                    val dbPatientDataDetails = DbPatientDataDetails(key, name)
                    patientDataList.add(dbPatientDataDetails)
                }

                CoroutineScope(Dispatchers.Main).launch {
                    val visitHistoryAdapter = PatientDetailDataAdapter(patientDataList, requireContext())
                    binding.recyclerView.adapter = visitHistoryAdapter
                }
            }
        }




    }



}