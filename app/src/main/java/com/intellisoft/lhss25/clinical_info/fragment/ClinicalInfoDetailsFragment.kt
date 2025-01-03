package com.intellisoft.lhss25.clinical_info.fragment

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.intellisoft.lhss25.R
import com.intellisoft.lhss25.clinical_info.viewmodel.ClinicalInfoDetailsViewModel

class ClinicalInfoDetailsFragment : Fragment() {

    companion object {
        fun newInstance() = ClinicalInfoDetailsFragment()
    }

    private val viewModel: ClinicalInfoDetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_clinical_info_details, container, false)
    }
}