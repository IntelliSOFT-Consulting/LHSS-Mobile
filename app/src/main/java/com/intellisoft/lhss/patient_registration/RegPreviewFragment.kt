package com.intellisoft.lhss.patient_registration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.intellisoft.lhss.R
import com.intellisoft.lhss.databinding.FragmentPatientLocationBinding
import com.intellisoft.lhss.databinding.FragmentRegPreviewBinding


class RegPreviewFragment : Fragment() {

    private lateinit var binding: FragmentRegPreviewBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentRegPreviewBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

}