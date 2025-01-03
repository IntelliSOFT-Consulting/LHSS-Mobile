package com.intellisoft.lhss25.auth.fragment

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.intellisoft.lhss25.R
import com.intellisoft.lhss25.databinding.FragmentRecoverPasswordBinding
import com.intellisoft.lhss25.auth.viewmodel.RecoverPasswordViewModel


class RecoverPasswordFragment : Fragment() {

    // ViewBinding instance for this fragment
    private var _binding: FragmentRecoverPasswordBinding? = null
    private val binding get() = _binding!! // This is safe to use after onCreateView


    private val viewModel: RecoverPasswordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Initialize the binding object
        _binding = FragmentRecoverPasswordBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRecoverPassword.setOnClickListener {
            findNavController().navigate(R.id.action_recoverPasswordFragment_to_newPasswordFragment)

        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        // Set binding to null to avoid memory leaks
        _binding = null
    }
}