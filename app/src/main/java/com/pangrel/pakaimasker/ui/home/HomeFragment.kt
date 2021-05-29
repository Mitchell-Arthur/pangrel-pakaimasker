package com.pangrel.pakaimasker.ui.home

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.pangrel.pakaimasker.*
import kotlinx.android.synthetic.main.fragment_home.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class HomeFragment : Fragment() {

    private lateinit var mAuth: FirebaseAuth
    private val receiver = object: BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            Log.d("HomeFragment", "receive " + p1?.action)
            when (p1?.action) {
                ACTION_MONITOR_ON -> startMonitoring()
                ACTION_MONITOR_OFF -> stopMonitoring()
                ACTION_UPDATE_RESULT -> updateResult()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val filter = IntentFilter()
        filter.addAction(ACTION_MONITOR_ON)
        filter.addAction(ACTION_MONITOR_OFF)
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
        val fullName = currentUser?.displayName
        val firstName = fullName?.split(" ")?.first()
        tv_name.text = firstName

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

        if (activity != null) {
            val isRunning = isServiceRunning(activity.applicationContext, CamService::class.java)
            updateButtonText(isRunning)


            if (isRunning) {
                updateResult()
            }
        }


    }

    fun stopMonitoring() {
        updateButtonText(false)
        img_status.setImageResource(R.drawable.bingung_icon)
        lastStatusLabel.text = getString(R.string.monitoroff)
        tv_laststatus.text = ""

        // Mega disini perlu di reset view UInya (teks) ubah kembali ke saat belum start monitor //DONE
        tv_status.setText(getString(R.string.caption))
    }

    fun startMonitoring() {
        updateButtonText(true)

        lastStatusLabel.text = getString(R.string.starting)
    }

    @SuppressLint("UseCompatLoadingForColorStateLists")
    private fun updateButtonText(running: Boolean) {
        if (running) {
            btnMonitoring.text = getString(R.string.OFF)
            btnMonitoring.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.off)))
        } else {
            btnMonitoring.text = getString(R.string.ON)
            btnMonitoring.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.primary)))
        }

        btnMonitoring.isEnabled = true
    }

    private fun updateResult() {
        val lastStatus =
            activity?.getPreferences(Context.MODE_PRIVATE)?.getString("lastStatus", "")
        val lastUpdate =
            activity?.getPreferences(Context.MODE_PRIVATE)?.getString("lastUpdate", "")

        var dateTime = LocalDateTime.now()

        if (lastUpdate !== "") {
            dateTime = LocalDateTime.parse(lastUpdate)
        }

        if (lastStatus == "classification") {
            val classificationResult =
                activity?.getPreferences(Context.MODE_PRIVATE)?.getInt("classificationResult", ImageClassification.UNDEFINED)

            tv_status.setText("")

            var status = ""
            if (classificationResult === ImageClassification.UNSURE) {
                status = getString(R.string.Unsure)
            }
            if (classificationResult === ImageClassification.NOT_FOUND) {
                status = getString(R.string.No_Face)
            }
            if (classificationResult === ImageClassification.WITH_MASK) {
                status = getString(R.string.Masked)
                img_status.setImageResource(R.drawable.masked_icon)
                // UBAH INI MEGA //DONE
                tv_status.setText(getString(R.string.statusYes))
            }
            if (classificationResult === ImageClassification.WITHOUT_MASK) {
                status = getString(R.string.Unmasked)
                img_status.setImageResource(R.drawable.unmasked_icon)
                // UBAH INI MEGA //DONE
                tv_status.setText(getString(R.string.statusNo))
            }

            lastStatusLabel.setText(getString(R.string.Last_Status))
            tv_laststatus.setText(status + " " + getString(R.string.at) + " "+ dateTime.truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_LOCAL_TIME))
        }

        if (lastStatus == "in_safezone") {
            val zoneName = activity?.getPreferences(Context.MODE_PRIVATE)?.getString("zoneName", "")
            val zoneDistance = activity?.getPreferences(Context.MODE_PRIVATE)?.getInt("zoneDistance", 0)

            img_status.setImageResource(R.drawable.safezone_icon)
            tv_status.setText(getString(R.string.safe) + " " + zoneDistance + " " + getString(R.string.safezo) + zoneName + ")")

            lastStatusLabel.setText(getString(R.string.insafezone))
            tv_laststatus.setText("")
        }

        if (lastStatus == "out_of_schedule") {
            img_status.setImageResource(R.drawable.safezone_icon)
            // UBAH INI MEGA //UDA GINI AJA BAGUS ELAH UBAH APALAGI
            tv_status.setText(getString(R.string.statusOut))

            lastStatusLabel.setText(getString(R.string.OutSchedule))
            tv_laststatus.setText("")
        }
    }

    companion object {
        val ACTION_MONITOR_ON = "homefragment.action.MONITOR_ON"
        val ACTION_MONITOR_OFF = "homefragment.action.MONITOR_OFF"
        val ACTION_UPDATE_RESULT = "homefragment.action.UPDATE_RESULT"
        val ACTION_UPDATE_SAFEZONE = "homefragment.action.UPDATE_SAFEZONE"
    }
}