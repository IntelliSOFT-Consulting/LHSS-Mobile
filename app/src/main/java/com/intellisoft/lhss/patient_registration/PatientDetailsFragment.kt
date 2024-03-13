package com.intellisoft.lhss.patient_registration

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson

import com.intellisoft.lhss.R
import com.intellisoft.lhss.databinding.FragmentPatientDetailsBinding
import com.intellisoft.lhss.databinding.FragmentPatientLocationBinding
import com.intellisoft.lhss.fhir.data.CustomPatient
import com.intellisoft.lhss.fhir.data.FormatterClass
import com.intellisoft.lhss.utils.AppUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PatientDetailsFragment : Fragment() {

    private lateinit var binding: FragmentPatientDetailsBinding
    private val formatter = FormatterClass()
    private var mListener: OnButtonClickListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPatientDetailsBinding.inflate(layoutInflater)

        return binding.root    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AppUtils().disableEditing(binding.dateOfBirth)
        AppUtils().disableEditing(binding.calculatedAge)


        val isUpdate = FormatterClass().getSharedPref("isUpdate", requireContext())
        if (isUpdate != null) {
            displayInitialData()
        }

        binding.apply {
//            radioGroup.setOnCheckedChangeListener { group, checkedId ->
//                if (checkedId != -1) {
//                    val dataValue = when (checkedId) {
//                        R.id.radioButtonYes -> "Male"
//                        R.id.radioButtonNo -> "Female"
//                        else -> null
//                    }
//
//                }
//            }
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
            nextButton.apply {
                setOnClickListener {
                    validateData()
                }
            }
        }

    }
    private fun displayInitialData() {
        try {
            val personal = formatter.getSharedPref("personal", requireContext())

            if (personal != null) {
                val data = Gson().fromJson(personal, CustomPatient::class.java)
                binding.apply {
                    val parts = data.firstname.split(" ")
                    when (parts.size) {
                        2 -> {
                            val (firstName, lastName) = parts
                            firstname.setText(firstName)
                            lastname.setText(lastName)
                        }

                        3 -> {
                            val (firstName, middleName, lastName) = parts
                            firstname.setText(firstName)
                            lastname.setText(lastName)
                            middlename.setText(middleName)
                        }

                        else -> {
                            println("Invalid name format")
                        }
                    }
                    val gender = data.gender
                    if (gender == "Male") {
                        radioButtonYes.isChecked = true
                    } else {
                        radioButtonNo.isChecked = true
                    }
                    radioButtonYesDob.isChecked = true
                    telDateOfBirth.visibility = View.VISIBLE
                    dateOfBirth.setText(data.dateOfBirth)
                    calculateUserAge(data.dateOfBirth)


                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun calculateDateOfBirth(year: Int, months: Int, weeks: Int): String {
        // Get current date
        val currentDate = Calendar.getInstance()
        // Subtract years from the current date
        currentDate.add(Calendar.YEAR, -year)
        // Subtract months
        currentDate.add(Calendar.MONTH, -months)
        // Subtract weeks
        currentDate.add(Calendar.WEEK_OF_YEAR, -weeks)
        // Format the date to yyyy-MM-dd
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(currentDate.time)
    }

    private fun calculateUserAge(valueCurrent: String) {
        val result = formatter.calculateAge(valueCurrent)
        binding.calculatedAge.setText(result)

        // check the year as well
        val year = formatter.calculateAgeYear(valueCurrent)
    }



    private fun validateData() {
        var gender = ""
        var dateType = ""
        val firstName = binding.firstname.text.toString()
        val lastName = binding.lastname.text.toString()
        val middleName = binding.middlename.text.toString()
        var dateOfBirthString = binding.dateOfBirth.text.toString()
        val age = binding.calculatedAge.text.toString()


        if (firstName.isEmpty()) {
            binding.apply {
                telFirstname.error = "Enter firstname"
                firstname.requestFocus()
                return
            }
        }
        if (lastName.isEmpty()) {
            binding.apply {
                telLastName.error = "Enter lastname"
                lastname.requestFocus()
                return
            }
        }
        val checkedRadioButtonId = binding.radioGroup.checkedRadioButtonId
        if (checkedRadioButtonId != -1) {
            // RadioButton is selected, find the selected RadioButton
            val selectedRadioButton =
                binding.root.findViewById<RadioButton>(checkedRadioButtonId)
            gender = selectedRadioButton.text.toString()

        } else {
            // No RadioButton is selected, handle it as needed
            Toast.makeText(requireContext(), "Please select a gender", Toast.LENGTH_SHORT)
                .show()
            return
        }


        val estimatedID = binding.radioGroupDob.checkedRadioButtonId
        if (estimatedID != -1) {
            // RadioButton is selected, find the selected RadioButton
            val selectedRadioButtonDob = binding.root.findViewById<RadioButton>(estimatedID)
            dateType = selectedRadioButtonDob.text.toString()

        } else {
            // No RadioButton is selected, handle it as needed
            Toast.makeText(
                requireContext(),
                "Please select date of birth option",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (dateType == "Actual") {

            if (dateOfBirthString.isEmpty()) {
                binding.apply {
                    telDateOfBirth.error = "Enter date of birth"
                    dateOfBirth.requestFocus()
                    return
                }
            }
        } else {
            // check all the fields
            val year = binding.editTextOne.text.toString()
            val months = binding.editTextTwo.text.toString()
            val weeks = binding.editTextThree.text.toString()

            if (year.isEmpty() && months.isEmpty() && weeks.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Please enter estimate age",
                    Toast.LENGTH_SHORT
                )
                    .show()
                return
            }

            val enteredYear = binding.editTextOne.text.toString().toIntOrNull() ?: 0
            val enteredMonths = binding.editTextTwo.text.toString().toIntOrNull() ?: 0
            val enteredWeeks = binding.editTextThree.text.toString().toIntOrNull() ?: 0
            val dateOfBirth = calculateDateOfBirth(enteredYear, enteredMonths, enteredWeeks)

            dateOfBirthString = dateOfBirth

        }

        val payload = CustomPatient(
            firstname = firstName,
            middlename = middleName,
            lastname = lastName,
            gender = gender,
            age = age,
            dateOfBirth = dateOfBirthString
        )

        formatter.saveSharedPref("registrationFlowPersonal", Gson().toJson(payload), requireContext())

        findNavController().navigate(R.id.patientLocationFragment)

    }




}