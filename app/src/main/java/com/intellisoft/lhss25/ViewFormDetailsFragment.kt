package com.intellisoft.lhss25

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.fhir.FhirEngine
import com.intellisoft.lhss25.databinding.FragmentViewFormBinding
import com.intellisoft.lhss25.databinding.FragmentViewFormDetailsBinding
import com.intellisoft.lhss25.fhir.FhirApplication
import com.intellisoft.lhss25.referrals.viewmodels.ReferralDetailsViewModel
import com.intellisoft.lhss25.referrals.viewmodels.ReferralDetailsViewModelFactory
import com.intellisoft.lhss25.shared.FormDataAdapter
import com.intellisoft.lhss25.shared.FormatterClass

class ViewFormDetailsFragment : Fragment() {

    private var _binding: FragmentViewFormDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var fhirEngine: FhirEngine
    private lateinit var formatterClass: FormatterClass
    private var patientId:String = ""
    private var userFhirPractitionerId:String = ""
    private var serviceRequestId:String = ""
    private lateinit var viewModel: ReferralDetailsViewModel
    private lateinit var formDataAdapter: FormDataAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentViewFormDetailsBinding.inflate(inflater, container, false)
        formatterClass = FormatterClass(requireContext())
        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientId = formatterClass.getSharedPref("", "patientId") ?: ""
        userFhirPractitionerId = formatterClass.getSharedPref("", "userFhirPractitionerId") ?: ""
        serviceRequestId = formatterClass.getSharedPref("", "serviceRequestId") ?: ""

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

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val formDataList = viewModel.getServiceRequest()

        formDataAdapter = FormDataAdapter(formDataList, requireContext())
        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        binding.recyclerView.adapter = formDataAdapter

        Log.e("^^^^^^","^^^^^")
        println("formDataList $formDataList")
        Log.e("^^^^^^","^^^^^")

    }

    override fun onDestroyView() {
        super.onDestroyView()
    }


}