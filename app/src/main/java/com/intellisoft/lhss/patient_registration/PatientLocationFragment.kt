package com.intellisoft.lhss.patient_registration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.intellisoft.lhss.R
import com.intellisoft.lhss.databinding.ActivityPatientRegistrationBinding
import com.intellisoft.lhss.databinding.FragmentPatientLocationBinding

class PatientLocationFragment : Fragment() {

    private lateinit var binding: FragmentPatientLocationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentPatientLocationBinding.inflate(layoutInflater)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nextButton.setOnClickListener{
            findNavController().navigate(R.id.regPreviewFragment)
        }

    }

}