package com.intellisoft.chanjoke.detail.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ContentInfoCompat.Flags
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.google.android.fhir.FhirEngine
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.databinding.FragmentAppointmentsBinding
import com.intellisoft.chanjoke.databinding.FragmentVaccineDetailsBinding
import com.intellisoft.chanjoke.detail.PatientDetailActivity
import com.intellisoft.chanjoke.detail.ui.main.adapters.EventsAdapter
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import timber.log.Timber
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter


class VaccineDetailsFragment : Fragment() {
    private lateinit var binding: FragmentVaccineDetailsBinding
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel

    /**/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentVaccineDetailsBinding.inflate(inflater, container, false)
        fhirEngine = FhirApplication.fhirEngine(requireContext())
        val patientId = FormatterClass().getSharedPref("patientId", requireContext())

        patientDetailsViewModel =
            ViewModelProvider(
                this,
                PatientDetailsViewModelFactory(
                    requireActivity().application,
                    fhirEngine,
                    patientId.toString()
                ),
            ).get(PatientDetailsViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onBackPressed()
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = getString(R.string.administer_vaccine)
            setDisplayHomeAsUpEnabled(true)
        }

        binding.apply {
            val vaccineName = FormatterClass().getSharedPref("vaccine_name", requireContext())
            val vaccineDose = FormatterClass().getSharedPref("vaccine_dose", requireContext())
            val vaccineDate = FormatterClass().getSharedPref("vaccine_date", requireContext())
            tvVaccineName.text = vaccineName
            tvVaccineDate.text = vaccineDate
            tvVaccineDose.text = vaccineDose
            tvDaysSince.text = generateDaysSince(vaccineDate.toString(), days = true, month = false)
            tvMonthSince.text =
                generateDaysSince(vaccineDate.toString(), days = false, month = true)
            tvYearsSince.text =
                generateDaysSince(vaccineDate.toString(), days = false, month = false)


            (requireActivity() as AppCompatActivity).supportActionBar?.apply {
                if (vaccineName != null) {
                    title = vaccineName.replace("Vaccine:", "")
                }
            }
            val patientDob = FormatterClass().getSharedPref("patientDob", requireContext())
            val age = generateAgeSince(patientDob, vaccineDate)
            tvAgeThen.text = age

        }
        loadVaccineAdverseEvents()
    }

    private fun onBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            exitPageSection()
        }
    }

    private fun generateAgeSince(dateString: String?, vaccineDate: String?): String {
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val formatter2 = DateTimeFormatter.ofPattern("MMM dd yyyy")

            // Parse the string into a LocalDate
            val date1 = LocalDate.parse(dateString, formatter)
            val date2 = LocalDate.parse(vaccineDate, formatter2)
            // Calculate the period between the two dates
            val period = Period.between(date1, date2)
            when {
                period.years > 1 -> "${period.years} years ${period.months} months ${period.days} days"
                period.years == 1 -> "${period.years} year ${period.months} months ${period.days} days"
                period.months > 1 -> "${period.months} months ${period.days} days"
                else -> "${period.days} days"
            }

//            "${period.years} year(s) ${period.months} month(s) ${period.days} day(s)"

        } catch (e: Exception) {
            "0 day(s)"
        }
    }

    private fun generateDaysSince(
        dateString: String,
        days: Boolean,
        month: Boolean,
    ): String {

        // Define the date format
        try {
            val formatter = DateTimeFormatter.ofPattern("MMM dd yyyy")

            // Parse the string into a LocalDate
            val date1 = LocalDate.parse(dateString, formatter)
            val date2 = LocalDate.now()
            // Calculate the period between the two dates
            val period = Period.between(date1, date2)
            if (days) {
                return "${period.days} day(s)"
            }
            return if (month) {
                "${period.months} month(s)"
            } else {
                "${period.years} year(s)"
            }
        } catch (e: Exception) {
            return "0 day(s)"
        }
    }

    private fun loadVaccineAdverseEvents() {
        val logicalId = FormatterClass().getSharedPref("current_immunization", requireContext())

        if (logicalId != null) {
            val adverseEvents = patientDetailsViewModel.loadImmunizationAefis(logicalId)

            val vaccineAdapter = EventsAdapter(adverseEvents, requireContext())
            binding.recyclerView.adapter = vaccineAdapter
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                exitPageSection()
                true
            }

            else -> false
        }
    }

    private fun exitPageSection() {
        val patientId = FormatterClass().getSharedPref("patientId", requireContext())
        val intent = Intent(context, PatientDetailActivity::class.java)
        intent.putExtra("patientId", patientId)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        requireContext().startActivity(intent)
    }

}