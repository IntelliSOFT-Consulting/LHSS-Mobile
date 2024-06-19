package com.intellisoft.lhss

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class HospitalListReferralVisitFragment : Fragment() {

    companion object {
        fun newInstance() = HospitalListReferralVisitFragment()
    }

    private val viewModel: HospitalListReferralVisitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_hospital_list_referral_visit, container, false)
    }
}