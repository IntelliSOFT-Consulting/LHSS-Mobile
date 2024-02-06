package com.intellisoft.chanjoke.fhir.data

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.res.Resources
import android.util.Log
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.vaccine.validations.ImmunizationHandler
import com.intellisoft.chanjoke.vaccine.validations.NonRoutineVaccine
import com.intellisoft.chanjoke.vaccine.validations.PregnancyVaccine
import com.intellisoft.chanjoke.vaccine.validations.RoutineVaccine

import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.random.Random

class FormatterClass {
    fun saveSharedPref(key: String, value: String, context: Context) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(key, value);
        editor.apply();
    }

    fun getSharedPref(key: String, context: Context): String? {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE)
        return sharedPreferences.getString(key, null)

    }

    fun deleteSharedPref(key: String, context: Context) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(key);
        editor.apply();

    }

    fun convertStringToDate(dateString: String, format: String): Date? {
        return try {
            val dateFormat = SimpleDateFormat(format, Locale.getDefault())
            dateFormat.parse(dateString) ?: Date()
        } catch (e: Exception) {
            null
        }

    }

    fun convertDateToLocalDate(date: Date): LocalDate {
        val instant = date.toInstant()
        return instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
    }

    fun removeNonNumeric(input: String): String {
        // Regex pattern to match numeric values (with optional decimal part)
        val numericPattern = Regex("[0-9]+(\\.[0-9]+)?")

        // Find the numeric part in the input string
        val matchResult = numericPattern.find(input)

        // Extract the numeric value or return an empty string if not found
        return matchResult?.value ?: ""
    }

    fun convertDateFormat(inputDate: String): String? {
        // Define the input date formats to check
        val inputDateFormats = arrayOf(
            "yyyy-MM-dd",
            "MM/dd/yyyy",
            "yyyyMMdd",
            "dd-MM-yyyy",
            "yyyy/MM/dd",
            "MM-dd-yyyy",
            "dd/MM/yyyy",
            "yyyyMMddHHmmss",
            "yyyy-MM-dd HH:mm:ss",
            "EEE, dd MMM yyyy HH:mm:ss Z",  // Example: "Mon, 25 Dec 2023 12:30:45 +0000"
            "yyyy-MM-dd'T'HH:mm:ssXXX",     // ISO 8601 with time zone offset (e.g., "2023-11-29T15:44:00+03:00")
            "EEE MMM dd HH:mm:ss zzz yyyy", // Example: "Sun Jan 01 00:00:00 GMT+03:00 2023"

            // Add more formats as needed
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
                // Continue to the next format if parsing fails
            }
        }

        // If none of the formats match, return an error message or handle it as needed
        return null
    }


    fun calculateDateAfterWeeksAsString(dob: LocalDate, weeksAfterDob: Long): String {
        val calculatedDate = dob.plusWeeks(weeksAfterDob)
        return calculatedDate.toString()
    }

    fun clearVaccineShared(context: Context){

        //Clear the vaccines
        val vaccinationListToClear = listOf(
            "vaccinationBrand",
            "vaccinationDoseNumber",
            "vaccinationBatchNumber",
            "questionnaireJson" ,
            "vaccinationSite" ,
            "vaccinationTargetDisease" ,
            "vaccinationExpirationDate" ,
            "vaccinationDoseQuantity" ,
            "vaccinationFlow" ,
            "vaccinationSeriesDoses" ,
            "vaccinationManufacturer" ,
            "immunizationId" ,
//            "selectedVaccinationVenue" ,
//            "isSelectedVaccinationVenue" ,
            "administeredProduct")
        vaccinationListToClear.forEach {
            deleteSharedPref(it, context)
        }
    }

    fun clientInfoShared(context: Context){
        val vaccinationListToClear = listOf(
            "patientId",
            "patientDob" ,
            "appointmentId")
        vaccinationListToClear.forEach {
            deleteSharedPref(it, context)
        }
    }
    fun practionerInfoShared(context: Context){
        val vaccinationListToClear = listOf(
            "practitionerFullNames",
            "practitionerIdNumber" ,
            "practitionerRole" ,
            "fhirPractitionerId" ,
            "practitionerId" ,
            "access_token" ,
            "refresh_token" ,
            "refresh_expires_in" ,
            "expires_in")
        vaccinationListToClear.forEach {
            deleteSharedPref(it, context)
        }
    }



    fun saveStockValue(
        administeredProduct: String,
        targetDisease: String,
        context: Context
    ): ArrayList<DbVaccineStockDetails> {
        val stockList = ArrayList<DbVaccineStockDetails>()

        val immunizationHandler = ImmunizationHandler()
        val baseVaccineDetails =
            immunizationHandler.getVaccineDetailsByBasicVaccineName(administeredProduct)
        val vaccineDetails =
            immunizationHandler.getRoutineVaccineDetailsBySeriesTargetName(targetDisease)

        if (vaccineDetails != null && baseVaccineDetails != null) {

            var seriesDoses = ""

            seriesDoses = when (vaccineDetails) {
                is RoutineVaccine -> {
                    "${vaccineDetails.seriesDoses}"
                }

                is NonRoutineVaccine -> {
                    val nonRoutineVaccine =
                        vaccineDetails.vaccineList.firstOrNull() { it.targetDisease == targetDisease }
                    "${nonRoutineVaccine?.seriesDoses}"
                }

                is PregnancyVaccine -> {
                    "${vaccineDetails.seriesDoses}"
                }

                else -> {
                    ""
                }
            }

            stockList.addAll(
                listOf(
                    DbVaccineStockDetails("vaccinationTargetDisease", targetDisease),
                    DbVaccineStockDetails("administeredProduct", administeredProduct),

                    DbVaccineStockDetails(
                        "vaccinationSeriesDoses",
                        seriesDoses
                    ),

                    DbVaccineStockDetails(
                        "vaccinationDoseQuantity",
                        baseVaccineDetails.doseQuantity
                    ),
                    DbVaccineStockDetails("vaccinationDoseNumber", baseVaccineDetails.doseNumber),
                    DbVaccineStockDetails("vaccinationBrand", baseVaccineDetails.vaccineName),
                    DbVaccineStockDetails(
                        "vaccinationSite",
                        baseVaccineDetails.administrativeMethod
                    ),

                    DbVaccineStockDetails("vaccinationExpirationDate", ""),
                    DbVaccineStockDetails("vaccinationBatchNumber", ""),
                    DbVaccineStockDetails("vaccinationManufacturer", "")
                )
            )

            //Save to shared pref
            stockList.forEach {
                saveSharedPref(it.name, it.value, context)
            }

        }
        return stockList


    }

    fun formatString(input: String): String {
        val words = input.split("(?=[A-Z])".toRegex())
        val result = words.joinToString(" ") { it.capitalize() }
        return result
    }


    fun getFormattedAge(
        dob: String?,
        resources: Resources,
    ): String {
        if (dob == null) return ""

        val dobFormat = convertDateFormat(dob)
        if (dobFormat != null) {
            val dobDate = convertStringToDate(dobFormat, "MMM d yyyy")
            if (dobDate != null) {
                val finalDate = convertDateToLocalDate(dobDate)
                val period = Period.between(finalDate, LocalDate.now())

                val years = period.years
                val months = period.months
                val days = period.days

                val ageStringBuilder = StringBuilder()

                if (years > 0) {
                    ageStringBuilder.append(resources.getQuantityString(R.plurals.ageYear, years, years))
                    if (months > 0 || days > 0) {
                        ageStringBuilder.append(", ")
                    }
                }

                if (months > 0) {
                    ageStringBuilder.append(resources.getQuantityString(R.plurals.ageMonth, months, months))
                    if (days > 0) {
                        ageStringBuilder.append(", ")
                    }
                }

                if (days > 0) {
                    ageStringBuilder.append(resources.getQuantityString(R.plurals.ageDay, days, days))
                }

                return ageStringBuilder.toString()
            }
        }

        return ""
    }


    fun generateRandomCode(): String {
        // Get current date
        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("ddMMyyyy")
        val formattedDate = dateFormat.format(currentDate)

        /**
         * The code works as follows,
         * The first Code represents the year, month, date represented as per in the alphabetical order
         * The date is added as is
         * The last 4 letters are random number
         */

        // Extract year, month, and day
        val year = formattedDate.substring(4)
        val month = formattedDate.substring(2, 4)
        val day = formattedDate.substring(0, 2)

        // Generate the first three characters
        val firstChar = ('A'.toInt() + year.toInt() % 10).toChar()
        val secondChar = ('A'.toInt() + month.toInt()).toChar()
        val thirdChar = day

        // Generate the next four characters
        val randomChars = generateRandomChars(4)

        // Combine all parts to form the final code
        return "$firstChar$secondChar$thirdChar$randomChars"
    }

    fun generateRandomChars(n: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..n)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
    }

    fun calculateWeeksFromDate(dateString: String): Int? {
        val currentDate = LocalDate.now()
        val givenDate = LocalDate.parse(dateString)

        // Calculate the difference in weeks
        val weeksDifference = ChronoUnit.WEEKS.between(givenDate, currentDate)

        return weeksDifference.toString().toIntOrNull()
    }

    fun getNextDate(date: Date, weeksToAdd: Double): Date {

        // Create a Calendar instance and set it to the current date
        val calendar = Calendar.getInstance()
        calendar.time = date

        // Add the calculated milliseconds to the current date
        calendar.add(Calendar.WEEK_OF_YEAR, weeksToAdd.toInt())

        // Get the new date after adding weeks
        return calendar.time
    }

    fun daysBetweenTodayAndGivenDate(inputDate: String): Long? {
        try {
            val dobFormat = FormatterClass().convertDateFormat(inputDate)

            // Parse the input date
            if (dobFormat != null){
                val dateFormat = SimpleDateFormat("MMM d yyyy", Locale.getDefault())
                val parsedDate = dateFormat.parse(dobFormat)

                // Get the current date
                val currentDate = Calendar.getInstance().time

                // Calculate the difference in days
                if (parsedDate != null) {
                    val diffInMillis = abs(parsedDate.time - currentDate.time)
                    return diffInMillis / (24 * 60 * 60 * 1000)
                }
            }

        } catch (e: Exception) {
            // Handle parsing errors or other exceptions
            e.printStackTrace()
        }

        // Return null if there's an error
        return null
    }

    fun generateSubCounties(): List<String> {
        return listOf(
            "PR-address-sub-county-mombasa",
            "PR-address-sub-county-kwale",
            "PR-address-sub-county-kilifi",
            "PR-address-sub-county-tana-river",
            "PR-address-sub-county-lamu",
            "PR-address-sub-county-taita-taveta",
            "PR-address-sub-county-garissa",
            "PR-address-sub-county-wajir",
            "PR-address-sub-county-mandera",
            "PR-address-sub-county-marsabit",
            "PR-address-sub-county-isolo",
            "PR-address-sub-county-meru",
            "PR-address-sub-county-tharaka-nithi",
            "PR-address-sub-county-embu",
            "PR-address-sub-county-kitui",
            "PR-address-sub-county-machakos",
            "PR-address-sub-county-makueni",
            "PR-address-sub-county-nyandarua",
            "PR-address-sub-county-nyeri",
            "PR-address-sub-county-kirinyaga",
            "PR-address-sub-county-murang'a",
            "PR-address-sub-county-kiambu",
            "PR-address-sub-county-turkana",
            "PR-address-sub-county-west-pokot",
            "PR-address-sub-county-samburu",
            "PR-address-sub-county-trans-nzoia",
            "PR-address-sub-county-uasin-gishu",
            "PR-address-sub-county-elgeyo-marakwet",
            "PR-address-sub-county-nandi",
            "PR-address-sub-county-baringo",
            "PR-address-sub-county-laikipia",
            "PR-address-sub-county-nakuru",
            "PR-address-sub-county-narok",
            "PR-address-sub-county-kajiado",
            "PR-address-sub-county-kericho",
            "PR-address-sub-county-bomet",
            "PR-address-sub-county-kakamega",
            "PR-address-sub-county-vihiga",
            "PR-address-sub-county-bungoma",
            "PR-address-sub-county-busia",
            "PR-address-sub-county-siaya",
            "PR-address-sub-county-kisumu",
            "PR-address-sub-county-homa-bay",
            "PR-address-sub-county-migori",
            "PR-address-sub-county-kisii",
            "PR-address-sub-county-nyamira",
            "PR-address-sub-county-nairobi"
        )
    }

    fun generateWardCounties(): List<String> {
        return listOf(
            "PR-address-ward-changamwe",
            "PR-address-ward-jomvu",
            "PR-address-ward-kisauni",
            "PR-address-ward-nyali",
            "PR-address-ward-likoni",
            "PR-address-ward-mvita",
            "PR-address-ward-msambweni",
            "PR-address-ward-lungalunga",
            "PR-address-ward-matuga",
            "PR-address-ward-kinango",
            "PR-address-ward-kilifi-north",
            "PR-address-ward-kilifi-south",
            "PR-address-ward-kaloleni",
            "PR-address-ward-rabai",
            "PR-address-ward-ganze",
            "PR-address-ward-malindi",
            "PR-address-ward-magarini",
            "PR-address-ward-garsen",
            "PR-address-ward-galole",
            "PR-address-ward-bura",
            "PR-address-ward-lamu-east",
            "PR-address-ward-lamu-west",
            "PR-address-ward-taveta",
            "PR-address-ward-wundanyi",
            "PR-address-ward-mwatate",
            "PR-address-ward-voi",
            "PR-address-ward-garissa-township",
            "PR-address-ward-balambala",
            "PR-address-ward-lagdera",
            "PR-address-ward-dadaab",
            "PR-address-ward-fafi",
            "PR-address-ward-ijara",
            "PR-address-ward-wajir-north",
            "PR-address-ward-wajir-east",
            "PR-address-ward-tarbaj",
            "PR-address-ward-wajir-west",
            "PR-address-ward-eldas",
            "PR-address-ward-wajir-south",
            "PR-address-ward-mandera-west",
            "PR-address-ward-banissa",
            "PR-address-ward-mandera-north",
            "PR-address-ward-mandera-south",
            "PR-address-ward-mandera-east",
            "PR-address-ward-lafey",
            "PR-address-ward-moyale",
            "PR-address-ward-north-horr",
            "PR-address-ward-saku",
            "PR-address-ward-laisamis",
            "PR-address-ward-isiolo-north",
            "PR-address-ward-isiolo-south",
            "PR-address-ward-igembe-south",
            "PR-address-ward-igembe-central",
            "PR-address-ward-igembe-north",
            "PR-address-ward-tigania-west",
            "PR-address-ward-tigania-east",
            "PR-address-ward-north-imenti",
            "PR-address-ward-buuri",
            "PR-address-ward-central-imenti",
            "PR-address-ward-south-imenti",
            "PR-address-ward-maara",
            "PR-address-ward-chuka-igambang'om",
            "PR-address-ward-tharaka",
            "PR-address-ward-manyatta",
            "PR-address-ward-runyenjes",
            "PR-address-ward-mbeere-south",
            "PR-address-ward-mbeere-north",
            "PR-address-ward-mwingi-north",
            "PR-address-ward-mwingi-west",
            "PR-address-ward-mwingi-central",
            "PR-address-ward-kitui-west",
            "PR-address-ward-kitui-rural",
            "PR-address-ward-kitui-central",
            "PR-address-ward-kitui-east",
            "PR-address-ward-kitui-south",
            "PR-address-ward-masinga",
            "PR-address-ward-yatta",
            "PR-address-ward-kangundo",
            "PR-address-ward-matungulu",
            "PR-address-ward-kathiani",
            "PR-address-ward-mavoko",
            "PR-address-ward-machakos-town",
            "PR-address-ward-mwala",
            "PR-address-ward-mbooni",
            "PR-address-ward-kilome",
            "PR-address-ward-kaiti",
            "PR-address-ward-makueni",
            "PR-address-ward-kibwezi-west",
            "PR-address-ward-kibwezi-east",
            "PR-address-ward-kinangop",
            "PR-address-ward-kipipiri",
            "PR-address-ward-ol-kalou",
            "PR-address-ward-ol-jorok",
            "PR-address-ward-ndaragwa",
            "PR-address-ward-tetu",
            "PR-address-ward-kieni",
            "PR-address-ward-mathira",
            "PR-address-ward-othaya",
            "PR-address-ward-mukurweini",
            "PR-address-ward-nyeri-town",
            "PR-address-ward-mwea",
            "PR-address-ward-gichugu",
            "PR-address-ward-ndia",
            "PR-address-ward-kirinyaga-central",
            "PR-address-ward-kangema",
            "PR-address-ward-mathioya",
            "PR-address-ward-kiharu",
            "PR-address-ward-kigumo",
            "PR-address-ward-maragwa",
            "PR-address-ward-kandara",
            "PR-address-ward-gatanga",
            "PR-address-ward-gatundu-south",
            "PR-address-ward-gatundu-north",
            "PR-address-ward-juja",
            "PR-address-ward-thika-town",
            "PR-address-ward-ruiru",
            "PR-address-ward-githunguri",
            "PR-address-ward-kiambu",
            "PR-address-ward-kiambaa",
            "PR-address-ward-kabete",
            "PR-address-ward-kikuyu",
            "PR-address-ward-limuru",
            "PR-address-ward-lari",
            "PR-address-ward-turkana-north",
            "PR-address-ward-turkana-west",
            "PR-address-ward-turkana-central",
            "PR-address-ward-loima",
            "PR-address-ward-turkana-south",
            "PR-address-ward-turkana-east",
            "PR-address-ward-kapenguria",
            "PR-address-ward-sigor",
            "PR-address-ward-kacheliba",
            "PR-address-ward-pokot-south",
            "PR-address-ward-samburu-west",
            "PR-address-ward-samburu-north",
            "PR-address-ward-samburu-east",
            "PR-address-ward-kwanza",
            "PR-address-ward-endebess",
            "PR-address-ward-saboti",
            "PR-address-ward-kiminini",
            "PR-address-ward-cherangany",
            "PR-address-ward-soy",
            "PR-address-ward-turbo",
            "PR-address-ward-moiben",
            "PR-address-ward-ainabkoi",
            "PR-address-ward-kapseret",
            "PR-address-ward-kesses",
            "PR-address-ward-marakwet-east",
            "PR-address-ward-marakwet-west",
            "PR-address-ward-keiyo-north",
            "PR-address-ward-keiyo-south",
            "PR-address-ward-tinderet",
            "PR-address-ward-aldai",
            "PR-address-ward-nandi-hills",
            "PR-address-ward-chesumei",
            "PR-address-ward-emgwen",
            "PR-address-ward-mosop",
            "PR-address-ward-tiaty",
            "PR-address-ward-baringo--north",
            "PR-address-ward-baringo-central",
            "PR-address-ward-baringo-south",
            "PR-address-ward-mogotio",
            "PR-address-ward-eldama-ravine",
            "PR-address-ward-laikipia-west",
            "PR-address-ward-laikipia-east",
            "PR-address-ward-laikipia-north",
            "PR-address-ward-molo",
            "PR-address-ward-njoro",
            "PR-address-ward-naivasha",
            "PR-address-ward-gilgil",
            "PR-address-ward-kuresoi-south",
            "PR-address-ward-kuresoi-north",
            "PR-address-ward-subukia",
            "PR-address-ward-rongai",
            "PR-address-ward-bahati",
            "PR-address-ward-nakuru-town-west",
            "PR-address-ward-nakuru-town-east",
            "PR-address-ward-kilgoris",
            "PR-address-ward-emurua-dikirr",
            "PR-address-ward-narok-north",
            "PR-address-ward-narok-east",
            "PR-address-ward-narok-south",
            "PR-address-ward-narok-west",
            "PR-address-ward-kajiado-north",
            "PR-address-ward-kajiado-central",
            "PR-address-ward-kajiado-east",
            "PR-address-ward-kajiado-west",
            "PR-address-ward-kajiado-south",
            "PR-address-ward-kipkelion-east",
            "PR-address-ward-kipkelion-west",
            "PR-address-ward-ainamoi",
            "PR-address-ward-bureti",
            "PR-address-ward-belgut",
            "PR-address-ward-sigowet-soin",
            "PR-address-ward-sotik",
            "PR-address-ward-chepalungu",
            "PR-address-ward-bomet-east",
            "PR-address-ward-bomet-central",
            "PR-address-ward-konoin",
            "PR-address-ward-lugari",
            "PR-address-ward-likuyani",
            "PR-address-ward-malava",
            "PR-address-ward-lurambi",
            "PR-address-ward-navakholo",
            "PR-address-ward-mumias-west",
            "PR-address-ward-mumias-east",
            "PR-address-ward-matungu",
            "PR-address-ward-butere",
            "PR-address-ward-khwisero",
            "PR-address-ward-shinyalu",
            "PR-address-ward-ikolomani",
            "PR-address-ward-vihiga",
            "PR-address-ward-sabatia",
            "PR-address-ward-hamisi",
            "PR-address-ward-luanda",
            "PR-address-ward-emuhaya",
            "PR-address-ward-mt.elgon",
            "PR-address-ward-sirisia",
            "PR-address-ward-kabuchai",
            "PR-address-ward-bumula",
            "PR-address-ward-kanduyi",
            "PR-address-ward-webuye-east",
            "PR-address-ward-webuye-west",
            "PR-address-ward-kimilili",
            "PR-address-ward-tongaren",
            "PR-address-ward-teso-north",
            "PR-address-ward-teso-south",
            "PR-address-ward-nambale",
            "PR-address-ward-matayos",
            "PR-address-ward-butula",
            "PR-address-ward-funyula",
            "PR-address-ward-budalangi",
            "PR-address-ward-ugenya",
            "PR-address-ward-ugunja",
            "PR-address-ward-alego-usonga",
            "PR-address-ward-gem",
            "PR-address-ward-bondo",
            "PR-address-ward-rarieda",
            "PR-address-ward-kisumu-east",
            "PR-address-ward-kisumu-west",
            "PR-address-ward-kisumu-central",
            "PR-address-ward-seme",
            "PR-address-ward-nyando",
            "PR-address-ward-muhoroni",
            "PR-address-ward-nyakach",
            "PR-address-ward-kasipul",
            "PR-address-ward-kabondo-kasipul",
            "PR-address-ward-karachuonyo",
            "PR-address-ward-rangwe",
            "PR-address-ward-homa-bay-town",
            "PR-address-ward-ndhiwa",
            "PR-address-ward-mbita",
            "PR-address-ward-suba",
            "PR-address-ward-rongo",
            "PR-address-ward-awendo",
            "PR-address-ward-suna-east",
            "PR-address-ward-suna-west",
            "PR-address-ward-uriri",
            "PR-address-ward-nyatike",
            "PR-address-ward-kuria-west",
            "PR-address-ward-kuria-east",
            "PR-address-ward-bonchari",
            "PR-address-ward-south-mugirango",
            "PR-address-ward-bomachoge-borabu",
            "PR-address-ward-bobasi",
            "PR-address-ward-bomachoge-chache",
            "PR-address-ward-nyaribari-masaba",
            "PR-address-ward-nyaribari-chache",
            "PR-address-ward-kitutu-chache-north",
            "PR-address-ward-kitutu-chache-south",
            "PR-address-ward-kitutu-masaba",
            "PR-address-ward-west-mugirango",
            "PR-address-ward-north-mugirango",
            "PR-address-ward-borabu",
            "PR-address-ward-westlands",
            "PR-address-ward-dagoretti-north",
            "PR-address-ward-dagoretti-south",
            "PR-address-ward-langata",
            "PR-address-ward-kibra",
            "PR-address-ward-roysambu",
            "PR-address-ward-kasarani",
            "PR-address-ward-ruaraka",
            "PR-address-ward-embakasi-south",
            "PR-address-ward-embakasi-north",
            "PR-address-ward-embakasi-central",
            "PR-address-ward-embakasi-east",
            "PR-address-ward-embakasi-west",
            "PR-address-ward-makadara",
            "PR-address-ward-kamukunji",
            "PR-address-ward-starehe",
            "PR-address-ward-mathare"
        )
    }


}