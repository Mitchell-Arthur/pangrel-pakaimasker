package com.pangrel.pakaimasker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
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
        } else if (!isLogin(getPreferences(Context.MODE_PRIVATE))){
            toLogin()
        }
    }

    private fun isFirstTime(preferences: SharedPreferences): Boolean = preferences.getBoolean("isFirstTime", true)
    private fun introduction() {
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putBoolean("isFirstTime", false)
            apply()
        }
        startActivity(Intent(this, LandingActivity::class.java).putExtra("extra_intro_page", 0))
    }
    private fun isLogin(preferences: SharedPreferences): Boolean = preferences.getBoolean("isLogin", false)
    private fun toLogin(){
        startActivity(Intent(this, LandingActivity::class.java).putExtra("extra_intro_page", 4))
    }
}