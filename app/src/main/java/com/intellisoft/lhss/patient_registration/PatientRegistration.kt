package com.intellisoft.lhss.patient_registration

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.intellisoft.lhss.R
import com.intellisoft.lhss.databinding.ActivityMainBinding
import com.intellisoft.lhss.databinding.ActivityPatientRegistrationBinding
import com.intellisoft.lhss.viewmodel.MainActivityViewModel

class PatientRegistration : AppCompatActivity() {

    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var binding: ActivityPatientRegistrationBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val layout = findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar_layout)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_activity_bottem_navigation)

        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.patientDetailsFragment, R.id.patientLocationFragment, R.id.regPreviewFragment))
        findViewById<Toolbar>(R.id.toolbar)
            .setupWithNavController(navController, appBarConfiguration)


        layout.setupWithNavController(toolbar, navController, appBarConfiguration)

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
//        navController.navigate(R.id.patientDetailsFragment)
    }
}