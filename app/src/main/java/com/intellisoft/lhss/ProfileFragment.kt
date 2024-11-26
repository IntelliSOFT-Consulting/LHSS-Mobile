package com.intellisoftkenya.a24cblhss

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.intellisoftkenya.a24cblhss.databinding.FragmentPatientCardBinding
import com.intellisoftkenya.a24cblhss.databinding.FragmentProfileBinding
import com.intellisoftkenya.a24cblhss.shared.FormatterClass
import com.intellisoftkenya.a24cblhss.shared.Item
import com.intellisoftkenya.a24cblhss.shared.ItemAdapter

class ProfileFragment : Fragment() {


    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var formatterClass: FormatterClass
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        formatterClass = FormatterClass(requireContext())

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Create sample data
        val itemList = listOf(
            Item("Facility Name", "Tanaka", R.drawable.ic_facility_name),
            Item("Telephone", "0712345678", R.drawable.ic_facility_telephone),
            Item("Email", "tanakafacility@tanaka.com", R.drawable.ic_facility_email)
        )

        // Set the adapter
        val adapter = ItemAdapter(itemList)
        binding.recyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}