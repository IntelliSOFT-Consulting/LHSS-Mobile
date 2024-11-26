package com.intellisoft.lhss.registration.fragment

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.intellisoft.lhss.R
import com.intellisoft.lhss.databinding.FragmentAddressBinding
import com.intellisoft.lhss.shared.DbClasses
import com.intellisoft.lhss.shared.DbField
import com.intellisoft.lhss.shared.DbNavigationDetails
import com.intellisoft.lhss.shared.DbWidgets
import com.intellisoft.lhss.dynamic_components.DefaultLabelCustomizer
import com.intellisoft.lhss.dynamic_components.FieldManager
import com.intellisoft.lhss.shared.FormData
import com.intellisoft.lhss.dynamic_components.FormUtils
import com.intellisoft.lhss.registration.viewmodel.AddressViewModel
import com.intellisoft.lhss.shared.FormatterClass
import com.intellisoft.lhss.shared.MainActivityViewModel

class AddressFragment : Fragment() {

    private var _binding: FragmentAddressBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddressViewModel by viewModels()

    private lateinit var fieldManager: FieldManager
    private var countryOriginList = listOf(
        "Djibouti", "Eritrea", "Kenya", "Ethiopia", "Somalia", "South Sudan", "Sudan", "Uganda")

    private lateinit var formatterClass: FormatterClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAddressBinding.inflate(inflater, container, false)

        navigationActions()
        formatterClass = FormatterClass(requireContext())

        val workflowTitles = formatterClass.getWorkflowTitles(DbClasses.ADDRESS.name)
        if (workflowTitles != null){
            binding.tvTitle.text = formatterClass.toSentenceCase(workflowTitles.text)
            binding.imgBtn.setImageResource(workflowTitles.iconId)
        }

        return binding.root

    }

    private fun navigationActions() {
        // Set the next button text to "Continue" and add click listeners
        val navigationButtons = binding.navigationButtons
        navigationButtons.setNextButtonText("Next")

        navigationButtons.setBackButtonClickListener {
            // Handle back button click
            findNavController().navigateUp()
        }

        navigationButtons.setNextButtonClickListener {
            // Handle next button click
            // Navigate to the next fragment or perform any action
            // Call the function to extract form data
            val (addedFields, missingFields) = FormUtils.extractAllFormData(binding.rootLayout)

            if (missingFields.isNotEmpty()){
                var missingText = ""
                missingFields.forEach { missingText += "\n ${it.tag}, " }

                val mandatoryText = "The following are mandatory fields and " +
                        "need to be filled before proceeding: \n" +
                        missingText

                formatterClass.showDialog("Missing Content", mandatoryText)
            }else{
                findNavController().navigate(R.id.action_addressFragment_to_nextOfKinFragment)
                val formData = FormData(
                    DbClasses.ADDRESS.name,
                    addedFields)

                val gson = Gson()
                val json = gson.toJson(formData)

                formatterClass.saveSharedPref(
                    sharedPrefName = DbNavigationDetails.PATIENT_REGISTRATION.name,
                    DbClasses.ADDRESS.name,
                    json
                )

            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize FieldManager with dependencies (inject via constructor or manually)
        fieldManager = FieldManager(DefaultLabelCustomizer(), requireContext())

        val dbFieldList = listOf(
            DbField(
                DbWidgets.SPINNER.name,
                "Country of Origin", true, null,
                countryOriginList),
            DbField(
                DbWidgets.SPINNER.name,
                "Country of Residence", true, null,
                countryOriginList),
            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Residential Address in Referring Country", true,
                InputType.TYPE_CLASS_TEXT),

            DbField(
                DbWidgets.EDIT_TEXT.name,
                "Residential Address in Receiving Country", true,
                InputType.TYPE_CLASS_TEXT),
        )

        FormUtils.populateView(ArrayList(dbFieldList), binding.rootLayout, fieldManager, requireContext())

//        viewModel.extractFormData(binding.rootLayout)

        FormUtils.loadFormData(
            requireContext(),
            binding.rootLayout,
            DbNavigationDetails.PATIENT_REGISTRATION.name,
            DbClasses.ADDRESS.name
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}