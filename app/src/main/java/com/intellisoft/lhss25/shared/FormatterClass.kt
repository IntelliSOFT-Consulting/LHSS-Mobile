package com.intellisoft.lhss25.shared

import android.app.DatePickerDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Build
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.hbb20.CCPCountry
import com.intellisoft.lhss25.LocationViewModel
import com.intellisoft.lhss25.R
import com.intellisoft.lhss25.dynamic_components.MandatoryRadioGroup


import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.regex.Pattern

class FormatterClass(private val context: Context) {

    fun sortPatientInformation(formDataList :ArrayList<FormData>):ArrayList<FormData>{
        val sortOrder = listOf("DEMOGRAPHICS", "ADDRESS_ORIGIN", "ADDRESS_RESIDENCE", "NEXT_OF_KIN")

        formDataList.sortWith { formData1, formData2 ->
            val index1 = sortOrder.indexOf(formData1.title)
            val index2 = sortOrder.indexOf(formData2.title)
            index1.compareTo(index2)
        }
        return formDataList
    }
    fun getAvailableList(context: Context, locationViewModel: LocationViewModel): ArrayList<String> {

        val kenyaHospitals = context.resources
            .getStringArray(R.array.available_kenya_countries_array)
        val ugandaHospitals = context.resources
            .getStringArray(R.array.available_uganda_countries_array)

        val userCountryNameCode = getSharedPref("","userCountryNameCode")
            ?: return ArrayList(emptyList())

        val userFacilityId = getSharedPref("","userFacility")?.replace("Location/","")
            ?: return ArrayList(emptyList())

        val availableHospitals = ArrayList<String>()
        val allHospitals = ArrayList<String>()
        val kenyaHospitalsList = ArrayList(kenyaHospitals.toList())
        val ugandaHospitalsList = ArrayList(ugandaHospitals.toList())

        // Add both lists to allHospitals
        allHospitals.addAll(kenyaHospitalsList)
        allHospitals.addAll(ugandaHospitalsList)


        allHospitals.forEach {hospitalName ->

            val locationReferenceList = locationViewModel.getFacilityByName(hospitalName)

            if (locationReferenceList.isNotEmpty()){
                val locationReference = locationReferenceList.first()
                val locationName = locationReference.name
                val locationCode = locationReference.code
                val locationId = locationReference.id?.split("/")?.get(1)

                if (locationId != null && locationId != userFacilityId){
                    availableHospitals.add(locationName)
                }
            }
        }

        return availableHospitals

    }

    fun findTextViewByText(rootLayout: ViewGroup, searchText: String): TextView? {
        for (i in 0 until rootLayout.childCount) {
            val child = rootLayout.getChildAt(i)

            // Check if the child is a TextView
            if (child is TextView) {
                // Compare the text of the TextView
                if (child.text.toString() == searchText) {
                    return child
                }
            }

            // If the child is a ViewGroup (like LinearLayout), recursively search its children
            if (child is ViewGroup) {
                val result = findTextViewByText(child, searchText)
                if (result != null) return result
            }
        }
        return null
    }
    fun isValidEmail(email: String): Boolean {
        val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        val pattern = Pattern.compile(emailPattern)
        val matcher = pattern.matcher(email)
        return matcher.matches()
    }

    private val dateInverseFormatSeconds: SimpleDateFormat =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)

    fun generateUuid(): String {
        return UUID.randomUUID().toString()
    }

    fun calculateAge(dob: String): String? {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        try {
            // Parse the input date
            val birthDate = LocalDate.parse(dob, formatter)

            // Get the current date
            val currentDate = LocalDate.now()

            // Calculate the period between the two dates
            val age = Period.between(birthDate, currentDate)

            // Return age as years, months, and days
            return "${age.years} years, ${age.months} months, ${age.days} days"
        } catch (e: DateTimeParseException) {
            return null
        }
    }

    fun showDialog(title:String, message: String) {

        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)

        // Positive button (Yes)
        builder.setPositiveButton("Confirm") { dialog, _ ->
            // Handle the Yes action
            dialog.dismiss()
        }

        // Negative button (No)
        builder.setNegativeButton("Back") { dialog, _ ->
            // Handle the No action
            dialog.dismiss()
        }

        // Create and show the dialog
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    fun showDatePickerWithLimits(textView: TextView, isPast: Boolean, fromDateStr: String?) {
        // Get current date
        val calendar = Calendar.getInstance()

        // Set a DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            textView.context,
            { _, year, month, dayOfMonth ->
                // Format the chosen date and set it to the TextView
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("MMM dd yyyy", Locale.getDefault())
                textView.text = dateFormat.format(selectedCalendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Set min and max date
        val today = calendar.timeInMillis


        if (isPast){
            datePickerDialog.datePicker.maxDate = today
        }else{
            val fromDate = fromDateStr?.let { convertStringToDate(it)?.time } ?: today
            datePickerDialog.datePicker.minDate = fromDate
            datePickerDialog.datePicker.maxDate = today
        }

        // Show the DatePickerDialog
        datePickerDialog.show()
    }

    private fun convertStringToDate(dateStr: String): Date? {
        val dateFormat = SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH)
        return try {
            dateFormat.parse(dateStr)
        } catch (e: Exception) {
            null // Handle parsing errors gracefully
        }
    }

    fun formatCurrentDateTime(date: Date): String {
        return dateInverseFormatSeconds.format(date)
    }

    fun addRadioButtonWithDatePicker(context: Context, linearLayout: LinearLayout) {

        // Create a new LinearLayout with horizontal orientation
        val horizontalLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 8, 16, 8)
        }

        // Add the RadioGroup inside this horizontal layout
        val radioGroup = MandatoryRadioGroup(context).apply {
            // Set orientation to horizontal
            orientation = RadioGroup.HORIZONTAL
            tag = "DOB_SELECTION"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val acc = View.generateViewId() // Use unique view IDs
        val est = View.generateViewId() // Use unique view IDs

        val radioButtonAccurate = RadioButton(context).apply {
            text = "Accurate"
            tag = "Accurate"
            id = acc // Assign unique ID to the RadioButton
            layoutParams = LinearLayout.LayoutParams(
                0, // Width is 0 to allow weight distribution
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val radioButtonEstimate = RadioButton(context).apply {
            text = "Estimate"
            tag = "Estimate"
            id = est // Assign unique ID to the RadioButton
            layoutParams = LinearLayout.LayoutParams(
                0, // Width is 0 to allow weight distribution
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val textViewDateOfBirthLabel = TextView(context).apply {
            text = "Date of Birth *"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            this.setPadding(32, 8, 20, 8)
            textSize = 18f
        }

        val textViewDateOfBirthLabel1 = TextView(context).apply {
            text = "Date of Birth *"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            this.setPadding(32, 8, 20, 8)
            textSize = 18f
        }

        // Add both RadioButtons to the RadioGroup (ensures exclusive selection)
        radioGroup.addView(radioButtonAccurate)
        radioGroup.addView(radioButtonEstimate)

        linearLayout.addView(textViewDateOfBirthLabel1)

        // Add the RadioGroup to the horizontal layout
        horizontalLayout.addView(radioGroup)

        // Add the horizontal layout to the parent linearLayout
        linearLayout.addView(horizontalLayout)

        // TextView to show DatePickerDialog when Accurate is selected
        val textViewDate = TextView(context).apply {
            text = "Select Date"
            tag = "DOB"
            background = ContextCompat.getDrawable(context, R.drawable.rounded_edittext)
            // Set drawable to the right
            val rightIcon = ContextCompat.getDrawable(context, R.drawable.ic_date_picker) // Your drawable resource
            setCompoundDrawablesWithIntrinsicBounds(null, null, rightIcon, null)

            setOnClickListener {
                showDatePickerDialog(context, this)
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 5, 8, 12)
            }
            this.setPadding(32, 16, 20, 16)
        }

        // Create a new LinearLayout for horizontal orientation of Estimate fields
        val horizontalLayoutForEstimate = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 5, 0, 12)
            }
            setPadding(16, 8, 16, 8)
        }

        // EditText for Year input (for Estimate)
        val editTextYears = EditText(context).apply {
            hint = "Years"
            background = ContextCompat.getDrawable(context, R.drawable.rounded_edittext)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            visibility = View.GONE // Hidden initially
            filters = arrayOf(InputFilter.LengthFilter(4), YearInputFilter(this))
            layoutParams = LinearLayout.LayoutParams(
                0, // Set width to 0 for equal distribution
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f // Weight of 1 to distribute space equally between Year and Month inputs
            ).apply {
                setMargins(0, 5, 8, 12)
            }
            setPadding(32, 16, 20, 16)
        }

        // EditText for Month input (for Estimate)
        val editTextMonths = EditText(context).apply {
            hint = "Months"
            background = ContextCompat.getDrawable(context, R.drawable.rounded_edittext)
            inputType = android.text.InputType.TYPE_CLASS_TEXT
            visibility = View.GONE // Hidden initially
            filters = arrayOf(InputFilter.LengthFilter(2), MonthInputFilter())
            layoutParams = LinearLayout.LayoutParams(
                0, // Set width to 0 for equal distribution
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f // Weight of 1 to distribute space equally between Year and Month inputs
            ).apply {
                setMargins(0, 5, 8, 12)
            }
            setPadding(32, 16, 20, 16)
        }

        // Add the EditTexts to the horizontal layout
        horizontalLayoutForEstimate.addView(editTextYears)
        horizontalLayoutForEstimate.addView(editTextMonths)

        // Add the horizontal layout to the parent LinearLayout
        linearLayout.addView(horizontalLayoutForEstimate)

        linearLayout.addView(textViewDateOfBirthLabel)
        linearLayout.addView(textViewDate)


        // Add manual click listener for RadioButtons
        radioButtonAccurate.setOnClickListener {
            textViewDate.isEnabled = true
            editTextYears.visibility = View.GONE
            editTextMonths.visibility = View.GONE

            radioButtonEstimate.isChecked = false
        }

        radioButtonEstimate.setOnClickListener {
            textViewDate.isEnabled = false
            editTextYears.visibility = View.VISIBLE
            editTextMonths.visibility = View.VISIBLE

            radioButtonAccurate.isChecked = false
        }

        // Add a TextWatcher to editTextYears
        editTextYears.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            @RequiresApi(Build.VERSION_CODES.O)
            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    val selectedYear = s.toString()
                    val selectedMonth = editTextMonths.text.toString()

                    if (!TextUtils.isEmpty(selectedMonth)) {
                        val selectedDate = approximateDate(selectedYear, selectedMonth)
                        textViewDate.text = selectedDate
                    }

                }
            }
        })

        // Add a TextWatcher to editTextMonths
        editTextMonths.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            @RequiresApi(Build.VERSION_CODES.O)
            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    val selectedMonth = s.toString()
                    val years = editTextYears.text.toString()
                    if (!TextUtils.isEmpty(years)) {
                        val selectedDate = approximateDate(years, selectedMonth)
                        textViewDate.text = selectedDate
                    }


                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun approximateDate(yearsInput: String, monthsInput: String): String {
        // Parse the years and months from the input strings
        val years = yearsInput.toIntOrNull() ?: 0
        val months = monthsInput.toIntOrNull() ?: 0

        // Get the current date
        val currentDate = LocalDate.now()

        // Subtract the years and months from the current date
        val approximateDate = currentDate.minusYears(years.toLong()).minusMonths(months.toLong())

        // Format the resulting date as yyyy-MM-dd (day will always be '01')
        val formattedDate =
            approximateDate.withDayOfMonth(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        return formattedDate
    }


    // Input filter for Year
    class YearInputFilter(private val editText: EditText) : InputFilter {
        override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
            if (source.isNullOrEmpty()) return null

            val input = (dest.toString() + source).toIntOrNull()
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)

            return if (input != null && input > currentYear) {
                editText.setText("") // Clear the input
                editText.error = "Year cannot be in the future" // Set error message
                ""  // Reject input
            } else {
                null  // Accept input
            }
        }
    }

    // Input filter for Month
    class MonthInputFilter : InputFilter {
        override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
            if (source.isNullOrEmpty()) return null

            val input = (dest.toString() + source).toIntOrNull()

            return if (input != null && (input < 1 || input > 12)) {
                ""  // Reject input if it's not within the valid range
            } else {
                null  // Accept input
            }
        }
    }

    private fun showDatePickerDialog(context: Context, editTextSelectedDate: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                editTextSelectedDate.text = selectedDate

            },
            year, month, day
        )
        // Block off future dates
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis

        datePickerDialog.show()
    }



    fun clearData() {

        listOf(
            DbNavigationDetails.PATIENT_REGISTRATION.name,
            DbNavigationDetails.REFER_PATIENT.name,
            DbNavigationDetails.REFERRALS.name).forEach {
            clearSharedPreferences(it)
        }
        listOf("serviceRequestId", "patientId", "CLINICAL_REFERRAL").forEach {
            deleteSharedPref("", it)
        }

    }
    fun clearPatientData() {

        listOf(
            DbNavigationDetails.REFER_PATIENT.name,
            DbNavigationDetails.REFERRALS.name).forEach {
            clearSharedPreferences(it)
        }
        listOf("serviceRequestId", "CLINICAL_REFERRAL").forEach {
            deleteSharedPref("", it)
        }

    }

    fun saveSharedPref(
        sharedPrefName:String,
        key: String,
        value: String)
    {
        val sharedPreferenceName = if (sharedPrefName == ""){
            context.getString(R.string.app_name)
        }else{
            sharedPrefName
        }

        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(sharedPreferenceName, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(key, value);
        editor.apply();
    }

    fun getSharedPref(
        sharedPrefName:String,
        key: String): String? {
        val sharedPreferenceName = if (sharedPrefName == ""){
            context.getString(R.string.app_name)
        }else{
            sharedPrefName
        }
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(sharedPreferenceName, MODE_PRIVATE)
        return sharedPreferences.getString(key, null)

    }

    fun toSentenceCase(input:String): String {
        return input.lowercase()
            .replace("_"," ")
            .replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }
    }

    // Function to clear all SharedPreferences data
    fun clearSharedPreferences(sharedPrefName: String) {
        val sharedPreferenceName = if (sharedPrefName == ""){
            context.getString(R.string.app_name)
        }else{
            sharedPrefName
        }
        val sharedPreferences = context.getSharedPreferences(sharedPreferenceName, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()  // Clear all the stored values
        editor.apply()  // Apply changes
    }

    fun deleteSharedPref(
        sharedPrefName:String,
        key: String) {
        val sharedPreferenceName = if (sharedPrefName == ""){
            context.getString(R.string.app_name)
        }else{
            sharedPrefName
        }
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(sharedPreferenceName, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(key);
        editor.apply();

    }

    fun sortPatientListByDate(
        patientList: List<DbPatientItem>,
        fromDate: String?,
        toDate: String?
    ): List<DbPatientItem> {
        // Define the date format that matches the format of dateCreated field
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)

        // Define supported date formats
        val inputDateFormats = listOf(
            SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH),
            SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH),
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH),
            SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
        )

        // Parse the fromDate and toDate if they are not null
        val minDate: Date? = fromDate?.let { parseDateSafely(it, dateFormat) }
        val maxDate: Date? = toDate?.let { parseDateSafely(it, dateFormat) }

        val sortedPatients = patientList
            // Filter based on fromDate (min) and toDate (max)
            .filter { patient ->
                patient.dateCreated?.let { dateStr ->
                    val createdDate = parseDateSafely(dateStr, dateFormat)
                    // Remove null dateCreated if minDate is specified
                    if (minDate != null && createdDate == null) return@filter false
                    // Check if dateCreated is after minDate (if minDate exists)
                    if (minDate != null && createdDate != null && createdDate.before(minDate)) return@filter false
                    // Check if dateCreated is before maxDate (if maxDate exists)
                    if (maxDate != null && createdDate != null && createdDate.after(maxDate)) return@filter false
                    true
                } ?: (minDate == null && maxDate == null) // If dateCreated is null, allow only if no min or max
            }
            // Sort the filtered list by dateCreated in descending order
            .sortedByDescending { patient ->
                patient.dateCreated?.let { dateStr ->
                    parseDateSafely(dateStr, dateFormat)
                }
            }

        val sortedPatientsList = ArrayList(sortedPatients)

        sortedPatientsList.sortWith(compareByDescending {
            try {
                it.dateCreated?.let { it1 -> parseDateSafely(it1, dateFormat) }
            } catch (e: Exception) {
                Log.e("PatientListViewModel", "Error parsing date: ${it.dateCreated}")
                null
            }
        })

        return sortedPatients
    }

    // Helper function to safely parse dates and handle exceptions
    fun parseDateSafely(dateStr: String, dateFormat: SimpleDateFormat): Date? {
        return try {
            dateFormat.parse(dateStr)
        } catch (e: Exception) {
            null // In case of parsing failure, return null
        }
    }

    fun convertDateFormat(inputDate: String): String? {
        // Define the input date formats to check
        val inputDateFormats = arrayOf(
            "yyyy-MM-dd",
            "dd/MM/yyyy",
            "MM/dd/yyyy",
            "yyyyMMdd",
            "dd-MM-yyyy",
            "yyyy/MM/dd",
            "MM-dd-yyyy",
            "yyyyMMddHHmmss",
            "yyyy-MM-dd HH:mm:ss",
            "EEE, dd MMM yyyy HH:mm:ss Z",  // "Mon, 25 Dec 2023 12:30:45 +0000"
            "yyyy-MM-dd'T'HH:mm:ssXXX",      // ISO 8601 with time zone offset (e.g., "2023-11-29T15:44:00+03:00")
            "EEE MMM dd HH:mm:ss zzz yyyy", // "Wed Nov 13 18:35:24 GMT+03:00 2024"
            "EEE MMM dd HH:mm:ss z yyyy",
            "EEE MMM dd HH:mm:ss 'GMT'XXX yyyy",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", // "2024-11-19T15:30:45.123+03:00"
            "EEE, dd MMM yyyy",             // "Mon, 25 Dec 2023"
            "yyyy-MM-dd hh:mm:ss a",        // "2024-11-19 03:15:30 PM"
            "yyyy-MM-dd HH:mm:ss 'GMT'XXX", // "2024-11-19 15:30:45 GMT+03:00"
            "dd/MM/yyyy HH:mm:ss",          // "19/11/2024 15:30:45"
            "EEE dd MMM yyyy HH:mm:ss",     // "Thu 14 Nov 2024 00:31:07"
            "MMM dd, yyyy",                 // "Nov 14, 2024"
            "EEE MMM dd HH:mm:ss z yyyy",   // "Thu Nov 14 00:31:07 GMT 2024"
            "yyyy-MM-dd'T'HH:mm:ss",        // "2024-11-19T15:30:45"
            "yyyy-MM-dd HH:mm:ss.SSS",      // "2024-11-19 15:30:45.123"
            "MMM yyyy",                     // "Nov 2024"
            "MM/dd/yyyy HH:mm:ss Z",        // "11/19/2024 15:30:45 +0300"
            "yyyyMMdd",                     // "20241119"
            "EEE MMM dd hh:mm:ss a",        // "Thu Nov 14 12:31:07 AM"
            "EEE, dd MMM yyyy HH:mm:ss Z"   // "Thu, 14 Nov 2024 00:31:07 +0300"
        )


        // Try parsing the input date with each format
        for (format in inputDateFormats) {
            try {
                val dateFormat = SimpleDateFormat(format, Locale.getDefault())
                dateFormat.isLenient = false // Set lenient to false
                val parsedDate = dateFormat.parse(inputDate)

                // If parsing succeeds, format and return the date in the desired format
                parsedDate?.let {
                    return SimpleDateFormat("MMM d yyyy", Locale.getDefault()).format(it)
                }
            } catch (e: ParseException) {
                e.printStackTrace()
                // Continue to the next format if parsing fails
            }
        }

        // If none of the formats match, return an error message or handle it as needed
        return null
    }

    fun parsePhoneNumber(number: String): Pair<String, String>? {
        // Regex pattern to match any country code (starting with +) followed by digits

        val regexPattern = Regex("""^\+?\d+$""")
        if (!regexPattern.matches(number)) {
            return null
        }

        val phoneCodeList = ArrayList<String>()
        val countryCodeList = CCPCountry.getLibraryMasterCountriesEnglish()
        countryCodeList.forEach { ccpCountry ->
            val phoneCode = ccpCountry.phoneCode
            phoneCodeList.add(phoneCode)
        }

        val countryCode = number.replace("+", "").substring(0, 3)

        return if (phoneCodeList.contains(countryCode)) {
            Pair(countryCode, number.replace("+", "").substring(3))
        }else{
            null
        }
    }

    fun getNameFields(formDataList: ArrayList<FormData>): String {
        var firstName: String = ""
        var middleName: String = ""
        var lastName: String = ""

        // Loop through formDataList
        formDataList.forEach { formData ->
            if (formData.title == "DEMOGRAPHICS") {
                // Loop through the list of DbFormData
                formData.formDataList.forEach { dbFormData ->
                    when (dbFormData.tag) {
                        "First Name" -> firstName = dbFormData.text
                        "Middle Name" -> middleName = dbFormData.text
                        "Last Name" -> lastName = dbFormData.text
                    }
                }
            }
        }

        // Return a Triple containing the First Name, Middle Name, and Last Name
        return "$firstName $middleName $lastName"
    }

    fun getStandardPhoneNumber(number: String):Boolean{

        return if (number.length > 8){
            val input1 = StringBuilder()
            input1.append(number)
            val reversedString = input1.reverse()
            val newReversedString = reversedString.substring(0, 9)

            val stringBuilder = StringBuilder()
            stringBuilder.append(newReversedString)
            val newString = stringBuilder.reverse()
            val newPhone= "0$newString"
            true
        }else{
            false
        }



    }

    fun getWorkflowTitles(textId: String): WorkflowTitles? {
        val entries = WorkflowTitles.entries
        val textIdValue = entries.find { it.textId == textId }
        return textIdValue
    }

    fun filterPatients(
        patientList: ArrayList<DbPatientItem>,
        fromDateChar: CharSequence? = null,
        toDateChar: CharSequence? = null
    ): List<DbPatientItem> {
        // Define supported date formats
        val inputDateFormats = listOf(
            SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH),
            SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH),
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH),
            SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
        )


        // Helper function to parse dates using the supported formats
        fun parseDate(dateStr: String?): Date? {
            if (dateStr == null) return null
            for (format in inputDateFormats) {
                try {
                    return format.parse(dateStr)
                } catch (e: ParseException) {
                    // Ignore and try the next format
                }
            }
            return null // Return null if none of the formats work
        }

        // Parse fromDate and toDate using the helper function
        val fromDate: Date? = parseDate(fromDateChar?.toString())
        val toDate: Date? = parseDate(toDateChar?.toString())

        // Filter the patientList
        val patientList1 = patientList.filter { patient ->
            val patientDate = parseDate(patient.dateCreated)
            when {
                fromDate != null && toDate != null -> patientDate != null
                        && !patientDate.before(fromDate) && !patientDate.after(toDate)
                fromDate != null -> patientDate != null && !patientDate.before(fromDate)
                toDate != null -> patientDate != null && !patientDate.after(toDate)
                else -> true // No filtering if both fromDate and toDate are null
            }
        }

        return patientList1
    }


    fun filterPatientsByDate(
        patients: List<DbPatientItem>,
        fromDate: String?,
        toDate: String?
    ): List<DbPatientItem> {
        val dateFormat = SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH) // Format for fromDate and toDate
        val dateCreatedFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH) // Format for dateCreated

        val from: Date? = try {
            fromDate?.let { dateFormat.parse(it) }
        } catch (e: Exception) {
            println("Unparseable fromDate: $fromDate")
            null
        }

        val to: Date? = try {
            toDate?.let { dateFormat.parse(it) }
        } catch (e: Exception) {
            println("Unparseable toDate: $toDate")
            null
        }

        return patients.filter { patient ->
            val patientDateCreated: Date? = try {
                dateCreatedFormat.parse(patient.dateCreated)
            } catch (e: Exception) {
                println("Unparseable dateCreated for patient ID ${patient.id}: ${patient.dateCreated}")
                null
            }

            if (patientDateCreated == null) {
                false // Exclude patients with unparseable dateCreated
            } else {
                when {
                    from != null && to != null -> patientDateCreated in from..to
                    from != null -> patientDateCreated >= from
                    to != null -> patientDateCreated <= to
                    else -> true // Include all patients if both fromDate and toDate are null
                }
            }
        }
    }



}