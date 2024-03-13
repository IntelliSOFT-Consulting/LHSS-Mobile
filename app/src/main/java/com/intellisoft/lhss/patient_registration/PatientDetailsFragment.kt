package com.intellisoft.lhss.patient_registration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

import com.intellisoft.lhss.R
import com.intellisoft.lhss.databinding.FragmentPatientDetailsBinding
import com.intellisoft.lhss.databinding.FragmentPatientLocationBinding

class PatientDetailsFragment : Fragment() {

    private lateinit var binding: FragmentPatientDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPatientDetailsBinding.inflate(layoutInflater)

        return binding.root    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nextButton.setOnClickListener{
            findNavController().navigate(R.id.patientLocationFragment)
        }
    }





}