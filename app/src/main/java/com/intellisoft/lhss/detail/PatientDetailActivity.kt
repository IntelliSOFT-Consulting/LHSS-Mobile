package com.intellisoft.lhss.detail

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import com.intellisoft.lhss.MainActivity
import com.intellisoft.lhss.detail.ui.main.SectionsPagerAdapter
import com.intellisoft.lhss.detail.ui.main.ReferralsFragment
import com.intellisoft.lhss.fhir.FhirApplication
import com.intellisoft.lhss.utils.AppUtils
import com.intellisoft.lhss.viewmodel.PatientDetailsViewModel
import com.intellisoft.lhss.viewmodel.PatientDetailsViewModelFactory
import com.google.android.fhir.FhirEngine
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.intellisoft.lhss.R
import com.intellisoft.lhss.databinding.ActivityPatientDetailBinding
import com.intellisoft.lhss.detail.ui.main.routine.VisitHistory
import com.intellisoft.lhss.fhir.data.FormatterClass
import com.intellisoft.lhss.fhir.data.NavigationDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PatientDetailActivity : AppCompatActivity() {
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String

    //    private val args: PatientDetailActivityArgs by navArgs()
    private lateinit var binding: ActivityPatientDetailBinding
    private var formatterClass = FormatterClass()
    private var country :String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityPatientDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        patientId = FormatterClass().getSharedPref("patientId", this).toString()

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



                else -> false
            }
        }

    }


    private fun getPatientDetails() {

        CoroutineScope(Dispatchers.IO).launch {

            val patientDetail = patientDetailsViewModel.getPatientInfo()
            CoroutineScope(Dispatchers.Main).launch {
                binding.apply {
                    tvName.text = patientDetail.name
                    tvGender.text = AppUtils().capitalizeFirstLetter(patientDetail.gender)
                    tvSystemId.text = patientDetail.systemId

                    val dob = formatterClass.convertDateFormat(patientDetail.dob)
                    val age = formatterClass.getFormattedAge(patientDetail.dob,tvAge.context.resources)

                    tvDob.text = dob
                    tvAge.text = "$age old"

                }
            }
            country = formatterClass.getSharedPref("country", this@PatientDetailActivity)



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
        menuInflater.inflate(R.menu.edit_details, menu)
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
                    intent.putExtra("functionToCall", NavigationDetails.VISIT_HISTORY.name)
                    intent.putExtra("patientId", patientId)
                    startActivity(intent)
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
                    intent.putExtra("functionToCall", NavigationDetails.VISIT_HISTORY.name)
                    intent.putExtra("patientId", patientId)
                    startActivity(intent)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}