package com.pangrel.pakaimasker.ui.profile

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.pangrel.pakaimasker.R
import kotlinx.android.synthetic.main.fragment_profile.*


class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnLanguage.setOnClickListener {
            val mIntent = Intent(Settings.ACTION_LOCALE_SETTINGS)
            startActivity(mIntent)
        }
        btnKeluar.setOnClickListener {

        }

        btnJadwal.setOnClickListener {

        }

        btnInterval.setOnClickListener {

        }

        btnZona.setOnClickListener {

        }

        saklar?.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "Pemberitahuan Dinyalakan" else "Pemberitahuan Dimatikan"
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }
}