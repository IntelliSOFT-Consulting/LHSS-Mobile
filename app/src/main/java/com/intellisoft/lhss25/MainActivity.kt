package com.intellisoft.lhss25

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.intellisoft.lhss25.shared.FormatterClass


class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var formatterClass: FormatterClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        formatterClass = FormatterClass(this)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // Set up NavController
        val navHostFragment =
            supportFragmentManager.findFragmentById(
                R.id.nav_host_fragment_activity_bottom_navigation)
                    as NavHostFragment
        navController = navHostFragment.navController

        // Set up AppBarConfiguration with the top-level destinations
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.splashFragment, // Your top-level destinations (no back button)
                R.id.loginFragment,
                R.id.landingPageFragment,
            )
        )

        // Set up the ActionBar to work with NavController
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Listen for changes in the destination to update back button visibility
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Show or hide the back button depending on the fragment
            supportActionBar?.setDisplayHomeAsUpEnabled(
                appBarConfiguration.topLevelDestinations.contains(destination.id).not()
            )
        }

        NavigationUI.setupWithNavController(bottomNavigationView, navController)

        // List of fragments where the BottomNavigationView should be hidden
        val fragmentsToHideBottomNav = setOf(
            R.id.splashFragment,  // Add fragment IDs where BottomNav should be hidden
//            R.id.landingPageFragment,
            R.id.loginFragment,
            R.id.recoverPasswordFragment,
            R.id.newPasswordFragment,
        )

        // Add destination change listener to hide/show BottomNavigationView
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id in fragmentsToHideBottomNav) {
                bottomNavigationView.visibility = BottomNavigationView.GONE
            } else {
                bottomNavigationView.visibility = BottomNavigationView.VISIBLE
            }

            if (destination.id == R.id.patientCardFragment ){
                toolbar.foregroundGravity = Gravity.CENTER
            }
        }

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.homeFragment -> {
                    // Handle Home action
                    navController.navigate(R.id.landingPageFragment)
                    true
                }
                R.id.patientsFragment -> {
                    // Handle Profile action
                    navController.navigate(R.id.patientListFragment)

                    true
                }
                R.id.profileFragment -> {
                    // Handle Settings action
                    navController.navigate(R.id.profileFragment2)

                    true
                }
                else -> false
            }
        }


    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        val navController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation) // Replace with your NavHostFragment ID
        if (navController.currentDestination?.id == R.id.patientCardFragment) { // Current fragment to handle back press
            clearBackStackAndNavigateToFragment()
        } else {
            super.onBackPressed() // Default behavior
        }
    }

    private fun clearBackStackAndNavigateToFragment() {
        val navController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation) // Replace with your NavHostFragment ID

        // Clear the back stack
        navController.popBackStack(navController.graph.startDestinationId, false)

        // Navigate to your specific fragment
        navController.navigate(R.id.landingPageFragment) // Replace with your target fragment ID
    }

    // Override to handle back button clicks
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()

        formatterClass.clearData()
    }
}