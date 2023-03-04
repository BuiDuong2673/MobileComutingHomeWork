package com.codemave.mobilecomputing.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

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