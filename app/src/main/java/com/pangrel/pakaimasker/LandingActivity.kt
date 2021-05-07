package com.pangrel.pakaimasker

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.pangrel.pakaimasker.databinding.ActivityLandingBinding
import kotlin.system.exitProcess
import android.content.SharedPreferences
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.pangrel.pakaimasker.landing.IntroductionFragment
import com.pangrel.pakaimasker.landing.LoginFragment

class LandingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLandingBinding
    private lateinit var fragmentManager: FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityLandingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fragmentManager = supportFragmentManager

        if (isFirstTime(getPreferences(Context.MODE_PRIVATE))){
            // load introduction fragment
            introduction()
        }else{

        }
    }

    override fun onBackPressed() {
        val count = supportFragmentManager.backStackEntryCount

        if (count <= 1) {
            exitProcess(0)
        } else {
            supportFragmentManager.popBackStack()
        }
    }

    private fun isFirstTime(preferences: SharedPreferences): Boolean {
        // for reading data from local directory
        return preferences.getBoolean("isFirstTime", true)
    }
    private fun introduction() {
        supportFragmentManager.commit {
            add(binding.container.id, IntroductionFragment())
        }
    }
    fun closeIntroduction() {
        supportFragmentManager.commit {
            replace(binding.container.id, LoginFragment())
        }
    }
}
