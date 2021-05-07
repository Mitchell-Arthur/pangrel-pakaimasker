package com.pangrel.pakaimasker.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pangrel.pakaimasker.R
import com.pangrel.pakaimasker.databinding.FragmentIntroductionBinding


class IntroductionFragment : Fragment() {
    private lateinit var binding: FragmentIntroductionBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIntroductionBinding.inflate(inflater, container, false)
        binding.pager.adapter = activity?.let { IntroductionAdapter(it) }
        return binding.root
    }
}