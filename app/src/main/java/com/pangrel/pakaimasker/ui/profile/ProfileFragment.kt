package com.pangrel.pakaimasker.ui.profile

import android.Manifest
import android.R.attr.button
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.RippleDrawable
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.slider.Slider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.pangrel.pakaimasker.*
import kotlinx.android.synthetic.main.fragment_profile.*
import java.text.SimpleDateFormat
import java.util.*


class ProfileFragment : Fragment() {

    private lateinit var mAuth: FirebaseAuth

    override fun onResume() {
        super.onResume()

        val uid = FirebaseAuth.getInstance().uid

        if (uid != null) {
            FirebaseDatabase.getInstance().getReference("/users/" + uid + "/code").get().addOnSuccessListener {
                tv_devicecode.text = it.value.toString()
            }.addOnFailureListener{
                tv_devicecode.text = "-"
            }
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    @SuppressLint("SimpleDateFormat", "InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser
        tv_profilename.text = currentUser?.displayName
        tv_profileemail.text = currentUser?.email
        Glide.with(this)
            .load(currentUser?.photoUrl)
            .centerCrop()
            .into(img_profile)

        btnLanguage.setOnClickListener {
            val mIntent = Intent(Settings.ACTION_LOCALE_SETTINGS)
            startActivity(mIntent)
        }

        btnKeluar.setOnClickListener {
            val alert: AlertDialog.Builder = AlertDialog.Builder(context)
            alert.setTitle("Apakah anda mau keluar?")
            alert.setPositiveButton("Ya") { dialog, whichButton ->
                Firebase.auth.signOut()
                (activity as HomeActivity).setLogin(false)
                (activity as HomeActivity).toLogin()
            }
            alert.setNegativeButton("Tidak", null)
            alert.show()
        }

        lateinit var timeFrom: String
        lateinit var timeUntil: String
        btnJadwal.setOnClickListener {
            val dialog = AlertDialog.Builder(context).create()
            val dialogView = layoutInflater.inflate(R.layout.dialog_jadwal, null)
            dialog.setView(dialogView)
            dialog.setCancelable(true)
            val btnScheduleStart = dialogView.findViewById<Button>(R.id.btn_time_start)
            val btnScheduleEnd =  dialogView.findViewById<Button>(R.id.btn_time_end)
            val btnSetSchedule = dialogView.findViewById<Button>(R.id.btn_set_jadwal)
            val btnUnsetSchedule = dialogView.findViewById<Button>(R.id.btn_unset)

            val calendarFrom: Calendar = Calendar.getInstance()
            val calendarUntil: Calendar = Calendar.getInstance()
            
            val timeFromListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                calendarFrom.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendarFrom.set(Calendar.MINUTE, minute)
                val simpleDateFormat = SimpleDateFormat("HH:mm")
                timeFrom = simpleDateFormat.format(calendarFrom.time)
                btnScheduleStart.text = timeFrom
                btnScheduleStart.setBackgroundColor(Color.WHITE)
                btnScheduleStart.setTextColor(Color.BLACK)
            }
            val pickerFrom = TimePickerDialog(
                context,
                timeFromListener,
                calendarFrom.get(Calendar.HOUR_OF_DAY),
                calendarFrom.get(Calendar.MINUTE),
                true
            )
            pickerFrom.setTitle("Start Monitoring at")

            val timeUntilListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                calendarUntil.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendarUntil.set(Calendar.MINUTE, minute)
                val simpleDateFormat = SimpleDateFormat("HH:mm")
                timeUntil = simpleDateFormat.format(calendarUntil.time)
                btnScheduleEnd.text = timeUntil
                btnScheduleEnd.setBackgroundColor(Color.WHITE)
                btnScheduleEnd.setTextColor(Color.BLACK)
            }
            val pickerUntil = TimePickerDialog(
                context,
                timeUntilListener,
                calendarUntil.get(Calendar.HOUR_OF_DAY),
                calendarUntil.get(Calendar.MINUTE),
                true
            )
            pickerUntil.setTitle("End Monitoring at")

            btnScheduleStart.setOnClickListener {
                pickerFrom.show()
            }
            btnScheduleEnd.setOnClickListener {
                pickerUntil.show()
            }

            btnSetSchedule.setOnClickListener {
                Toast.makeText(context, "set monitor time $timeFrom until $timeUntil", Toast.LENGTH_SHORT).show()
                (activity as HomeActivity).updateMonitoringSchedule(timeFrom, timeUntil)
                dialog.dismiss()
            }

            btnUnsetSchedule.setOnClickListener {
                timeFrom = ""
                timeUntil = ""

                btnScheduleStart.text = getString(R.string.dialog_jadwal_time)
                val mode1 = context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
                when (mode1) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        btnScheduleStart.setBackgroundColor(resources.getColor(R.color.purple_200))
                    }
                    Configuration.UI_MODE_NIGHT_NO -> {
                        btnScheduleStart.setBackgroundColor(resources.getColor(R.color.teal_200))
                    }
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
                }
                btnScheduleStart.setTextColor(Color.WHITE)

                btnScheduleEnd.text = getString(R.string.dialog_jadwal_time)
                val mode2 = context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
                when (mode2) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        btnScheduleEnd.setBackgroundColor(resources.getColor(R.color.purple_200))
                    }
                    Configuration.UI_MODE_NIGHT_NO -> {
                        btnScheduleEnd.setBackgroundColor(resources.getColor(R.color.teal_200))
                    }
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
                }
                btnScheduleEnd.setTextColor(Color.WHITE)
                (activity as HomeActivity).updateMonitoringSchedule(timeFrom, timeUntil)
            }

            val timeData = (activity as HomeActivity).getMonitoringSchedule()
            if (timeData[0] != ""){
                timeFrom = timeData[0]!!
                btnScheduleStart.text = timeFrom
                btnScheduleStart.setBackgroundColor(Color.WHITE)
                btnScheduleStart.setTextColor(Color.BLACK)
            }
            if (timeData[1] != ""){
                timeUntil = timeData[1]!!
                btnScheduleEnd.text = timeUntil
                btnScheduleEnd.setBackgroundColor(Color.WHITE)
                btnScheduleEnd.setTextColor(Color.BLACK)
            }

            dialog.show()
        }

        btnInterval.setOnClickListener {
            val dialog = AlertDialog.Builder(context)
            val dialogView = layoutInflater.inflate(R.layout.dialog_interval, null)
            dialog.setView(dialogView)
            dialog.setCancelable(true)
            dialog.setTitle("Set Interval")

            val sliderInterval = dialogView.findViewById<Slider>(R.id.slider_interval)
            val tvInterval = dialogView.findViewById<TextView>(R.id.tv_interval)
            val valueInterval = (activity as HomeActivity).getInterval()
            sliderInterval.value = valueInterval
            when (sliderInterval.value) {
                5.0f -> tvInterval.text = getString(R.string.interval1)
                10.0f -> tvInterval.text = getString(R.string.interval2)
                15.0f -> tvInterval.text = getString(R.string.interval3)
            }
            sliderInterval.addOnChangeListener { slider, value, fromUser ->
                when (slider.value) {
                    5.0f -> tvInterval.text = getString(R.string.interval1)
                    10.0f -> tvInterval.text = getString(R.string.interval2)
                    15.0f -> tvInterval.text = getString(R.string.interval3)
                }
            }

            dialog.setPositiveButton(
                "SET"
            ) { dialog, which ->
                // set interval
                (activity as HomeActivity).setInterval(sliderInterval.value)
                dialog.dismiss()
            }

            dialog.setNegativeButton(
                "CANCEL"
            ) { dialog, which -> dialog.dismiss() }

            dialog.show()
        }

        btnZona.setOnClickListener {
            if (activity != null) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    HomeActivity.CODE_PERM_LOCATION
                )
            }

            val dialog = AlertDialog.Builder(context).create()
            val dialogView = layoutInflater.inflate(R.layout.dialog_safe_zone, null)
            dialog.setCancelable(true)
            dialog.setView(dialogView)

            val lvSafeZone = dialogView.findViewById<ListView>(R.id.lv_safe_zone)
            val locations = (activity as HomeActivity).getLocation()
            val adapter = SafeZoneAdapter((activity as HomeActivity))
            adapter.locations = locations as ArrayList<Zone>
            lvSafeZone.adapter = adapter
            val btnAddLocation = dialogView.findViewById<Button>(R.id.btn_add_location)

            btnAddLocation.setOnClickListener {
                val dialog2 = AlertDialog.Builder(context).create()
                val dialogView2 = layoutInflater.inflate(R.layout.dialog_add_safe_zone, null)
                dialog2.setCancelable(true)
                dialog2.setView(dialogView2)

                val edLocation = dialogView2.findViewById<EditText>(R.id.ed_location)
                val btnAddSafeZone = dialogView2.findViewById<Button>(R.id.btn_add_safe_zone)
                btnAddSafeZone.setOnClickListener {
                    addLocation(adapter, btnAddSafeZone, dialog2, edLocation.text.toString())
                }
                val ivExit2 = dialogView2.findViewById<ImageView>(R.id.iv_exit2)
                ivExit2.setOnClickListener {
                    dialog2.dismiss()
                }

                dialog2.show()
            }


            val ivExit = dialogView.findViewById<ImageView>(R.id.iv_exit)
            ivExit.setOnClickListener {
                dialog.dismiss()
                (activity as HomeActivity).updateLocation(adapter.locations)
            }

            dialog.show()
        }


        saklar.isChecked = (activity as HomeActivity).getNotificationStatus()
        saklar.setOnCheckedChangeListener { _, isChecked ->
            (activity as HomeActivity).setNotificationStatus(isChecked)
            val message = if (isChecked) getString(R.string.notif_on) else getString(R.string.notif_off)
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun addLocation(adapter: SafeZoneAdapter, button: Button, dialog: AlertDialog, name: String) {
        // Now create a location manager
        val locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (activity?.applicationContext == null) return

        if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            Toast.makeText(activity?.applicationContext, "GPS must be active!", Toast.LENGTH_LONG).show()
            return
        }

        if (ActivityCompat.checkSelfPermission(
                activity?.applicationContext!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activity?.applicationContext!!,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(activity?.applicationContext, "GPS must be active!", Toast.LENGTH_LONG).show()
            return
        }

        button.isEnabled = false

        val locationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(currentLocation: Location) {
                Log.d("Location Changes", currentLocation.toString())

//                val intent = Intent(CamService.ACTION_ADD_SAFEZONE)
//
//                intent.putExtra("name", name)
//                intent.putExtra("location", currentLocation)
//
//                activity?.sendBroadcast(intent)

                adapter.locations.add(Zone(name, currentLocation.latitude, currentLocation.longitude))
                //(lvSafeZone.adapter as SafeZoneAdapter).locations = locations
                adapter.notifyDataSetChanged()

                dialog.dismiss()
            }

            override fun onStatusChanged(
                provider: String?,
                status: Int,
                extras: Bundle?
            ) {}

            override fun onProviderEnabled(provider: String) {}

            override fun onProviderDisabled(provider: String) {}
        }

        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE
        criteria.powerRequirement = Criteria.POWER_HIGH
        criteria.isAltitudeRequired = false
        criteria.isBearingRequired = false
        criteria.isSpeedRequired = false
        criteria.isCostAllowed = true
        criteria.horizontalAccuracy = Criteria.ACCURACY_HIGH
        criteria.verticalAccuracy = Criteria.ACCURACY_HIGH


        locationManager.requestSingleUpdate(criteria, locationListener, null)
    }

    private fun isTimeValid(){

    }
}