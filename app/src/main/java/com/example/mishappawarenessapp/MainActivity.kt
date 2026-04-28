package com.example.mishappawarenessapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.mishappawarenessapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. FORCE LIGHT MODE (Prevents sudden dark theme)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)

        // 2. Setup View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 3. Setup Toolbar & App Title
        setSupportActionBar(binding.topAppBar)
        supportActionBar?.title = "MisHap"

        // 4. Setup NavController
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // 5. NAVIGATION LISTENER (Fixed Return Types)
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    navController.popBackStack(R.id.homeFragment, false)
                    true
                }
                R.id.mapFragment -> {
                    NavigationUI.onNavDestinationSelected(item, navController)
                    true
                }
                R.id.profileFragment -> {
                    NavigationUI.onNavDestinationSelected(item, navController)
                    true
                }
                else -> false
            }
        }

        // 6. FAB Click Listener
        binding.fabPost.setOnClickListener {
            navController.navigate(R.id.postFragment)
        }

        // 7. KEYBOARD LISTENER (Fixes Bottom Nav sliding up)
        // This detects when the screen height changes (i.e., keyboard opens)
        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = android.graphics.Rect()
            binding.root.getWindowVisibleDisplayFrame(rect)
            val screenHeight = binding.root.rootView.height
            val keypadHeight = screenHeight - rect.bottom

            // If keypad takes up more than 15% of screen, hide the nav bars
            if (keypadHeight > screenHeight * 0.15) {
                binding.bottomNav.visibility = View.GONE
                binding.fabPost.visibility = View.GONE
            } else {
                binding.bottomNav.visibility = View.VISIBLE
                binding.fabPost.visibility = View.VISIBLE
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}