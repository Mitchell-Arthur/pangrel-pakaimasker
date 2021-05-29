package com.pangrel.pakaimasker

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.pangrel.pakaimasker.databinding.ActivityLandingBinding
import androidx.fragment.app.commit

class LandingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLandingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityLandingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val introPage = intent.getIntExtra("extra_intro_page", 0)
        if (introPage == 0){
            supportFragmentManager.commit {
                add(binding.container.id, IntroductionFragment())
            }
        }else if (introPage == 4){
            supportFragmentManager.commit {
                add(binding.container.id, IntroductionFragment(4))
            }
        }
    }

    override fun onBackPressed() {
        finishAffinity()
    }

    fun closeIntroduction() {
        setResult(Activity.RESULT_OK)
        finish()
    }
}
