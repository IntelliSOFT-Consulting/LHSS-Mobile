package com.intellisoftkenya.a24cblhss

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.fhir.FhirEngine
import com.intellisoft.lhss.clinical_info.shared.ClinicalChildAdapter
import com.intellisoftkenya.a24cblhss.databinding.FragmentClinicalViewEncountersBinding
import com.intellisoftkenya.a24cblhss.fhir.FhirApplication
import com.intellisoftkenya.a24cblhss.referrals.viewmodels.ReferralDetailsViewModel
import com.intellisoftkenya.a24cblhss.referrals.viewmodels.ReferralDetailsViewModelFactory
import com.intellisoftkenya.a24cblhss.shared.DbNavigationDetails
import com.intellisoftkenya.a24cblhss.shared.FormatterClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClinicalViewEncountersFragment : Fragment() {

    private var _binding: FragmentClinicalViewEncountersBinding? = null
    private val binding get() = _binding!!
    private lateinit var fhirEngine: FhirEngine
    private lateinit var formatterClass: FormatterClass
    private var patientId:String = ""
    private var carePlanId:String = ""
    private var encounterId:String = ""
    private var clinicalInfo:String? = null
    private lateinit var viewModel: ReferralDetailsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = FragmentClinicalViewEncountersBinding.inflate(inflater, container, false)
        formatterClass = FormatterClass(requireContext())

        patientId = formatterClass.getSharedPref("", "patientId")?: ""
        carePlanId = formatterClass.getSharedPref(DbNavigationDetails.CARE_PLAN.name,"carePlanId")?: ""
        encounterId = formatterClass.getSharedPref("","encounterId")?: ""

        clinicalInfo = formatterClass.getSharedPref("", "CLINICAL_REFERRAL")
        fhirEngine = FhirApplication.fhirEngine(requireContext())

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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Make sure the encounter is in the form of 'Encounter/...'

        CoroutineScope(Dispatchers.IO).launch {
            val encounter = "Encounter/$encounterId"
            val formData = viewModel.getEncounterObservationDetails(encounter)

            CoroutineScope(Dispatchers.Main).launch {
                binding.childRecyclerView.apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = formData?.formDataList?.let { ClinicalChildAdapter(it) }
                }
                binding.tvTitle.text = formData?.title?.let { formatterClass.toSentenceCase(it) }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}