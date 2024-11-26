package com.intellisoft.lhss.referrals.fragment

import android.app.Application
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.fhir.FhirEngine
import com.intellisoft.lhss.R
import com.intellisoft.lhss.databinding.FragmentDemographicsBinding
import com.intellisoft.lhss.databinding.FragmentReferralDetailsBinding
import com.intellisoft.lhss.dynamic_components.FieldManager
import com.intellisoft.lhss.fhir.FhirApplication
import com.intellisoft.lhss.patient_details.viewmodel.PatientCardViewModel
import com.intellisoft.lhss.patient_details.viewmodel.PatientDetailsViewModelFactory
import com.intellisoft.lhss.referrals.viewmodels.ReferralDetailsViewModel
import com.intellisoft.lhss.referrals.viewmodels.ReferralDetailsViewModelFactory
import com.intellisoft.lhss.shared.FormDataAdapter
import com.intellisoft.lhss.shared.FormatterClass

class ReferralDetailsFragment : Fragment() {

    private var _binding: FragmentReferralDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var fieldManager: FieldManager

    private lateinit var viewModel: ReferralDetailsViewModel
    private lateinit var formatterClass: FormatterClass
    private lateinit var fhirEngine: FhirEngine
    private var patientId:String = ""
    private var serviceRequestId:String = ""
    private lateinit var formDataAdapter: FormDataAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentReferralDetailsBinding.inflate(inflater, container, false)

        formatterClass = FormatterClass(requireContext())

        patientId = formatterClass.getSharedPref("", "patientId") ?: ""
        serviceRequestId = formatterClass.getSharedPref("", "serviceRequestId") ?: ""

        fhirEngine = FhirApplication.fhirEngine(requireContext())

        viewModel =
            ViewModelProvider(
                this,
                ReferralDetailsViewModelFactory(
                    requireContext().applicationContext as Application,
                    fhirEngine,
                    patientId,
                    serviceRequestId
                ),
            )
                .get(ReferralDetailsViewModel::class.java)

        navigationActions()

        return binding.root
    }

    private fun navigationActions() {
        // Set the next button text to "Continue" and add click listeners
        val navigationButtons = binding.navigationButtons
        navigationButtons.setNextButtonText("Receive Patient")

        navigationButtons.setBackButtonClickListener {
            // Handle back button click
            findNavController().navigateUp()
        }

        navigationButtons.setNextButtonClickListener {
            // Handle next button click
            // Navigate to the next fragment or perform any action
            findNavController().navigate(R.id.action_referralDetailsFragment_to_acknoledgementFormFragment)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val formDataList = viewModel.getServiceRequest()

        formDataAdapter = FormDataAdapter(formDataList, requireContext())
        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        binding.recyclerView.adapter = formDataAdapter

        val fullName = formatterClass.getNameFields(formDataList)
        binding.tvFullName.text = fullName
        val crossBorderId = "Cross Border Id: ${patientId.substring(0,6)}"
        binding.tvCrossBorderId.text = crossBorderId
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

}