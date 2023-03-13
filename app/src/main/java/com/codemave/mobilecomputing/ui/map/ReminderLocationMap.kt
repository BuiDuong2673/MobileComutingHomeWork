package com.codemave.mobilecomputing.ui.map

import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.codemave.mobilecomputing.Graph
import com.codemave.mobilecomputing.data.entity.Reminder
import com.codemave.mobilecomputing.ui.searchByLocation.ReminderListViewModel
import com.codemave.mobilecomputing.ui.searchByLocation.isLocationNear
import com.codemave.mobilecomputing.util.rememberMapViewWithLifecycle
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.ktx.awaitMap
import kotlinx.coroutines.launch
import java.util.*
import com.codemave.mobilecomputing.ui.MainActivity
import com.codemave.mobilecomputing.ui.reminder.reminderIsNear
import com.codemave.mobilecomputing.ui.reminder.reminderIsNearNotification

@Composable
fun ReminderLocationMap(
    navController: NavController,
) {
    val mapView = rememberMapViewWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    val viewModel: ReminderListViewModel = viewModel()
    val viewState by viewModel.state.collectAsState()

    Column (modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
        .padding(bottom = 36.dp)) {
        AndroidView({mapView}) {
            coroutineScope.launch {
                // set up the initial marked location
                val map = mapView.awaitMap()
                map.uiSettings.isZoomControlsEnabled = true
                map.uiSettings.isScrollGesturesEnabled = true
                val location = LatLng(65.06, 25.47) // latitude and longitude of the University of Oulu TODO make the variable of this coordinate
                //val location = LatLng(latitude, longitude)
                map.moveCamera(
                    //CameraUpdateFactory.newLatLngZoom(location, 10f)
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng (location.latitude, location.longitude),
                        10f
                    )
                )

                val markerOptions = MarkerOptions()
                    .title ("Welcome to the University of Oulu!")
                    .position(location)
                map.addMarker(markerOptions)

                setMapLongClick(map = map, navController = navController, reminders = viewState.reminders)
            }
        }
    }
}
/*
private fun nearReminderList(
    list: List<Reminder>,
    map: GoogleMap
) {
    var userLocation: Location = Location("")
    var fusedLocationClient = LocationServices.getFusedLocationProviderClient(Graph.appContext)
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location: Location? ->
            if (location != null) {
                 userLocation = location
            }
        }
    list.forEach { reminder ->
        val reminderLocation = LatLng(reminder.locationX, reminder.locationY)
        if (isLocationNear(reminderLocation, LatLng(userLocation.latitude, userLocation.longitude))) {
            reminderIsNearNotification(reminder)
        }
    }
}
*/
/**
 * When we long click, the latitude and the longitude will be shown
 */
private fun setMapLongClick(
    map:GoogleMap,
    navController: NavController,
    reminders: List<Reminder>
) {
    map.setOnMapLongClickListener { latlng ->
        val snippet = String.format(
            Locale.getDefault(),
            "Lat: %1$.2f, Lng: %2$.2f",
            latlng.latitude,
            latlng.longitude
        )

        map.addMarker(
            MarkerOptions().position(latlng).title("Reminder Location").snippet(snippet)
        ).apply {
            navController
                .previousBackStackEntry
                ?.savedStateHandle
                ?.set("location_data", latlng)
            navController.popBackStack()
        }

        //nearReminderList(reminders, map)
    }
}