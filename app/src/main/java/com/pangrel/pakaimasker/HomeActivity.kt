package com.pangrel.pakaimasker

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.pangrel.pakaimasker.ui.home.HomeFragment
import java.util.*
import kotlin.math.roundToInt

class HomeActivity : AppCompatActivity() {
    private val receiver = object: BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            Log.d("MAIN", "receive " + p1?.action)
            when (p1?.action) {
                CamService.ACTION_PREPARED -> startMonitoring()
                CamService.ACTION_STOPPED -> stopMonitoring()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val filter = IntentFilter()
        filter.addAction(CamService.ACTION_PREPARED)
        filter.addAction(CamService.ACTION_STOPPED)
        filter.addAction(CamService.ACTION_LOCATION)
        registerReceiver(receiver, filter)
    }

    override fun onPause() {
        super.onPause()

        unregisterReceiver(receiver)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            CODE_PERM_CAMERA -> {
                if (grantResults?.firstOrNull() != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "App requires camera permission to work!", Toast.LENGTH_LONG).show()
                    stopService(Intent(this, CamService::class.java))
                } else {
                    sendBroadcast(Intent(CamService.ACTION_MONITOR))
                }
            }
            CODE_PERM_LOCATION -> {
                if (grantResults?.firstOrNull() != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "App requires location permission to work!", Toast.LENGTH_LONG).show()
                } else {

                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        supportActionBar?.hide()
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_history, R.id.navigation_profile
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        if (isFirstTime(getPreferences(Context.MODE_PRIVATE))){
            introduction()
        } else if (!isLogin(getPreferences(Context.MODE_PRIVATE))){
            toLogin()
        }
    }

    private fun startCameraMonitoring() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // We don't have camera permission yet. Request it from the user.
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
                CODE_PERM_CAMERA
            )
        } else {
            sendBroadcast(Intent(CamService.ACTION_MONITOR))
        }
    }

    private fun stopMonitoring() {
        sendBroadcast(Intent(HomeFragment.ACTION_MONITOR_OFF))
    }

    private fun updateServiceConfig() {
        val intent = Intent(CamService.ACTION_UPDATE_CONFIG)

        intent.putExtra("interval", getInterval().toLong() * 1000)
        intent.putExtra("alert", getNotificationStatus())

        sendBroadcast(intent)
    }

    private fun startMonitoring() {
        this.updateServiceConfig()
        this.startCameraMonitoring()
        sendBroadcast(Intent(HomeFragment.ACTION_MONITOR_ON))
    }

    private fun isFirstTime(preferences: SharedPreferences): Boolean = preferences.getBoolean("isFirstTime", true)
    private fun introduction() {
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putBoolean("isFirstTime", false)
            apply()
        }
        startActivity(Intent(this, LandingActivity::class.java).putExtra("extra_intro_page", 0))
    }
    private fun isLogin(preferences: SharedPreferences): Boolean = preferences.getBoolean("isLogin", false)
    fun toLogin(){
        startActivity(Intent(this, LandingActivity::class.java).putExtra("extra_intro_page", 4))
    }

    fun updateMonitoringSchedule(start: String, end: String){
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putString("monitorStart", start)
            putString("monitorEnd", end)
            apply()
        }
    }

    fun setInterval(interval: Float){
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putFloat("monitorInterval", interval)
            apply()
        }

        this.updateServiceConfig()
    }

    fun getInterval(): Float = this.getPreferences(Context.MODE_PRIVATE).getFloat("monitorInterval", 5.0f)

    fun getLocation(): ArrayList<String>? {
        val data = ArrayList<String>()
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        val locationData = sharedPref.getString("locationData", null)
        println(locationData)
        val temp = locationData?.split("/*;")
        if (temp != null) {
            for (element in temp){
                data.add(element)
            }
            data.removeAt(data.size - 1)
        }
        return data
    }

    fun updateLocation(locations: ArrayList<String>) {
        var location: String?
        if (locations.size > 0){
            location = ""
            for (i in locations){
                location += "$i/*;"
            }
            println(location)
            val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
            with (sharedPref.edit()) {
                putString("locationData", location)
                apply()
            }
        }else{
            val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
            with (sharedPref.edit()) {
                putString("locationData", null)
                apply()
            }
        }
    }

    fun setNotificationStatus(status: Boolean){
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putBoolean("statusMonitoring", status)
            apply()
        }


        this.updateServiceConfig()
    }

    fun getNotificationStatus(): Boolean = this.getPreferences(Context.MODE_PRIVATE).getBoolean("statusMonitoring", true)


    companion object {
        val CODE_PERM_CAMERA = 6112
        val CODE_PERM_LOCATION = 6115
    }

}