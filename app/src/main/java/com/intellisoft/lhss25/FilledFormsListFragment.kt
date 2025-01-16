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
import com.intellisoft.lhss25.clinical_info.shared.ClinicalEncounterAdapter
import com.intellisoft.lhss25.databinding.FragmentEndTreatmentFormBinding
import com.intellisoft.lhss25.databinding.FragmentFilledFormsListBinding
import com.intellisoft.lhss25.fhir.FhirApplication
import com.intellisoft.lhss25.patient_details.FormFillsEncounterAdapter
import com.intellisoft.lhss25.referrals.viewmodels.ReferralDetailsViewModel
import com.intellisoft.lhss25.referrals.viewmodels.ReferralDetailsViewModelFactory
import com.intellisoft.lhss25.shared.FormDataAdapter
import com.intellisoft.lhss25.shared.FormatterClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class FilledFormsListFragment : Fragment() {

    private var _binding: FragmentFilledFormsListBinding? = null
    private val binding get() = _binding!!

    private lateinit var formatterClass: FormatterClass
    private var patientId:String = ""
    private lateinit var fhirEngine: FhirEngine
    private lateinit var viewModel: ReferralDetailsViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFilledFormsListBinding.inflate(inflater, container, false)
        fhirEngine = FhirApplication.fhirEngine(requireContext())


        formatterClass = FormatterClass(requireContext())

        patientId = formatterClass.getSharedPref("", "patientId")?: ""

        viewModel =
            ViewModelProvider(
                this,
                ReferralDetailsViewModelFactory(
                    requireContext().applicationContext as Application,
                    fhirEngine,
                    patientId,
                    ""
                ),
            )[ReferralDetailsViewModel::class.java]

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val formName = formatterClass.getSharedPref("","FORM_NAME")

        val formList  = formName?.let { viewModel.getFilledFormList(it) }
        val formDataAdapter = formList?.let {
            FormFillsEncounterAdapter(
                requireContext().applicationContext,
                this@FilledFormsListFragment,
                ArrayList(it)
            )
        }

        CoroutineScope(Dispatchers.Main).launch {
            binding.recyclerView.adapter = formDataAdapter
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Set binding to null to avoid memory leaks
        _binding = null
    }


}