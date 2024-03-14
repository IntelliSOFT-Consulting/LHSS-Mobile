package com.intellisoft.lhss

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.intellisoft.lhss.add_patient.AddPatientFragment
import com.intellisoft.lhss.databinding.ActivityMainBinding
import com.intellisoft.lhss.detail.ui.main.UpdateFragment
import com.intellisoft.lhss.fhir.data.FormatterClass
import com.intellisoft.lhss.fhir.data.NavigationDetails
import com.intellisoft.lhss.viewmodel.MainActivityViewModel

class MainActivity : AppCompatActivity() {
    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private val formatter = FormatterClass()


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar =
            findViewById<Toolbar>(R.id.toolbar) // Assuming you have a Toolbar with id 'toolbar' in your layout
        setSupportActionBar(toolbar)
        val navController = findNavController(R.id.nav_host_fragment_activity_bottem_navigation)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setHomeButtonEnabled(false)
            setHomeAsUpIndicator(null)
        }




        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navController.navigate(R.id.landing_page)
                    true
                }
                R.id.navigation_patients -> {
                    navController.navigate(R.id.patient_list)
                    true
                }
                R.id.navigation_profile -> {
                    navController.navigate(R.id.practionerDetails)
                    true
                }



                else -> false
            }
        }


        viewModel.updateLastSyncTimestamp()
        viewModel.triggerOneTimeSync()
        //        load initial landing page
        navController.navigate(R.id.landing_page)

        when (intent.getStringExtra("functionToCall")) {
            "registerFunction" -> {
                registerFunction()
            }
//            "updateFunction" -> {
//                val patientId = intent.getStringExtra("patientId")
//                if (patientId != null) {
//                    updateFunction(patientId)
//                }
//            }
//
//
//
//            "editFunction" -> {
//                val patientId = intent.getStringExtra("patientId")
//                if (patientId != null) {
//                    editFunction(patientId)
//                }
//            }
//
//            NavigationDetails.ADMINISTER_VACCINE.name -> {
//                val patientId = intent.getStringExtra("patientId")
//                if (patientId != null) {
//                    administerVaccine(patientId, R.id.administerVaccine)
//                }
//            }


            NavigationDetails.REFERRAL_DETAIL_VIEW.name -> {
                val patientId = intent.getStringExtra("patientId")
                if (patientId != null) {
                    manageCBD(patientId, R.id.fragmentReferralDetails)
                }
            }
            NavigationDetails.VISIT_HISTORY.name -> {
                val patientId = intent.getStringExtra("patientId")
                if (patientId != null) {
                    manageCBD(patientId, R.id.visitHistory)
                }
            }
            NavigationDetails.REFERRAL_LIST.name -> {
                val patientId = intent.getStringExtra("patientId")
                if (patientId != null) {
                    manageCBD(patientId, R.id.referralsFragment)
                }
            }

            NavigationDetails.ADD_VISIT_HISTORY.name -> {
                val patientId = intent.getStringExtra("patientId")
                if (patientId != null) {
                    administerVaccine(patientId, R.id.administerVaccine)
                }
            }
            NavigationDetails.ADD_REFERRAL_LIST.name -> {
                val patientId = intent.getStringExtra("patientId")
                if (patientId != null) {
                    administerVaccine(patientId, R.id.administerVaccine)
                }
            }
            NavigationDetails.DETAIL_VIEW.name -> {
                val patientId = intent.getStringExtra("patientId")
                if (patientId != null) {
                    manageCBD(patientId, R.id.detailFragment)
                }
            }
//
            NavigationDetails.CLIENT_LIST.name -> {
                val patientId = intent.getStringExtra("patientId")
                if (patientId != null) {
                    administerVaccine(patientId, R.id.patient_list)
                }
            }
            NavigationDetails.PRACTITIONER_VIEW.name -> {
                val patientId = intent.getStringExtra("patientId")
                if (patientId != null) {
                    navController.navigate(R.id.practionerDetails)
                }
            }
            NavigationDetails.REGISTER_VACCINE.name -> {
                val patientId = intent.getStringExtra("patientId")
                if (patientId != null) {
                    navController.navigate(R.id.patientDetailsFragment)
                }
            }
        }


        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.landing_page,
                R.id.patient_list,
//                R.id.updateFragment,
//                R.id.editPatientFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)


    }


    private fun administerVaccine(patientId: String, administerVaccine: Int) {
        val questionnaireJson = formatter.getSharedPref("questionnaireJson", this)
        formatter.saveSharedPref("patientId", patientId, this)

        val bundle = Bundle()
        bundle.putString(UpdateFragment.QUESTIONNAIRE_FRAGMENT_TAG, questionnaireJson)
        bundle.putString("patientId", patientId)
        findNavController(R.id.nav_host_fragment_activity_bottem_navigation)
            .navigate(administerVaccine, bundle)

    }

    private fun editFunction(patientId: String) {

        formatter.saveSharedPref("patientId", patientId, this)
        val bundle = Bundle()
        bundle.putString(UpdateFragment.QUESTIONNAIRE_FRAGMENT_TAG, "update.json")
        bundle.putString("patientId", patientId)
        findNavController(R.id.nav_host_fragment_activity_bottem_navigation).navigate(
            R.id.editPatientFragment, bundle
        )
    }



    private fun updateFunction(patientId: String) {
        val bundle = Bundle()
        bundle.putString(UpdateFragment.QUESTIONNAIRE_FRAGMENT_TAG, "update.json")
        bundle.putString("patientId", patientId)
        formatter.saveSharedPref("patientId", patientId, this)

        findNavController(R.id.nav_host_fragment_activity_bottem_navigation).navigate(
            R.id.updateFragment,
            bundle
        )
    }

    private fun manageCBD(patientId: String, navigationId:Int) {
        val bundle = Bundle()
        bundle.putString("patientId", patientId)
        formatter.saveSharedPref("patientId", patientId, this)

        findNavController(R.id.nav_host_fragment_activity_bottem_navigation).navigate(
            navigationId, bundle
        )
    }
    private fun referralsFragment(patientId: String) {
        val bundle = Bundle()
        bundle.putString("patientId", patientId)
        formatter.saveSharedPref("patientId", patientId, this)

        findNavController(R.id.nav_host_fragment_activity_bottem_navigation).navigate(
            R.id.referralsFragment,
            bundle
        )
    }
    private fun registerFunction() {
        val bundle = Bundle()
        bundle.putString(
            AddPatientFragment.QUESTIONNAIRE_FILE_PATH_KEY,
            "new-patient-registration-paginated.json")
        findNavController(R.id.nav_host_fragment_activity_bottem_navigation).navigate(
            R.id.addPatientFragment,
            bundle
        )
    }

}