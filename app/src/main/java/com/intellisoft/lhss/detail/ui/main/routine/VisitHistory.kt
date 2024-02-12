package com.intellisoft.lhss.detail.ui.main.routine

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.intellisoft.lhss.MainActivity
import com.intellisoft.lhss.databinding.FragmentVisitHistoryBinding
import com.intellisoft.lhss.fhir.FhirApplication
import com.intellisoft.lhss.fhir.data.FormatterClass
import com.intellisoft.lhss.fhir.data.NavigationDetails
import com.intellisoft.lhss.viewmodel.PatientDetailsViewModel
import com.intellisoft.lhss.viewmodel.PatientDetailsViewModelFactory


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [VisitHistory.newInstance] factory method to
 * create an instance of this fragment.
 */
class VisitHistory : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentVisitHistoryBinding
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine
    private val formatterClass = FormatterClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentVisitHistoryBinding.inflate(inflater, container, false)

        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientId = formatterClass.getSharedPref("patientId", requireContext()).toString()

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModelFactory(requireContext().applicationContext as Application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        getVisitHistory()

        binding.floatingActionButton.setOnClickListener {
            FormatterClass().saveSharedPref(
                "questionnaireJson",
                "add_visit.json",
                requireContext()
            )

            //Send to contraindications
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.putExtra("functionToCall", NavigationDetails.VISIT_HISTORY.name)
            intent.putExtra("patientId", patientId)
            startActivity(intent)
        }

        return binding.root

    }

    private fun getVisitHistory() {


    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment VisitHistory.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            VisitHistory().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}