package com.intellisoft.lhss

import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellisoft.lhss.databinding.FragmentRegPreviewBinding
import com.intellisoft.lhss.databinding.FragmentWorkFlowReviewBinding
import com.intellisoft.lhss.detail.ui.main.adapters.PatientDetailDataAdapter
import com.intellisoft.lhss.fhir.FhirApplication
import com.intellisoft.lhss.fhir.data.CustomPatient
import com.intellisoft.lhss.fhir.data.DbAdministrative
import com.intellisoft.lhss.fhir.data.DbPatientDataDetails
import com.intellisoft.lhss.fhir.data.DbWorkFlowData
import com.intellisoft.lhss.fhir.data.FormatterClass
import com.intellisoft.lhss.utils.BlurBackgroundDialog
import com.intellisoft.lhss.vaccine.AdministerVaccineViewModel
import com.intellisoft.lhss.viewmodel.PatientDetailsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray

class WorkFlowReviewFragment : Fragment() {

    private lateinit var binding: FragmentWorkFlowReviewBinding
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var fhirEngine: FhirEngine
    private val formatterClass = FormatterClass()
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private val viewModel: AdministerVaccineViewModel by viewModels()
    private var dbPatientDataDetailsList = ArrayList<DbPatientDataDetails>()
    private lateinit var patientId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        binding = FragmentWorkFlowReviewBinding.inflate(layoutInflater)
        patientId = formatterClass.getSharedPref("patientId", requireContext()).toString()

        fhirEngine = FhirApplication.fhirEngine(requireContext())

        layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setHasFixedSize(true)

        layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setHasFixedSize(true)

        binding.nextSubmit.setOnClickListener {

            val progressBar = ProgressDialog(requireContext())
            progressBar.setCanceledOnTouchOutside(false)
            progressBar.setTitle("Saving Details")
            progressBar.setMessage("Please wait as the records are being saved")
            progressBar.show()

            CoroutineScope(Dispatchers.IO).launch {
                viewModel.performManualExtraction(dbPatientDataDetailsList, patientId)

                CoroutineScope(Dispatchers.Main).launch {
                    progressBar.dismiss()

                    val blurBackgroundDialog =
                        BlurBackgroundDialog(this@WorkFlowReviewFragment, requireContext())
                    blurBackgroundDialog.show()

                }

            }
        }

        binding.imgBtnBack.setOnClickListener {
            formatterClass.saveSharedPref("isWorkflowUpdateBack","true", requireContext())

        }

        getData()

        return binding.root

    }

    private fun getData() {

        val titleValue = formatterClass.getSharedPref("title", requireContext())

        val toolbar = view?.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = titleValue
        }

        formatterClass.deleteSharedPref("isWorkflowUpdateBack", requireContext())

        val gson = Gson()

        val personal = formatterClass.getSharedPref("workFlowPersonal", requireContext())
        if (personal != null){

            val jsonArray = JSONArray(personal)
            // Iterate through the JSON array and extract key-value pairs
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val key = jsonObject.getString("key")
                val value = jsonObject.getString("value")

                // Create a WorkflowItem object and add it to the list
                val workflowItem = DbPatientDataDetails(key, value)
                dbPatientDataDetailsList.add(workflowItem)
            }

        }

        CoroutineScope(Dispatchers.Main).launch {
            val visitHistoryAdapter = PatientDetailDataAdapter(dbPatientDataDetailsList, requireContext())
            binding.recyclerView.adapter = visitHistoryAdapter
        }

    }



}