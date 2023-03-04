package com.codemave.mobilecomputing.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import com.codemave.mobilecomputing.ui.welcome.Welcome
import androidx.navigation.compose.composable

import com.codemave.mobilecomputing.ui.edit.EditReminder
import com.codemave.mobilecomputing.ui.home.Home
import com.codemave.mobilecomputing.ui.login.LogIn
import com.codemave.mobilecomputing.ui.profile.Profile
import com.codemave.mobilecomputing.ui.reminder.Reminder
import com.codemave.mobilecomputing.ui.signup.SignUp

@Composable
fun PhoneBossApp (
    appState: PhoneBossAppState = rememberPhoneBossAppState()
) {
    NavHost(
        navController = appState.navController,
        startDestination = "welcome"
    ) {
        composable(route = "welcome") {
            Welcome(navController = appState.navController)
        }
        composable(route = "login") {
            LogIn(navController = appState.navController)
        }

        composable(route = "signup") {
            SignUp(navController = appState.navController)
        }
        composable(route = "home") {
            Home(navController = appState.navController)
        }

        composable(route = "reminder") {
            Reminder(onBackPress = appState::navigateBack)
        }

        composable(route = "edit") {
            EditReminder(navController = appState.navController, onBackPress = appState::navigateBack)
        }

        composable(route = "profile") {
            Profile(navController = appState.navController, onBackPress = appState::navigateBack)
        }
    }
}