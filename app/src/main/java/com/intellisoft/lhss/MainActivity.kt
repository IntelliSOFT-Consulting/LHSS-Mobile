package com.intellisoft.lhss

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.intellisoft.lhss.databinding.ActivityMainBinding

const val MAX_RESOURCE_COUNT = 20

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initActionBar()
        initNavigationDrawer()
        observeLastSyncTime()
        viewModel.updateLastSyncTimestamp()
    }

    override fun onBackPressed() {
        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawer(GravityCompat.START)
            return
        }
        super.onBackPressed()
    }

    fun setDrawerEnabled(enabled: Boolean) {
        val lockMode =
            if (enabled) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        binding.drawer.setDrawerLockMode(lockMode)
        drawerToggle.isDrawerIndicatorEnabled = enabled
    }

    fun openNavigationDrawer() {
        binding.drawer.openDrawer(GravityCompat.START)
        viewModel.updateLastSyncTimestamp()
    }

    private fun initActionBar() {
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
    }

    private fun initNavigationDrawer() {
        binding.navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected)
        drawerToggle = ActionBarDrawerToggle(this, binding.drawer, R.string.open, R.string.close)
        binding.drawer.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
    }

    private fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_sync -> {
                viewModel.triggerOneTimeSync()
                binding.drawer.closeDrawer(GravityCompat.START)
                return false
            }
        }
        return false
    }

    private fun observeLastSyncTime() {
        viewModel.lastSyncTimestampLiveData.observe(this) {
            binding.navigationView.getHeaderView(0).findViewById<TextView>(R.id.last_sync_tv).text = it
        }
    }
}