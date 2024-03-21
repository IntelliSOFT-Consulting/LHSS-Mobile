package com.intellisoft.lhss

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.intellisoft.lhss.databinding.FragmentPractionerDetailsBinding
import com.intellisoft.lhss.fhir.data.FormatterClass
import com.intellisoft.lhss.shared.Login

class PractionerDetails : Fragment() {

    private lateinit var _binding: FragmentPractionerDetailsBinding
    private val binding get() = _binding

    private val formatterClass = FormatterClass()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPractionerDetailsBinding.inflate(inflater, container, false)

        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = resources.getString(R.string.app_name)
            setDisplayHomeAsUpEnabled(true)
        }
        getUserDetails()

        binding.btnSignOut.setOnClickListener {

            formatterClass.saveSharedPref("isLoggedIn", "false", requireContext())

            val list = mutableListOf("practitionerFullNames","practitionerFacility")
            formatterClass.deleteUserDetails(requireContext(), ArrayList(list))

            val intent = Intent(requireContext(),  Login::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)

        }
        binding.bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    true
                }



                else -> false
            }
        }


    }

    private fun getUserDetails() {

        val practitionerFullNames = formatterClass.getSharedPref("practitionerFullNames", requireContext())
        val practitionerIdNumber = formatterClass.getSharedPref("practitionerIdNumber", requireContext())
        val practitionerRole = formatterClass.getSharedPref("practitionerRole", requireContext())
        val fhirPractitionerId = formatterClass.getSharedPref("fhirPractitionerId", requireContext())
        val practitionerId = formatterClass.getSharedPref("practitionerId", requireContext())
        val practitionerEmail = formatterClass.getSharedPref("practitionerEmail", requireContext())
        val practitionerPhone = formatterClass.getSharedPref("practitionerPhone", requireContext())

        binding.tvEmailAddress.text = practitionerEmail
        binding.tvPhoneNumber.text = practitionerPhone
        binding.tvIdNumber.text = practitionerIdNumber
        binding.tvFullName.text = practitionerFullNames

    }
}