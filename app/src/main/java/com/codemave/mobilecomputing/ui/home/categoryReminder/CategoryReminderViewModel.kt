package com.codemave.mobilecomputing.ui.home.categoryReminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codemave.mobilecomputing.Graph
import com.codemave.mobilecomputing.data.repository.ReminderRepository
import com.codemave.mobilecomputing.data.room.ReminderToCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CategoryReminderViewModel (
    private val categoryId: Long,
    private val reminderRepository: ReminderRepository = Graph.reminderRepository
): ViewModel() {
    private val _state = MutableStateFlow(CategoryReminderViewState())

    val state: StateFlow<CategoryReminderViewState>
        get() = _state

    init {
        viewModelScope.launch {
            reminderRepository.remindersInCategory(categoryId).collect { list ->
                _state.value = CategoryReminderViewState(
                    reminders = list
                )
            }
        }
    }
}

data class CategoryReminderViewState(
    val reminders: List<ReminderToCategory> = emptyList()
)