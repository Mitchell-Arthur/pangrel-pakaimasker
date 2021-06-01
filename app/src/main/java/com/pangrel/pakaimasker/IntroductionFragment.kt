package com.pangrel.pakaimasker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.pangrel.pakaimasker.databinding.FragmentIntroductionBinding
import kotlinx.android.synthetic.main.fragment_introduction.*


class IntroductionFragment(private val currentPage: Int = 0) : Fragment() {
    private lateinit var binding: FragmentIntroductionBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIntroductionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.pager.adapter = activity?.let { IntroductionAdapter(it) }
        binding.pager.currentItem = currentPage

        addBottomDots()
        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentDots(position)
            }

        })
    }

    private fun addBottomDots() {
        val dots = arrayOfNulls<ImageView>(5)
        val layoutParams: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        layoutParams.setMargins(5, 0, 5, 0)
        for (i in dots.indices) {
            dots[i] = ImageView(activity?.applicationContext)
            dots[i].apply {
                this?.setImageDrawable(
                    activity?.let {
                        ContextCompat.getDrawable(
                            it.applicationContext,
                            R.drawable.inactive
                        )
                    }
                )
                this?.layoutParams = layoutParams
            }
            layoutDots.addView(dots[i])
        }
    }

    private fun setCurrentDots(index: Int) {
        val childCount: Int = layoutDots.childCount
        for (i in 0 until childCount) {
            val imageView: ImageView = layoutDots[i] as ImageView
            if (i == index) {
                imageView.setImageDrawable(
                    activity?.let {
                        ContextCompat.getDrawable(
                            it.applicationContext,
                            R.drawable.active
                        )
                    }
                )
            } else {
                imageView.setImageDrawable(
                    activity?.let {
                        ContextCompat.getDrawable(
                            it.applicationContext,
                            R.drawable.inactive
                        )
                    }
                )
            }
        }
    }
}