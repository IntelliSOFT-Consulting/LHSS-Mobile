package com.intellisoft.lhss25.refer_patient.fragment

import android.app.ProgressDialog
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.intellisoft.lhss25.R
import com.intellisoft.lhss25.databinding.FragmentReviewReferBinding
import com.intellisoft.lhss25.shared.DbClasses
import com.intellisoft.lhss25.shared.DbNavigationDetails
import com.intellisoft.lhss25.dynamic_components.FieldManager
import com.intellisoft.lhss25.shared.FormData
import com.intellisoft.lhss25.refer_patient.viewmodel.ReviewReferViewModel
import com.intellisoft.lhss25.shared.BlurBackgroundDialog
import com.intellisoft.lhss25.shared.FormDataAdapter
import com.intellisoft.lhss25.shared.FormatterClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ReviewReferFragment : Fragment() {

    private var _binding: FragmentReviewReferBinding? = null
    private val binding get() = _binding!!
    private lateinit var fieldManager: FieldManager


    private val viewModel: ReviewReferViewModel by viewModels()
    private lateinit var formatterClass: FormatterClass
    private lateinit var formDataAdapter: FormDataAdapter
    private var formDataList = ArrayList<FormData>()

    val navigationDetails = DbNavigationDetails.REFER_PATIENT.name
    val registrationClassesList = listOf(
        DbClasses.REFERRING_FACILITY_INFO.name,
        DbClasses.REFERRAL_INFO.name
    )
    private var patientId:String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentReviewReferBinding.inflate(inflater, container, false)
        formatterClass = FormatterClass(requireContext())

        patientId = formatterClass.getSharedPref("", "patientId") ?: ""

        navigationActions()


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
//            findNavController().navigate(R.id.action_reviewReferFragment_to_patientCardFragment)
        }
    }
    private fun submitData() {

        val requesterId = formatterClass.getSharedPref("", "userFhirPractitionerId")?: ""

        CoroutineScope(Dispatchers.Main).launch {

            val progressDialog = ProgressDialog(requireContext())
            progressDialog.setTitle("Please wait")
            progressDialog.setMessage("Referral in progress.")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            var savedResources = ArrayList<String>()

            val job = Job()
            CoroutineScope(Dispatchers.IO + job).launch {

                savedResources = ArrayList(viewModel.createServiceRequest(
                    formDataList, patientId, requesterId))

                registrationClassesList.forEach {
                    formatterClass.deleteSharedPref(navigationDetails, it)
                }

            }.join()

            progressDialog.dismiss()

            val blurBackgroundDialog = if (savedResources.isNotEmpty()){
                //Save was okay
                BlurBackgroundDialog(requireContext(),
                    "Referral has been completed Successfully.",
                    this@ReviewReferFragment,
                    R.id.action_reviewReferFragment_to_patientFileFragment
                )
            }else{
                //Save was not okay
                BlurBackgroundDialog(requireContext(),
                    "There was an issue with the referral request.",
                    this@ReviewReferFragment,
                    R.id.action_reviewReferFragment_to_patientCardFragment
                )
            }
            blurBackgroundDialog.show()



        }

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gson = Gson()

        registrationClassesList.forEach {
            val savedJson = formatterClass.getSharedPref(navigationDetails, it)
            val formDataFromJson = gson.fromJson(savedJson, FormData::class.java)
            formDataList.addAll(listOf(formDataFromJson))
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