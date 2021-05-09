package com.pangrel.pakaimasker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        supportActionBar?.hide()
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_history, R.id.navigation_profile
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        if (isFirstTime(getPreferences(Context.MODE_PRIVATE))){
            introduction()
        }
    }

    private fun isFirstTime(preferences: SharedPreferences): Boolean {
        // for reading data from local directory
        return preferences.getBoolean("isFirstTime", true) // if null then return true as default value
    }
    private fun introduction() {
        startActivity(Intent(this, LandingActivity::class.java))
        val sharedPref = this.getSharedPreferences(
            "isFirstTime", Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            putBoolean("isFirstTime", false)
            apply()
        }
    }
}