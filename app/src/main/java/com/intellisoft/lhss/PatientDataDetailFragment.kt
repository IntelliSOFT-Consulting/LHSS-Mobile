package com.intellisoft.lhss

import android.app.Application
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.google.gson.Gson
import com.intellisoft.lhss.databinding.FragmentPatientDataDetailBinding
import com.intellisoft.lhss.detail.ui.main.UpdateFragment
import com.intellisoft.lhss.detail.ui.main.adapters.PatientDetailDataAdapter
import com.intellisoft.lhss.fhir.FhirApplication
import com.intellisoft.lhss.fhir.data.CustomPatient
import com.intellisoft.lhss.fhir.data.DbAdministrative
import com.intellisoft.lhss.fhir.data.DbPatientDataDetails
import com.intellisoft.lhss.fhir.data.FormatterClass
import com.intellisoft.lhss.fhir.data.NavigationDetails
import com.intellisoft.lhss.utils.AppUtils
import com.intellisoft.lhss.viewmodel.PatientDetailsViewModel
import com.intellisoft.lhss.viewmodel.PatientDetailsViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class PatientDataDetailFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentPatientDataDetailBinding

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine
    private val formatterClass = FormatterClass()
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private var customPatient = CustomPatient("","","","","","", "")
    private var dbAdministrative = DbAdministrative("","","","","", "","")
    private var country :String? = null

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

        binding = FragmentPatientDataDetailBinding.inflate(inflater, container, false)
        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientId = formatterClass.getSharedPref("patientId", requireContext()).toString()

        layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setHasFixedSize(true)

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModelFactory(requireContext().applicationContext as Application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setHasFixedSize(true)

        binding.btnAddressDetails.setOnClickListener {
            val patientDataDetailsList = patientDetailsViewModel.getAddressDetails()
            showCustomDialog(patientDataDetailsList)
        }

        binding.btnVisitHistory.setOnClickListener {
//            val intent = Intent(requireContext(), MainActivity::class.java)
//            intent.putExtra("functionToCall", NavigationDetails.VISIT_HISTORY.name)
//            intent.putExtra("patientId", patientId)
//            startActivity(intent)

            findNavController().navigate(R.id.visitHistory)
        }

        binding.btnReferrals.setOnClickListener {
//            val intent = Intent(requireContext(), MainActivity::class.java)
//            intent.putExtra("functionToCall", NavigationDetails.REFERRAL_LIST.name)
//            intent.putExtra("patientId", patientId)
//            startActivity(intent)

            findNavController().navigate(R.id.referralsFragment)
        }

        binding.imgBtnBack.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.putExtra("functionToCall", NavigationDetails.CLIENT_LIST.name)
            intent.putExtra("patientId", patientId)
            startActivity(intent)
        }

        binding.imgBtnOptions.setOnClickListener {
            showPopupMenu(it)
        }

        getPatientDetails()

        return binding.root

    }
    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.patient_actions, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_item_edit_person -> {
                    // Action for menu item 1
                    CoroutineScope(Dispatchers.IO).launch {
                        formatterClass.saveSharedPref("isPatientUpdate","true", requireContext())
                    }

                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.putExtra("functionToCall", NavigationDetails.REGISTER_VACCINE.name)
                    intent.putExtra("patientId", patientId)
                    startActivity(intent)
                    true
                }
                R.id.menu_item_add_visit -> {
                    // Action for menu item 2
                    if (country != null){
                        if (country == "Ethiopia"){
                            FormatterClass().saveSharedPref(
                                "questionnaireJson",
                                "add-visit-ethiopia.json",
                                requireContext()
                            )
                        }
                        if (country == "Djibouti"){
                            FormatterClass().saveSharedPref(
                                "questionnaireJson",
                                "add-visit-djibouti.json",
                                requireContext()
                            )
                        }

                        FormatterClass().saveSharedPref(
                            "title",
                            "New Visit",
                            requireContext()
                        )
                        FormatterClass().saveSharedPref(
                            "lhssFlow",
                            "NEW_VISIT",
                            requireContext()
                        )
                        //Send to contraindications
                        administerVaccine()
                    }else{
                        Toast.makeText(requireContext(), "The client does not have a country associated with him/her", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.menu_item_refer_patient -> {
                    // Action for menu item 2
                    if (country != null){
                        if (country == "Ethiopia"){
                            FormatterClass().saveSharedPref(
                                "questionnaireJson",
                                "referral-form-ethiopia.json",
                                requireContext()
                            )
                        }
                        if (country == "Djibouti"){
                            FormatterClass().saveSharedPref(
                                "questionnaireJson",
                                "referral-form-djibouti.json",
                                requireContext()
                            )
                        }
                        FormatterClass().saveSharedPref(
                            "title",
                            "Referrals",
                            requireContext()
                        )
                        FormatterClass().saveSharedPref(
                            "lhssFlow",
                            "REFERRALS",
                            requireContext()
                        )
                        //Send to contraindications
                        administerVaccine()
                    }else{
                        Toast.makeText(requireContext(), "The client does not have a country associated with him/her", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                // Add more cases for other menu items if needed
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun administerVaccine() {
        val questionnaireJson = formatterClass.getSharedPref("questionnaireJson", requireContext())
        formatterClass.saveSharedPref("patientId", patientId, requireContext())

        val bundle = Bundle()
        bundle.putString(UpdateFragment.QUESTIONNAIRE_FRAGMENT_TAG, questionnaireJson)
        bundle.putString("patientId", patientId)
        findNavController().navigate(R.id.administerVaccine, bundle)

    }


    private fun showCustomDialog(patientDataDetailsList: ArrayList<DbPatientDataDetails>) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.address_details_dialog)
        dialog.setTitle("")

        // Set dialog width to match parent (full width)
        // Set dialog width to match parent (full width)
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window!!.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        dialog.window!!.attributes = layoutParams

        // Find views within the dialog layout
        val closeButton = dialog.findViewById<Button>(R.id.btnClose)
        val recyclerView = dialog.findViewById<RecyclerView>(R.id.recyclerView)
        val layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)

        val visitHistoryAdapter = PatientDetailDataAdapter(patientDataDetailsList, requireContext())
        recyclerView.adapter = visitHistoryAdapter


        // Set click listener for close button
        closeButton.setOnClickListener { // Close the dialog when close button is clicked
            dialog.dismiss()
        }
        dialog.show()
    }


    private fun getPatientDetails() {

        CoroutineScope(Dispatchers.IO).launch {

            val patientDetail = patientDetailsViewModel.getPatientInfo()
            val fullName = patientDetail.name
            val gender = patientDetail.gender
            val dateBirth = patientDetail.dob

            val identificationType = patientDetail.docType
            val identificationNumber = patientDetail.docId
            val occupationType = patientDetail.occupation
            val originCountry = patientDetail.originCountry
            val residenceCountry = patientDetail.residenceCountry
            val region = patientDetail.region
            val district = patientDetail.district

            CoroutineScope(Dispatchers.Main).launch {

                binding.apply {
                    tvNameDetails.text = fullName
                    tvName.text = fullName
                    tvGender.text = AppUtils().capitalizeFirstLetter(gender)
                    tvSystemId.text = patientDetail.systemId

                    val dob = formatterClass.convertDateFormat(dateBirth)
                    val age = formatterClass.getFormattedAge(dateBirth,tvAge.context.resources)

                    tvDob.text = dob
                    tvAge.text = "$age old"

                }
            }

            val parts = fullName.split(" ")
            if (parts.isNotEmpty()){
                val firstName = parts.getOrNull(0) ?: ""
                val middleNameParts = parts.subList(1, parts.size - 1)
                val middleName = if (middleNameParts.isNotEmpty()) middleNameParts.joinToString(" ") else ""
                val lastName = parts.last()

                customPatient = CustomPatient(
                    firstName,
                    middleName,
                    lastName,
                    gender,
                    dateBirth,
                    "",
                    "")

                dbAdministrative = DbAdministrative(
                    identificationType, identificationNumber, occupationType, originCountry, residenceCountry, region, district
                )
                formatterClass.saveSharedPref("registrationFlowPersonal", Gson().toJson(customPatient), requireContext())
                formatterClass.saveSharedPref("registrationFlowAdministrative", Gson().toJson(dbAdministrative), requireContext())

            }



            country = formatterClass.getSharedPref("country", requireContext())

            val patientDataDetailsList = patientDetailsViewModel.getUserDetails(requireContext())

            CoroutineScope(Dispatchers.Main).launch {
                val visitHistoryAdapter = PatientDetailDataAdapter(patientDataDetailsList, requireContext())
                binding.recyclerView.adapter = visitHistoryAdapter
            }



        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PatientDataDetailFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PatientDataDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}