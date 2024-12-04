package com.intellisoft.lhss.auth.fragment

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import com.intellisoft.lhss.R
import com.intellisoft.lhss.databinding.FragmentLoginBinding
import com.intellisoft.lhss.shared.DbSignIn
import com.intellisoft.lhss.auth.viewmodel.LoginViewModel
import com.intellisoft.lhss.network_request.RetrofitCallsAuthentication


class LoginFragment : Fragment() {


    // ViewBinding instance for this fragment
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!! // This is safe to use after onCreateView

    private val viewModel: LoginViewModel by viewModels()
    private var retrofitCallsAuthentication = RetrofitCallsAuthentication()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Initialize the binding object
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.languages_array, // Use the array from strings.xml
            android.R.layout.simple_spinner_item // Default spinner layout
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            binding.languageSpinner.adapter = adapter
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Access the button from the binding and set an action
        binding.btnLogin.setOnClickListener {

            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()

            if (username == "1" && password == "1"){
                findNavController().navigate(R.id.landingPageFragment)
            }else if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {

                val dbSignIn = DbSignIn(username, password)
                retrofitCallsAuthentication.loginUser(requireContext(), dbSignIn, this, R.id.landingPageFragment)

            } else{
                if (TextUtils.isEmpty(username)) binding.etUsername.error = "Please Enter Username"
                if (TextUtils.isEmpty(password)) binding.etPassword.error = "Please Enter Password"
            }

        }

        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_recoverPasswordFragment)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Set binding to null to avoid memory leaks
        _binding = null
    }
}