package com.intellisoft.lhss

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.search.search
import com.intellisoft.lhss.clinical_info.viewmodel.ClinicalInfoDetailsViewModel
import com.intellisoft.lhss.databinding.FragmentLandingPageBinding
import com.intellisoft.lhss.fhir.FhirApplication
import com.intellisoft.lhss.shared.DbNavigationDetails
import com.intellisoft.lhss.shared.FormatterClass
import com.intellisoft.lhss.shared.LayoutListViewModel
import com.intellisoft.lhss.shared.LayoutsRecyclerViewAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Location

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LandingPageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LandingPageFragment : Fragment() {
    private val layoutViewModel: LayoutListViewModel by viewModels()

    private var _binding: FragmentLandingPageBinding? = null
    private val binding get() = _binding!!

    private lateinit var formatterClass: FormatterClass
    private lateinit var fhirEngine: FhirEngine
    private lateinit var locationViewModel: LocationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLandingPageBinding.inflate(inflater, container, false)
        fhirEngine = FhirApplication.fhirEngine(requireContext())

        formatterClass = FormatterClass(requireContext())

        formatterClass.clearData()

        locationViewModel =
            ViewModelProvider(
                this,
                LocationViewModel.LocationViewModelFactory(
                    requireActivity().application,
                    fhirEngine
                ),
            )[LocationViewModel::class.java]

        return binding.root
    }



    private fun onItemClick(layout: LayoutListViewModel.Layout) {
        when (layout.textId) {
            "Search Patient" -> {
                findNavController().navigate(R.id.patientListFragment)
            }
            "Referrals" -> {
                findNavController().navigate(R.id.referralPatientList)
            }

            "Register Patient" -> {
                findNavController().navigate(R.id.demographicsFragment)
            }
            "Notifications" -> {
                findNavController().navigate(R.id.notificationFragment)
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

        val userFacility = formatterClass.getSharedPref("","userFacility") ?: "Facility"

        val userFullName = formatterClass.getSharedPref("","userFullName")

        binding.tvFacilityName.text = userFullName?.lowercase() ?: ""


//        CoroutineScope(Dispatchers.IO).launch {
//
//            val locationDetails =
//                locationViewModel.getLocationDetails("Location/Kachumbala-Health-Centre-IV")
//
//            locationDetails.forEach {
//                Log.e("------->","<--------")
//                println("name ${it.name}")
//                println("code ${it.code}")
//                println("partOf ${it.partOf}")
//                Log.e("------->","<--------")
//
//            }
//
//        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}