package com.pangrel.pakaimasker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import java.time.LocalDateTime

interface OnLocationListener {
    fun onUpdated(safeZones: ArrayList<Zone>, isSafe: Boolean)
}

class LocationTracker {
    private val context: Context
    private var safeZones = ArrayList<Zone>()
    private var lastLocation: Location? = null
    private var lastUpdated: LocalDateTime? = null
    private var listener: OnLocationListener? = null
    private var locationClient: FusedLocationProviderClient
    private var isStarted = false
    private var isSafe = false
    private var safePlace: Zone? = null
    private var updateInterval: Long
    private var smallDisplacementDistance: Float
    private var safezoneRadius: Int

    constructor(context: Context, interval: Long, radius: Int, small: Float) {
        this.context = context
        this.updateInterval = interval
        this.safezoneRadius = radius
        this.smallDisplacementDistance = small
        this.locationClient = LocationServices.getFusedLocationProviderClient(this.context)
    }

    fun isSafe(): Boolean {
        return this.isSafe
    }

    fun getSafePlace(): Zone? {
        return this.safePlace
    }

    fun getZones(): ArrayList<Zone> {
        return this.safeZones
    }

    fun setListener(listener: OnLocationListener) {
        this.listener = listener
    }

    private fun calculateZones() {
        var isSafeNow = false
        if (lastLocation !== null) {
            for (zone in safeZones!!) {
                if (zone.calculateDistance(lastLocation!!) <= safezoneRadius) {
                    zone.isSafe = true
                    isSafeNow = true
                    safePlace = zone
                }



                Log.d("LocationTracker", zone.name + " (" + zone.distance.toString() + ")")
            }
        }

        isSafe = isSafeNow

        if (isSafe === false) {
            safePlace = null
        }
    }

    private fun updateLocation(location: Location) {
        lastUpdated = LocalDateTime.now()
        lastLocation = location

        calculateZones()

        listener?.onUpdated(this.safeZones, this.isSafe)

        Log.d(this.javaClass.name, "Location Updated: " + location.getLatitude().toString() + " | " + location.getLongitude().toString())

    }

    fun updateSafeZones(zones: ArrayList<Zone>) {
        safeZones = zones

        calculateZones()

        if (safeZones.size === 0) {
            this.stopMonitoring()
        } else {
            this.startMonitor()
        }
    }

    fun clearSafeZone() {
        safeZones.clear()

        this.stopMonitoring()
    }

    private val onLocationChanged = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation
            updateLocation(location)
        }
    }

    fun stopMonitoring() {
        if (!isStarted) return

        isSafe = false
        isStarted = false
        lastLocation = null
        lastUpdated = null
        locationClient.removeLocationUpdates(onLocationChanged)

        Log.d(this.javaClass.name, "Location is stopped")
    }

    fun startMonitor() {
        if (isStarted) return

        if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(this.javaClass.name, "Location permission is not granted!")
            return
        }

        isStarted = true

        // Create the location request to start receiving updates
        val locationRequest = LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(updateInterval)
//            .setFastestInterval(updateInterval/2)
//            .setSmallestDisplacement(smallDisplacementDistance)

        // Create LocationSettingsRequest object using location request
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest!!)
        val locationSettingsRequest = builder.build()

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        val settingsClient = LocationServices.getSettingsClient(this.context)
        settingsClient.checkLocationSettings(locationSettingsRequest)

        locationClient.requestLocationUpdates(locationRequest, onLocationChanged, Looper.myLooper())

        Log.d(this.javaClass.name, "Location is started")
    }
}
