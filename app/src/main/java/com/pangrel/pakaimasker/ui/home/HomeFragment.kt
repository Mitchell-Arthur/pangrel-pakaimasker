package com.pangrel.pakaimasker.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.pangrel.pakaimasker.*
import kotlinx.android.synthetic.main.fragment_home.*
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

class HomeFragment : Fragment() {

    private lateinit var mAuth: FirebaseAuth
    private val receiver = object: BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            Log.d("HomeFragment", "receive " + p1?.action)
            when (p1?.action) {
                ACTION_MONITOR_ON -> updateButtonText(true)
                ACTION_MONITOR_OFF -> updateButtonText(false)
                ACTION_UPDATE_SAFEZONE -> updateSafeZoneStatus()
                ACTION_UPDATE_RESULT -> updateClassificationResult()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val filter = IntentFilter()
        filter.addAction(ACTION_MONITOR_ON)
        filter.addAction(ACTION_MONITOR_OFF)
        filter.addAction(ACTION_UPDATE_SAFEZONE)
        filter.addAction(ACTION_UPDATE_RESULT)
        activity?.registerReceiver(receiver, filter)
    }

    override fun onPause() {
        super.onPause()

        activity?.unregisterReceiver(receiver)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("HomeFragment", "onCreateView")
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("HomeFragment", "onViewCreated")
        super.onViewCreated(view, savedInstanceState)

        val activity = getActivity()

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser
        tv_name.text = currentUser?.displayName

        if (activity != null) {
            updateButtonText(isServiceRunning(activity.applicationContext, CamService::class.java))
        }

        btnMonitoring.setOnClickListener{
            btnMonitoring.isEnabled = false

            if (activity !== null) {
                if (!isServiceRunning(activity.applicationContext, CamService::class.java)) {
                    val intent = Intent(activity.applicationContext, CamService::class.java)
                    activity.startService(intent)
                } else {
                    activity.stopService(Intent(activity.applicationContext, CamService::class.java))
                }
            }
        }

        updateClassificationResult()
        updateSafeZoneStatus()
    }

    fun updateButtonText(running: Boolean) {
        if (running) {
            btnMonitoring.setText("STOP MONITORING")

        } else {
            btnMonitoring.setText("START MONITORING")
        }

        btnMonitoring.isEnabled = true
    }

    private fun updateClassificationResult() {
        val classificationResult =
            activity?.getPreferences(Context.MODE_PRIVATE)?.getInt("classificationResult", ImageClassification.UNDEFINED)
        val classificationUpdate =
            activity?.getPreferences(Context.MODE_PRIVATE)?.getString("classificationUpdate", "")

        var dateTime = LocalDateTime.now()

        if (classificationUpdate !== "") {
            dateTime = LocalDateTime.parse(classificationUpdate)
        }
        Log.d("HomeFragment", dateTime.toString())


        // Ini ubah mega, ubah UI sesuai hasil

        var status = ""
        if (classificationResult === com.pangrel.pakaimasker.ImageClassification.UNSURE) {
            status = "Unsure"
        }
        if (classificationResult === com.pangrel.pakaimasker.ImageClassification.NOT_FOUND) {
            status = "No Face"
        }
        if (classificationResult === com.pangrel.pakaimasker.ImageClassification.WITH_MASK) {
            status = "Masked"
            Toast.makeText(activity?.applicationContext, "MASK USED", Toast.LENGTH_LONG).show()
        }
        if (classificationResult === com.pangrel.pakaimasker.ImageClassification.WITHOUT_MASK) {
            status = "Unmasked"
            Toast.makeText(activity?.applicationContext, "MASK UNUSED", Toast.LENGTH_LONG).show()
        }

        tv_laststatus.setText(status + " at " + dateTime.truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_LOCAL_TIME))
    }

    private fun updateSafeZoneStatus() {
        val isInSafeZone =
            activity?.getPreferences(Context.MODE_PRIVATE)?.getBoolean("isInSafeZone", false)
        if (isInSafeZone == true) {
            val safeZoneName =
                activity?.getPreferences(Context.MODE_PRIVATE)?.getString("safeZoneName", "")
            val safeZoneDistance =
                activity?.getPreferences(Context.MODE_PRIVATE)?.getInt("safeZoneDistance", 0)

            // Ini ubah mega, ubah UI kalau dia berada di SafeZone
            tv_status.setText("You are " + safeZoneDistance + " meters from safe-zone (" + safeZoneName + ")")
        }
    }

    companion object {
        val ACTION_MONITOR_ON = "homefragment.action.MONITOR_ON"
        val ACTION_MONITOR_OFF = "homefragment.action.MONITOR_OFF"
        val ACTION_UPDATE_RESULT = "homefragment.action.UPDATE_RESULT"
        val ACTION_UPDATE_SAFEZONE = "homefragment.action.UPDATE_SAFEZONE"
    }
}