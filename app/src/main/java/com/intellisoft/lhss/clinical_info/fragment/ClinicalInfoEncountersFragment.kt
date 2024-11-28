package com.intellisoft.lhss.clinical_info.fragment

import android.app.Application
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
import com.intellisoft.lhss.clinical_info.shared.ClinicalEncounterAdapter
import com.intellisoft.lhss.databinding.FragmentClinicalInfoEncountersBinding
import com.intellisoft.lhss.fhir.Constants
import com.intellisoft.lhss.fhir.FhirApplication
import com.intellisoft.lhss.referrals.viewmodels.ReferralDetailsViewModel
import com.intellisoft.lhss.referrals.viewmodels.ReferralDetailsViewModelFactory
import com.intellisoft.lhss.shared.DbClasses
import com.intellisoft.lhss.shared.DbNavigationDetails
import com.intellisoft.lhss.shared.FormatterClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClinicalInfoEncountersFragment : Fragment() {

    private var _binding: FragmentClinicalInfoEncountersBinding? = null
    private val binding get() = _binding!!
    private lateinit var fhirEngine: FhirEngine
    private lateinit var formatterClass: FormatterClass
    private var patientId:String = ""
    private var carePlanId:String = ""
    private var carePlanStatus:String = ""
    private var clinicalInfo:String? = null
    private lateinit var viewModel: ReferralDetailsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentClinicalInfoEncountersBinding.inflate(inflater, container, false)
        formatterClass = FormatterClass(requireContext())
        patientId = formatterClass.getSharedPref("", "patientId")?: ""
        carePlanId = formatterClass.getSharedPref(DbNavigationDetails.CARE_PLAN.name,"carePlanId")?: ""
        carePlanStatus = formatterClass.getSharedPref(DbNavigationDetails.CARE_PLAN.name,"carePlanStatus")?: ""
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

        CoroutineScope(Dispatchers.IO).launch {

            val fhirCode = Constants.RECEIVING_FACILITY_NAME
            val tbRegistration = viewModel.getObservationCode(fhirCode)
            if (tbRegistration!= null) {
                formatterClass.saveSharedPref("","facilityName", tbRegistration.text)
            }


            val encounterList =  viewModel.getEncounterList(carePlanId)
            encounterList.removeIf{ it.referralReason != clinicalInfo }

            val formDataAdapter = ClinicalEncounterAdapter(
                requireActivity().applicationContext,
                this@ClinicalInfoEncountersFragment,
                clinicalInfo,
                encounterList)
            CoroutineScope(Dispatchers.Main).launch {
                binding.recyclerView.layoutManager = LinearLayoutManager(context)
                binding.recyclerView.adapter = formDataAdapter

                if(carePlanStatus == "COMPLETED") {
                    binding.btnAdd.visibility = View.GONE
                }

            }
        }

        binding.btnAdd.setOnClickListener {
            if (clinicalInfo != null) {
                if (clinicalInfo == DbClasses.TB_TREATMENT.name ||
                    clinicalInfo == DbClasses.HIV_STATUS_TREATMENT.name) {
                    findNavController().navigate(
                        R.id.action_clinicalInfoEncountersFragment_to_clinicalInfoFormI_IIFragment)
                }
                if (clinicalInfo == DbClasses.LABORATORY_RESULTS.name ||
                    clinicalInfo == DbClasses.DST.name ||
                    clinicalInfo == DbClasses.DR_TB_FOLLOW_UP_TEST.name) {
                    findNavController().navigate(
                        R.id.action_clinicalInfoEncountersFragment_to_clinicalInfoFormIII_IVFragment)
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}