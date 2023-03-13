package com.codemave.mobilecomputing.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.codemave.mobilecomputing.Graph
import com.codemave.mobilecomputing.data.repository.ReminderRepository

class PhoneBossAppState (val navController: NavHostController) {
    fun navigateBack() {
        navController.popBackStack()
    }
}

@Composable
fun rememberPhoneBossAppState(
    navController: NavHostController = rememberNavController()
) = remember(navController) {
    PhoneBossAppState(navController)
}

