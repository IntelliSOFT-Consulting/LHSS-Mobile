package com.intellisoft.lhss.patient_registration

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

import com.intellisoft.lhss.R
import com.intellisoft.lhss.databinding.FragmentPatientDetailsBinding
import com.intellisoft.lhss.databinding.FragmentPatientLocationBinding
import com.intellisoft.lhss.fhir.data.FormatterClass
import java.util.Calendar

class PatientDetailsFragment : Fragment() {

    private lateinit var binding: FragmentPatientDetailsBinding
    private val formatter = FormatterClass()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPatientDetailsBinding.inflate(layoutInflater)

        return binding.root    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            radioGroup.setOnCheckedChangeListener { group, checkedId ->
                if (checkedId != -1) {
                    val dataValue = when (checkedId) {
                        R.id.radioButtonYes -> "Male"
                        R.id.radioButtonNo -> "Female"
                        else -> null
                    }

                }
            }
            radioGroupDob.setOnCheckedChangeListener { group, checkedId ->
                if (checkedId != -1) {
                    when (checkedId) {
                        R.id.radioButtonYesDob -> {
                            lnEstimated.visibility = View.GONE
                            telDateOfBirth.visibility = View.VISIBLE
                        }

                        R.id.radioButtonNoDob -> {
                            lnEstimated.visibility = View.VISIBLE

                            telDateOfBirth.visibility = View.GONE
                        }

                        else -> {

                            lnEstimated.visibility = View.GONE

                            telDateOfBirth.visibility = View.GONE
                        }
                    }

                }
            }

            dateOfBirth.apply {

                setOnClickListener {
                    val calendar: Calendar = Calendar.getInstance()
                    val datePickerDialog = DatePickerDialog(
                        requireContext(),
                        { datePicker: DatePicker?, year: Int, month: Int, day: Int ->
                            val valueCurrent: String = formatter.getDate(year, month, day)
                            setText(valueCurrent)
                            calculateUserAge(valueCurrent)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )
                    datePickerDialog.datePicker.maxDate = calendar.getTimeInMillis()
                    datePickerDialog.show()
                }
            }
        }



        binding.nextButton.setOnClickListener{
            findNavController().navigate(R.id.patientLocationFragment)
        }
    }

    private fun calculateUserAge(valueCurrent: String) {
        val result = formatter.calculateAge(valueCurrent)
        binding.calculatedAge.setText(result)

        // check the year as well
        val year = formatter.calculateAgeYear(valueCurrent)
        if (year >= 18) {
            binding.telephone.visibility = View.VISIBLE
            formatter.saveSharedPref("isAbove", "true", requireContext())

        } else {
            binding.telephone.visibility = View.GONE
            formatter.saveSharedPref("isAbove", "false", requireContext())

        }
        updateIdentifications(year)
    }

    private fun updateIdentifications(age: Int) {
        val identifications = when {
            age < 3 -> {
                arrayOf(
                    "Birth Certificate",
                    "Passport",
                    "Birth Notification Number"
                )
            }

            age in 3..17 -> {
                arrayOf(
                    "Birth Certificate",
                    "Passport",
                    "Nemis"
                )
            }

            else -> {
                arrayOf(
                    "Birth Certificate",
                    "ID Number",
                    "Passport"
                )
            }
        }

        val adapterType =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                identifications
            )

        binding.apply {
            identificationType.apply {
                setAdapter(adapterType)
            }
        }

    }





}