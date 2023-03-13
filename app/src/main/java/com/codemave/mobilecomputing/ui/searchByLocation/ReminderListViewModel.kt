package com.codemave.mobilecomputing.ui.searchByLocation

import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codemave.mobilecomputing.Graph
import com.codemave.mobilecomputing.R
import com.codemave.mobilecomputing.data.entity.Reminder
import com.codemave.mobilecomputing.data.repository.ReminderRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReminderListViewModel (
    private val reminderRepository: ReminderRepository = Graph.reminderRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ReminderListViewState())

    val state: StateFlow<ReminderListViewState>
        get() = _state

    init {
        viewModelScope.launch {
            reminderRepository.reminders().collect { list ->
                _state.value = ReminderListViewState(
                    reminders = list
                )
            }
        }
    }
}

data class ReminderListViewState(
    val reminders: List<Reminder> = emptyList()
)