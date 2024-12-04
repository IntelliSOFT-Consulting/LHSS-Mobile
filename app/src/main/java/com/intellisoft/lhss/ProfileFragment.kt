package com.intellisoft.lhss

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.intellisoft.lhss.ProfileViewModel
import com.intellisoft.lhss.databinding.FragmentProfileBinding
import com.intellisoft.lhss.shared.FormatterClass
import com.intellisoft.lhss.shared.Item
import com.intellisoft.lhss.shared.ItemAdapter

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

        getUserDetails()

        binding.btnSignOut.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment2_to_loginFragment)
            formatterClass.clearSharedPreferences("")
        }

    }

    private fun getUserDetails() {

        val userPhoneNumber = formatterClass.getSharedPref("","userPhoneNumber")
        val userId = formatterClass.getSharedPref("","userId")
        val userRole = formatterClass.getSharedPref("","userRole")
        val userFullName = formatterClass.getSharedPref("","userFullName")
        val userEmailNumber = formatterClass.getSharedPref("","userEmailNumber")

        val itemList = ArrayList<Item>()
        // Update UI with user details
        if (userPhoneNumber != null)
            itemList.add(Item("Phone Number", userPhoneNumber, R.drawable.ic_facility_telephone))
        if (userFullName!= null)
            itemList.add(Item("Full Name", userFullName, R.drawable.ic_facility_name))
        if (userEmailNumber!= null)
            itemList.add(Item("Email", userEmailNumber, R.drawable.ic_facility_email))

        // Set the adapter
        val adapter = ItemAdapter(itemList)
        binding.recyclerView.adapter = adapter

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}