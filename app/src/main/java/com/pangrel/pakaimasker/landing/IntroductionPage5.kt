package com.pangrel.pakaimasker.landing

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pangrel.pakaimasker.LandingActivity
import com.pangrel.pakaimasker.databinding.FragmentIntroductionPage5Binding

class IntroductionPage5 : Fragment() {
    private lateinit var binding: FragmentIntroductionPage5Binding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentIntroductionPage5Binding.inflate(inflater, container, false)
        binding.btnLogin.setOnClickListener {
            (activity as LandingActivity).closeIntroduction()
        }
        binding.btnSignUp.setOnClickListener {
            (activity as LandingActivity).closeIntroduction()
        }
        return binding.root
    }
}