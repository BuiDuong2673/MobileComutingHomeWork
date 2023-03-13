package com.codemave.mobilecomputing.ui.searchByLocation

import android.location.Location
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.codemave.mobilecomputing.Graph
import com.codemave.mobilecomputing.data.entity.Reminder
import com.codemave.mobilecomputing.util.rememberMapViewWithLifecycle
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.ktx.awaitMap
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun SearchByLocation(navController: NavController) {
    val mapView: MapView = rememberMapViewWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    val viewModel: ReminderListViewModel = viewModel()
    val viewState by viewModel.state.collectAsState()

    AndroidView({mapView}) {
        coroutineScope.launch {
            val map = mapView.awaitMap()
            map.uiSettings.isZoomControlsEnabled = true
            map.uiSettings.isScrollGesturesEnabled = true
            val location = LatLng(65.06, 25.47)
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(location.latitude, location.longitude),
                    10f
                )
            )
            setMapLongClick(map, navController, reminders = viewState.reminders)
        }
    }
}

private fun nearReminderList(
    list: List<Reminder>,
    choosePoint: LatLng,
    map: GoogleMap
) {
    list.forEach { reminder ->
        var reminderLocation = LatLng(reminder.locationX, reminder.locationY)
        if (isLocationNear(reminderLocation, choosePoint)) {
            val reminderLocation = LatLng(reminder.locationX, reminder.locationY)
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.2f, Lng: %2$.2f",
                reminder.locationX,
                reminder.locationY
            )
            map.addMarker (
                MarkerOptions().position(reminderLocation).title(reminder.reminderTitle).snippet(snippet)
            )
        }
    }
}

@Composable
private fun NearReminderItem (
    reminder: Reminder,
    map: GoogleMap
) {
    val reminderLocation = LatLng(reminder.locationX, reminder.locationY)
    val snippet = String.format(
        Locale.getDefault(),
        "Lat: %1$.2f, Lng: %2$.2f",
        reminder.locationX,
        reminder.locationY
    )
    map.addMarker (
        MarkerOptions().position(reminderLocation).title(reminder.reminderTitle).snippet(snippet)
    )
}

fun isLocationNear(
    reminderPoint: LatLng,
    choosePoint: LatLng
): Boolean {
    val searchLocation = Location("").apply {
        choosePoint.latitude
        choosePoint.longitude
    }
    val reminderLocation = Location("").apply {
        reminderPoint.latitude
        reminderPoint.longitude
    }
    val distanceToReminder = searchLocation.distanceTo(reminderLocation)
    return distanceToReminder < 1000
}

private fun setMapLongClick (
    map: GoogleMap,
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
            MarkerOptions().position(latlng).title("Search Location").snippet(snippet)
        ).apply {
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("location_search_data", latlng)
        }
        nearReminderList(list = reminders, choosePoint = latlng, map = map)
    }
}