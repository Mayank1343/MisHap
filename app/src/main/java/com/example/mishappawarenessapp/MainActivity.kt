package com.example.mishappawarenessapp
import android.view.Menu


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.mishappawarenessapp.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate binding layout
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppBar)


        // ✅ Setup NavController safely
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // ✅ Connect BottomNavigationView to NavController
        binding.bottomNav.setupWithNavController(navController)

        // ✅ Connect Toolbar to NavController
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {

                R.id.action_notifications -> {
                    navController.navigate(R.id.alertsFragment)
                    true
                }

                else -> false
            }
        }

        binding.topAppBar.menu
            .findItem(R.id.action_notifications)
            ?.icon
            ?.setTint(getColor(android.R.color.white))


        // After bottom nav setup
        val badge = binding.bottomNav.getOrCreateBadge(R.id.alertsFragment)
        badge.isVisible = true
        badge.number = 3



        // ✅ FloatingActionButton click listener
        binding.fabPost.setOnClickListener {
            navController.navigate(R.id.postFragment)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_app_bar_menu, menu)
        return true
    }


    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
