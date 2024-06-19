package com.intellisoft.lhss

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class PreviewEnteredDataFragment : Fragment() {

    companion object {
        fun newInstance() = PreviewEnteredDataFragment()
    }

    private val viewModel: PreviewEnteredDataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_preview_entered_data, container, false)
    }
}