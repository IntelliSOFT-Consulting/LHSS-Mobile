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
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
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

        val toolbar = findViewById<Toolbar>(R.id.toolbar) // Assuming you have a Toolbar with id 'toolbar' in your layout
        setSupportActionBar(toolbar)



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

        val navController = findNavController(R.id.nav_host_fragment_activity_bottem_navigation)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.patientDataDetailFragment,
                R.id.administerVaccine,
                R.id.patientDetailsFragment,

                R.id.visitHistory,
                R.id.referralsFragment,
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        navController.navigate(R.id.patientDataDetailFragment)

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

                CoroutineScope(Dispatchers.IO).launch {
                    formatter.saveSharedPref("isPatientUpdate","true", this@PatientDetailActivity)
               }

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