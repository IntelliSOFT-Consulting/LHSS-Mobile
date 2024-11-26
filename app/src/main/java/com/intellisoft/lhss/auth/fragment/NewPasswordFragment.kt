package com.intellisoft.lhss.auth.fragment

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.intellisoft.lhss.R
import com.intellisoft.lhss.auth.viewmodel.NewPasswordViewModel

class NewPasswordFragment : Fragment() {

    companion object {
        fun newInstance() = NewPasswordFragment()
    }

    private val viewModel: NewPasswordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_new_password, container, false)
    }
}