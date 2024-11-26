package com.intellisoft.lhss.referrals.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.fhir.FhirEngine
import com.google.gson.Gson
import com.intellisoft.lhss.referrals.viewmodels.AcknoledgementDetailsViewModel
import com.intellisoft.lhss.R
import com.intellisoft.lhss.databinding.FragmentAcknoledgementDetailsBinding
import com.intellisoft.lhss.shared.DbClasses
import com.intellisoft.lhss.shared.DbNavigationDetails
import com.intellisoft.lhss.dynamic_components.FieldManager
import com.intellisoft.lhss.fhir.FhirApplication
import com.intellisoft.lhss.shared.BlurBackgroundDialog
import com.intellisoft.lhss.shared.FormData
import com.intellisoft.lhss.shared.FormDataAdapter
import com.intellisoft.lhss.shared.FormatterClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AcknoledgementDetailsFragment : Fragment() {

    private var _binding: FragmentAcknoledgementDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var fieldManager: FieldManager

    private lateinit var viewModel: AcknoledgementDetailsViewModel

    private var formDataList = ArrayList<FormData>()
    private lateinit var formDataAdapter: FormDataAdapter
    private lateinit var fhirEngine: FhirEngine
    private lateinit var formatterClass: FormatterClass
    private var patientId:String = ""
    private var serviceRequestId:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAcknoledgementDetailsBinding.inflate(inflater, container, false)

        navigationActions()

        formatterClass = FormatterClass(requireContext())

        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientId = formatterClass.getSharedPref("", "patientId") ?: ""
        serviceRequestId = formatterClass.getSharedPref("", "serviceRequestId") ?: ""

        viewModel =
            ViewModelProvider(
                this,
                AcknoledgementDetailsViewModel.AcknoledgementDetailsViewModelFactory(
                    requireActivity().application,
                    patientId,
                    serviceRequestId
                ),
            )[AcknoledgementDetailsViewModel::class.java]

        return binding.root
    }

    private fun navigationActions() {
        // Set the next button text to "Continue" and add click listeners
        val navigationButtons = binding.navigationButtons
        navigationButtons.setNextButtonText("Submit")

        navigationButtons.setBackButtonClickListener {
            // Handle back button click
            findNavController().navigateUp()
        }

        navigationButtons.setNextButtonClickListener {
            // Handle next button click
            // Navigate to the next fragment or perform any action
            submitData()
        }
    }

    private fun submitData() {

        CoroutineScope(Dispatchers.Main).launch {

            viewModel.createAcknowledgementDocumentResource(formDataList)

            val blurBackgroundDialog = //Save was okay
                BlurBackgroundDialog(requireContext(),
                    "Acknowledgement has been completed Successfully.",
                    this@AcknoledgementDetailsFragment,
                    R.id.action_acknoledgementDetailsFragment_to_patientCardFragment
                )

            blurBackgroundDialog.show()

        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gson = Gson()

        val navigationDetails = DbNavigationDetails.REFERRALS.name
        val registrationClassesList = listOf(
            DbClasses.ACKNOWLEDGEMENT_FORM.name
        )
        registrationClassesList.forEach {
            val savedJson = formatterClass.getSharedPref(navigationDetails, it)
            if (savedJson != null){
                val formDataFromJson = gson.fromJson(savedJson, FormData::class.java)
                if (formDataFromJson != null){
                    formDataList.addAll(listOf(formDataFromJson))
                }
            }

        }

        formDataAdapter = FormDataAdapter(formDataList, requireContext())
        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        binding.recyclerView.adapter = formDataAdapter

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}