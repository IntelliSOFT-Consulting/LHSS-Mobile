package com.intellisoft.lhss.detail

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.fhir.FhirEngine
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.intellisoft.lhss.MainActivity
import com.intellisoft.lhss.R
import com.intellisoft.lhss.databinding.ActivityPatientDetailBinding
import com.intellisoft.lhss.detail.ui.main.ReferralsFragment
import com.intellisoft.lhss.detail.ui.main.SectionsPagerAdapter
import com.intellisoft.lhss.detail.ui.main.adapters.PatientDetailDataAdapter
import com.intellisoft.lhss.detail.ui.main.routine.VisitHistory
import com.intellisoft.lhss.fhir.FhirApplication
import com.intellisoft.lhss.fhir.data.CustomPatient
import com.intellisoft.lhss.fhir.data.DbAdministrative
import com.intellisoft.lhss.fhir.data.DbPatientDataDetails
import com.intellisoft.lhss.fhir.data.FormatterClass
import com.intellisoft.lhss.fhir.data.NavigationDetails
import com.intellisoft.lhss.patient_registration.PatientRegistration
import com.intellisoft.lhss.utils.AppUtils
import com.intellisoft.lhss.viewmodel.PatientDetailsViewModel
import com.intellisoft.lhss.viewmodel.PatientDetailsViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class PatientDetailActivity : AppCompatActivity() {
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private val formatter = FormatterClass()

    //    private val args: PatientDetailActivityArgs by navArgs()
    private lateinit var binding: ActivityPatientDetailBinding
    private var formatterClass = FormatterClass()
    private var country :String? = null
    private lateinit var layoutManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityPatientDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        patientId = FormatterClass().getSharedPref("patientId", this).toString()

        layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setHasFixedSize(true)

        val bundle =
            bundleOf("patient_id" to patientId)
        val toolbar =
            findViewById<Toolbar>(R.id.toolbar) // Assuming you have a Toolbar with id 'toolbar' in your layout
        setSupportActionBar(toolbar)

        fhirEngine = FhirApplication.fhirEngine(this)
        patientDetailsViewModel =
            ViewModelProvider(
                this,
                PatientDetailsViewModelFactory(this.application, fhirEngine, patientId),
            )
                .get(PatientDetailsViewModel::class.java)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val adapter = SectionsPagerAdapter(supportFragmentManager)

        val visitHistory = VisitHistory()
        visitHistory.arguments = bundle

        val referrals = ReferralsFragment()
        referrals.arguments = bundle

        adapter.addFragment(visitHistory, getString(R.string.tab_text_1))
        adapter.addFragment(referrals, getString(R.string.tab_text_2))

        binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // Set the background color of the selected tab dynamically
                tab?.view?.setBackgroundResource(R.color.colorPrimary)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Handle tab unselection if needed
                tab?.view?.setBackgroundResource(R.color.unselectedTab)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle tab reselection if needed
            }
        })

        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = adapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)

        getPatientDetails()

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Handle home item click
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_patients -> {
                    // Handle home item click
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("functionToCall", NavigationDetails.CLIENT_LIST.name)
                    intent.putExtra("patientId", patientId)
                    startActivity(intent)
                    true
                }
                R.id.navigation_profile -> {
                    // Handle home item click
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("functionToCall", NavigationDetails.PRACTITIONER_VIEW.name)
                    intent.putExtra("patientId", patientId)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        binding.btnAddressDetails.setOnClickListener {
            val patientDataDetailsList = patientDetailsViewModel.getAddressDetails()
            showCustomDialog(patientDataDetailsList)
        }

        binding.btnVisitHistory.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("functionToCall", NavigationDetails.VISIT_HISTORY.name)
            intent.putExtra("patientId", patientId)
            startActivity(intent)
        }

        binding.btnReferrals.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("functionToCall", NavigationDetails.REFERRAL_LIST.name)
            intent.putExtra("patientId", patientId)
            startActivity(intent)
        }

    }

    private fun showCustomDialog(patientDataDetailsList: ArrayList<DbPatientDataDetails>) {
        val dialog = Dialog(this)
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
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)

        val visitHistoryAdapter = PatientDetailDataAdapter(patientDataDetailsList, this@PatientDetailActivity)
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
            val firstName = parts.getOrNull(0) ?: ""
            val middleNameParts = parts.subList(1, parts.size - 1)
            val middleName = if (middleNameParts.isNotEmpty()) middleNameParts.joinToString(" ") else ""
            val lastName = parts.last()

            val customPatient = CustomPatient(
                firstName,
                middleName,
                lastName,
                gender,
                dateBirth,
                "")

            val dbAdministrative = DbAdministrative(
                identificationType, identificationNumber, occupationType, originCountry, residenceCountry, region, district
            )

            CoroutineScope(Dispatchers.IO).launch {
                formatter.saveSharedPref("isPatientUpdate","true", this@PatientDetailActivity)
                formatter.saveSharedPref("registrationFlowPersonal", Gson().toJson(customPatient), this@PatientDetailActivity)
                formatter.saveSharedPref("registrationFlowAdministrative", Gson().toJson(dbAdministrative), this@PatientDetailActivity)

            }



            country = formatterClass.getSharedPref("country", this@PatientDetailActivity)

            val patientDataDetailsList = patientDetailsViewModel.getUserDetails(this@PatientDetailActivity)

            CoroutineScope(Dispatchers.Main).launch {
                val visitHistoryAdapter = PatientDetailDataAdapter(patientDataDetailsList, this@PatientDetailActivity)
                binding.recyclerView.adapter = visitHistoryAdapter
            }



        }
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("functionToCall", NavigationDetails.CLIENT_LIST.name)
        intent.putExtra("patientId", patientId)
        startActivity(intent)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.patient_actions, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_add_visit -> {

                if (country != null){
                    if (country == "Ethiopia"){
                        FormatterClass().saveSharedPref(
                            "questionnaireJson",
                            "add-visit-ethiopia.json",
                            this
                        )
                    }
                    if (country == "Djibouti"){
                        FormatterClass().saveSharedPref(
                            "questionnaireJson",
                            "add-visit-djibouti.json",
                            this
                        )
                    }

                    FormatterClass().saveSharedPref(
                        "title",
                        "New Visit",
                        this
                    )
                    FormatterClass().saveSharedPref(
                        "lhssFlow",
                        "NEW_VISIT",
                        this
                    )
                    //Send to contraindications
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("functionToCall", NavigationDetails.ADD_VISIT_HISTORY.name)
                    intent.putExtra("patientId", patientId)
                    startActivity(intent)
                }else{
                    Toast.makeText(this, "The client does not have a country associated with him/her", Toast.LENGTH_SHORT).show()
                }

                true
            }
            R.id.menu_item_refer_patient -> {
                if (country != null){
                    if (country == "Ethiopia"){
                        FormatterClass().saveSharedPref(
                            "questionnaireJson",
                            "referral-form-ethiopia.json",
                            this
                        )
                    }
                    if (country == "Djibouti"){
                        FormatterClass().saveSharedPref(
                            "questionnaireJson",
                            "referral-form-djibouti.json",
                            this
                        )
                    }
                    FormatterClass().saveSharedPref(
                        "title",
                        "Referrals",
                        this
                    )
                    FormatterClass().saveSharedPref(
                        "lhssFlow",
                        "REFERRALS",
                        this
                    )
                    //Send to contraindications
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("functionToCall", NavigationDetails.ADD_REFERRAL_LIST.name)
                    intent.putExtra("patientId", patientId)
                    startActivity(intent)
                }else{
                    Toast.makeText(this, "The client does not have a country associated with him/her", Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.menu_item_edit_person -> {

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("functionToCall", NavigationDetails.REGISTER_VACCINE.name)
                intent.putExtra("patientId", patientId)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}