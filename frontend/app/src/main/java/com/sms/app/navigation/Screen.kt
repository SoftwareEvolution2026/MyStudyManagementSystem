package com.sms.app.navigation

sealed class Screen(val route: String) {
    object Login      : Screen("login")
    object Register   : Screen("register")
    object Dashboard  : Screen("dashboard")
    object Tasks      : Screen("tasks")
    object Calendar   : Screen("calendar")
    object Sessions   : Screen("sessions")
    object Pomodoro   : Screen("pomodoro")
    object Analytics  : Screen("analytics")
}