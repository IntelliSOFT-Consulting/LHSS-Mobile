package com.intellisoft.lhss

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.intellisoft.lhss.databinding.ActivityMainBinding
import com.intellisoft.lhss.fhir.data.FormatterClass
import com.intellisoft.lhss.viewmodel.MainActivityViewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private val formatter = FormatterClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = findViewById<Toolbar>(R.id.toolbar) // Assuming you have a Toolbar with id 'toolbar' in your layout
        setSupportActionBar(toolbar)

        viewModel.updateLastSyncTimestamp()
        viewModel.triggerOneTimeSync()


        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_bottem_navigation) as NavHostFragment
        val navController = navHostFragment.navController
        navController.navigate(R.id.home_patient_list)




    }
}