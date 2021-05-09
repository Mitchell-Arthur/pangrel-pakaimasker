package com.pangrel.pakaimasker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.pangrel.pakaimasker.databinding.ActivityLandingBinding
import androidx.fragment.app.commit
import com.pangrel.pakaimasker.landing.IntroductionFragment

class LandingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLandingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityLandingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.commit {
            add(binding.container.id, IntroductionFragment())
        }
    }

    override fun onBackPressed() {
        finishAffinity()
    }

    fun closeIntroduction() {
        finish()
    }
}
