package com.intellisoft.lhss.registration

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.intellisoft.lhss.R
import com.intellisoft.lhss.viewmodels.RegisterPatientPreviewViewModel

class RegisterPatientPreviewFragment : Fragment() {

    companion object {
        fun newInstance() = RegisterPatientPreviewFragment()
    }

    private val viewModel: RegisterPatientPreviewViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_register_patient_preview, container, false)
    }
}