package com.intellisoft.lhss.shared

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.intellisoft.lhss.databinding.FragmentNewPasswordBinding


class NewPasswordFragment : Fragment() {

    private lateinit var binding: FragmentNewPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentNewPasswordBinding.inflate(inflater, container, false)

        return binding.root

    }

}