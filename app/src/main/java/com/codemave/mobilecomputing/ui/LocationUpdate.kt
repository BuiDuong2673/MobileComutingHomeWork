package com.codemave.mobilecomputing.ui

import android.Manifest
import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codemave.mobilecomputing.Graph
import com.codemave.mobilecomputing.data.entity.Account
import com.codemave.mobilecomputing.data.entity.Reminder
import com.codemave.mobilecomputing.data.repository.ReminderRepository
import com.codemave.mobilecomputing.ui.login.getProfile
import com.codemave.mobilecomputing.ui.login.setProfile
import com.codemave.mobilecomputing.ui.reminder.requestPermission
import com.codemave.mobilecomputing.ui.searchByLocation.ReminderListViewModel
import com.codemave.mobilecomputing.ui.searchByLocation.isLocationNear
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng

class LocationUpdate: Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationRequest: LocationRequest = LocationRequest.create().apply {
        interval = 10000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.let { result ->
                for (location in result.locations) {
                    setProfile(
                        Account (
                            id = getProfile().id,
                            name = getProfile().name,
                            password = getProfile().password,
                            locationX = location.latitude,
                            locationY = location.longitude
                        )
                    )

                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startLocationUpdates()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }
}

