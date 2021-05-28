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
                CamService.ACTION_RESULT -> handleResult(p1)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val filter = IntentFilter()
        filter.addAction(ACTION_MONITOR_ON)
        filter.addAction(ACTION_MONITOR_OFF)
        filter.addAction(CamService.ACTION_RESULT)
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

    private fun updateSafeZoneStatus() {
        val isInSafeZone =
            activity?.getPreferences(Context.MODE_PRIVATE)?.getBoolean("isInSafeZone", false)
        if (isInSafeZone == true) {
            val safeZoneName =
                activity?.getPreferences(Context.MODE_PRIVATE)?.getString("safeZoneName", "")
            val safeZoneDistance =
                activity?.getPreferences(Context.MODE_PRIVATE)?.getInt("safeZoneDistance", 0)

            // Ini ubah mega, ubah UI kalau dia berada di SafeZone
            Toast.makeText(activity?.applicationContext, "You are " + safeZoneDistance + " meters from safe-zone (" + safeZoneName + ")", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun handleResult(intent: Intent) {
        val cls = intent.getIntExtra("class", -1)
        val accuracy = intent.getDoubleExtra("accuracy", 0.0)
        var status = ""


        // Ini ubah mega, ubah UI sesuai hasil

        if (cls === ImageClassification.UNSURE) {
            status = "Unsure"
            Toast.makeText(activity?.applicationContext, "INCONSISTENT RESULT", Toast.LENGTH_LONG).show()
        }
        if (cls === ImageClassification.NOT_FOUND) {
            status = "No Face"
            Toast.makeText(activity?.applicationContext, "NO FACE FOUND", Toast.LENGTH_LONG).show()
        }
        if (cls === ImageClassification.WITH_MASK) {
            status = "Masked"
            Toast.makeText(activity?.applicationContext, "MASK USED (" + (accuracy * 100).roundToInt().toString() + "%" + ")", Toast.LENGTH_LONG).show()
        }
        if (cls === ImageClassification.WITHOUT_MASK) {
            status = "Unmasked"
            Toast.makeText(activity?.applicationContext, "MASK UNUSED (" + (accuracy * 100).roundToInt().toString() + "%" + ")", Toast.LENGTH_LONG).show()
        }

        tv_laststatus.setText(status + " at " + LocalTime.now().truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_LOCAL_TIME))
    }

    companion object {
        val ACTION_MONITOR_ON = "pakaimasker.action.MONITOR_ON"
        val ACTION_MONITOR_OFF = "pakaimasker.action.MONITOR_OFF"
    }
}