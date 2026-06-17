package com.sms.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sms.app.userinterface.analytics.AnalyticsScreen
import com.sms.app.userinterface.auth.LoginScreen
import com.sms.app.userinterface.auth.RegisterScreen
import com.sms.app.userinterface.calendar.CalendarScreen
import com.sms.app.userinterface.dashboard.DashboardScreen
import com.sms.app.userinterface.pomodoro.PomodoroScreen
import com.sms.app.userinterface.sessions.SessionsScreen
import com.sms.app.userinterface.tasks.TasksScreen

@Composable
fun NavGraph(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route)     { LoginScreen(navController) }
        composable(Screen.Register.route)  { RegisterScreen(navController) }
        composable(Screen.Dashboard.route) { DashboardScreen(navController) }
        composable(Screen.Tasks.route)     { TasksScreen(navController) }
        composable(Screen.Calendar.route)  { CalendarScreen(navController) }
        composable(Screen.Sessions.route)  { SessionsScreen(navController) }
        composable(Screen.Pomodoro.route)  { PomodoroScreen(navController) }
        composable(Screen.Analytics.route) { AnalyticsScreen(navController) }
    }
}