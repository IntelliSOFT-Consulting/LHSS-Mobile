package com.intellisoft.lhss.shared

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Constraints
import com.google.android.fhir.sync.PeriodicSyncConfiguration
import com.google.android.fhir.sync.RepeatInterval
import com.google.android.fhir.sync.Sync
import com.google.android.fhir.sync.SyncJobStatus
import com.intellisoft.lhss.R
import com.intellisoft.lhss.add_patient.AddPatientFragment
import com.intellisoft.lhss.databinding.FragmentLandingPageBinding
import com.intellisoft.lhss.fhir.data.FhirSyncWorker
import com.intellisoft.lhss.fhir.data.FormatterClass
import com.intellisoft.lhss.viewmodel.LayoutListViewModel
import com.intellisoft.lhss.viewmodel.LayoutsRecyclerViewAdapter
import com.intellisoft.lhss.viewmodel.MainActivityViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit


class LandingPage : Fragment() {
    private val layoutViewModel: LayoutListViewModel by viewModels()
    private lateinit var viewModel: LandingPageViewModel
    private lateinit var _binding: FragmentLandingPageBinding
    private val binding get() = _binding
    private var formatterClass = FormatterClass()
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentLandingPageBinding.inflate(inflater, container, false)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setHomeButtonEnabled(false)
            setHomeAsUpIndicator(null)
        }

        formatterClass.deleteSharedPref("lhssFlow", requireContext())

        formatterClass.deleteSharedPref("patientListAction", requireContext())
        formatterClass.deleteSharedPref("isPatientUpdateBack", requireContext())

        binding.btnSearch.setOnClickListener {
            findNavController().navigate(R.id.patient_list)
        }

        val practitionerFacility = formatterClass.getSharedPref("practitionerFacility", requireContext())
        if (practitionerFacility != null){
            binding.tvName.text = practitionerFacility
        }

        lifecycleScope.launch {
            mainActivityViewModel.pollState.collect {
                Timber.d("onViewCreated: pollState Got status $it")
                when (it) {
                    is SyncJobStatus.Started -> {
                        Timber.i("Sync: ${it::class.java.simpleName}")
                    }

                    is SyncJobStatus.InProgress -> {
                        Timber.i("Sync: ${it::class.java.simpleName} with data $it")
                    }

                    is SyncJobStatus.Finished -> {
                        Timber.i("Sync: ${it::class.java.simpleName} at ${it.timestamp}")
                        mainActivityViewModel.updateLastSyncTimestamp()

                    }

                    is SyncJobStatus.Failed -> {
                        Timber.i("Sync: ${it::class.java.simpleName} at ${it.timestamp}")
                        mainActivityViewModel.updateLastSyncTimestamp()

                    }

                    else -> {
                        Timber.i("Sync: Unknown state.")
                        mainActivityViewModel.updateLastSyncTimestamp()

                    }
                }
            }
        }



        return _binding.root

    }

    private fun onItemClick(layout: LayoutListViewModel.Layout) {
        Timber.e("***** ${layout.textId}")
        when (layout.textId) {
            "Search Patient" -> {
                findNavController().navigate(R.id.patient_list)
                formatterClass.deleteSharedPref("registrationFlowPersonal", requireContext())
                formatterClass.deleteSharedPref("registrationFlowAdministrative", requireContext())
                formatterClass.deleteSharedPref("isPatientUpdate", requireContext())
            }
            "Referrals" -> {
                findNavController().navigate(R.id.fragmentReferrals)
                formatterClass.saveSharedPref("lhssFlow", "referralFlow", requireContext())
            }

            "Register Patient" -> {
                findNavController().navigate(R.id.patientDetailsFragment)
            }

        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter =
            LayoutsRecyclerViewAdapter(::onItemClick).apply { submitList(layoutViewModel.getLayoutList()) }
        val recyclerView = requireView().findViewById<RecyclerView>(R.id.sdcLayoutsRecyclerView)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(context, 2)


    }


}