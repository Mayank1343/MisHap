package com.example.mishappawarenessapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.mishappawarenessapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Setup View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Setup Toolbar (Top App Bar)
        setSupportActionBar(binding.topAppBar)

        // 3. Setup NavController Safely
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // 4. MANUAL NAVIGATION LISTENER (The Fix)
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    // This pops the backstack to Home and removes anything on top (like PostFragment)
                    navController.popBackStack(R.id.homeFragment, false)
                    true
                }
                R.id.mapFragment -> {
                    // Standard navigation for Map
                    NavigationUI.onNavDestinationSelected(item, navController)
                    true
                }
                R.id.profileFragment -> {
                    // Standard navigation for Profile
                    NavigationUI.onNavDestinationSelected(item, navController)
                    true
                }
                else -> false
            }
        }

        // 5. FloatingActionButton Click Listener
        binding.fabPost.setOnClickListener {
            // Navigate to PostFragment - it sits "on top" of the stack
            navController.navigate(R.id.postFragment)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}