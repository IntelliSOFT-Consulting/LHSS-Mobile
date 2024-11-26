package com.intellisoftkenya.a24cblhss

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intellisoftkenya.a24cblhss.databinding.FragmentLandingPageBinding
import com.intellisoftkenya.a24cblhss.shared.DbNavigationDetails
import com.intellisoftkenya.a24cblhss.shared.FormatterClass
import com.intellisoftkenya.a24cblhss.shared.LayoutListViewModel
import com.intellisoftkenya.a24cblhss.shared.LayoutsRecyclerViewAdapter

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLandingPageBinding.inflate(inflater, container, false)

        formatterClass = FormatterClass(requireContext())

        formatterClass.clearData()

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}