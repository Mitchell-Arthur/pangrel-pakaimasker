package com.pangrel.pakaimasker.ui.profile

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.slider.Slider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.pangrel.pakaimasker.*
import kotlinx.android.synthetic.main.fragment_profile.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ProfileFragment : Fragment() {

    private lateinit var mAuth: FirebaseAuth

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

        var timeFrom = ""
        var timeUntil = ""

        btnLanguage.setOnClickListener {
            val mIntent = Intent(Settings.ACTION_LOCALE_SETTINGS)
            startActivity(mIntent)
        }

        btnKeluar.setOnClickListener {
            val alert: AlertDialog.Builder = AlertDialog.Builder(context)
            alert.setTitle("Apakah anda mau keluar?")
            alert.setPositiveButton("Ya") { dialog, whichButton ->
                Firebase.auth.signOut()
                (activity as HomeActivity).toLogin()
            }
            alert.setNegativeButton("Tidak", null)
            alert.show()
        }

        btnJadwal.setOnClickListener {
            val calendarfrom: Calendar = Calendar.getInstance()
            val calendarUntil: Calendar = Calendar.getInstance()

            val timeUntilListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                calendarUntil.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendarUntil.set(Calendar.MINUTE, minute)
                val simpleDateFormat = SimpleDateFormat("HH:mm")
                timeUntil = simpleDateFormat.format(calendarUntil.time)
                if (calendarUntil.time <= calendarfrom.time) {
                    Toast.makeText(
                        context,
                        "waktu monitor error, waktu selesai monitor harus sesudah waktu mulai monitor",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        "set monitor time $timeFrom until $timeUntil",
                        Toast.LENGTH_LONG
                    ).show()
                    (activity as HomeActivity).updateMonitoringSchedule(timeFrom, timeUntil)
                }
            }
            val pickerUntil = TimePickerDialog(
                context,
                timeUntilListener,
                calendarUntil.get(Calendar.HOUR_OF_DAY),
                calendarUntil.get(Calendar.MINUTE),
                true
            )
            pickerUntil.setTitle("Jadwal Selesai Monitor")
            pickerUntil.show()

            val timeFromListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                calendarfrom.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendarfrom.set(Calendar.MINUTE, minute)
                val simpleDateFormat = SimpleDateFormat("HH:mm")
                timeFrom = simpleDateFormat.format(calendarfrom.time)
            }
            val pickerFrom = TimePickerDialog(
                context,
                timeFromListener,
                calendarfrom.get(Calendar.HOUR_OF_DAY),
                calendarfrom.get(Calendar.MINUTE),
                true
            )
            pickerFrom.setTitle("Jadwal Mulai Monitor")
            pickerFrom.show()
        }

        btnInterval.setOnClickListener {
            val dialog = AlertDialog.Builder(context)
            val dialogView = layoutInflater.inflate(R.layout.dialog_interval, null)
            dialog.setView(dialogView)
            dialog.setCancelable(true)
            dialog.setTitle("Atur Interval")

            val sliderInterval = dialogView.findViewById<Slider>(R.id.slider_interval)
            val tvInterval = dialogView.findViewById<TextView>(R.id.tv_interval)
            val valueInterval = (activity as HomeActivity).getInterval()
            sliderInterval.value = valueInterval
            when (sliderInterval.value) {
                5.0f -> tvInterval.text = "Tiap 5 detik"
                10.0f -> tvInterval.text = "Tiap 10 detik"
                15.0f -> tvInterval.text = "Tiap 15 detik"
            }
            sliderInterval.addOnChangeListener { slider, value, fromUser ->
                when (slider.value) {
                    5.0f -> tvInterval.text = "Tiap 5 detik"
                    10.0f -> tvInterval.text = "Tiap 10 detik"
                    15.0f -> tvInterval.text = "Tiap 15 detik"
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
            val message = if (isChecked) "Pemberitahuan Dinyalakan" else "Pemberitahuan Dimatikan"
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
            ) {
            }

            override fun onProviderEnabled(provider: String?) {
            }

            override fun onProviderDisabled(provider: String?) {
            }
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
}