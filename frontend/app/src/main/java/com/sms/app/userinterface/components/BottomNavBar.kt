package com.sms.app.userinterface.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sms.app.navigation.Screen

data class NavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        NavItem(Screen.Dashboard, "Home",      Icons.Outlined.Home),
        NavItem(Screen.Tasks,     "Tasks",     Icons.Outlined.CheckCircle),
        NavItem(Screen.Sessions,  "Sessions",  Icons.Outlined.Schedule),
        NavItem(Screen.Pomodoro,  "Pomodoro",  Icons.Outlined.Timer),
        NavItem(Screen.Analytics, "Analytics", Icons.Outlined.BarChart)
    )

    val currentRoute by navController.currentBackStackEntryAsState()

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute?.destination?.route == item.screen.route,
                onClick = {
                    navController.navigate(item.screen.route) {
                        popUpTo(Screen.Dashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}