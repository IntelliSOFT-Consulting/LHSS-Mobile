package com.intellisoft.lhss.shared

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.lhss.R
import com.intellisoft.lhss.add_patient.AddPatientFragment
import com.intellisoft.lhss.databinding.FragmentLandingPageBinding
import com.intellisoft.lhss.fhir.data.FormatterClass
import com.intellisoft.lhss.viewmodel.LayoutListViewModel
import com.intellisoft.lhss.viewmodel.LayoutsRecyclerViewAdapter
import timber.log.Timber


class LandingPage : Fragment() {
    private val layoutViewModel: LayoutListViewModel by viewModels()
    private lateinit var viewModel: LandingPageViewModel
    private lateinit var _binding: FragmentLandingPageBinding
    private val binding get() = _binding
    private var formatterClass = FormatterClass()

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

        formatterClass.deleteSharedPref("patientListAction", requireContext())

        binding.btnSearch.setOnClickListener {
            findNavController().navigate(R.id.patient_list)
        }

        return _binding.root

    }

    private fun onItemClick(layout: LayoutListViewModel.Layout) {
        Timber.e("***** ${layout.textId}")
        when (layout.textId) {
            "Search Patient" -> {
                findNavController().navigate(R.id.patient_list)
            }
            "Referrals" -> {
                findNavController().navigate(R.id.patient_list)
            }

            "Register Patient" -> {
                val bundle = Bundle()
                bundle.putString(
                    AddPatientFragment.QUESTIONNAIRE_FILE_PATH_KEY,
                    "new-patient-registration-paginated.json"
                )
                findNavController().navigate(R.id.addPatientFragment, bundle)
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