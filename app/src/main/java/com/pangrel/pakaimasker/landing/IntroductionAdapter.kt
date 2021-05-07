package com.pangrel.pakaimasker.landing

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class IntroductionAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        var fragment: Fragment? = null
        when (position) {
            0 -> fragment = IntroductionPage1()
            1 -> fragment = IntroductionPage2()
            2 -> fragment = IntroductionPage3()
            3 -> fragment = IntroductionPage4()
            4 -> fragment = IntroductionPage5()
        }
        return fragment as Fragment
    }
}