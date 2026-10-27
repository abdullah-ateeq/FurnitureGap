package com.example.furnituregap.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null,
    val isTopLevel: Boolean = false
) {
    object Splash : Screen("splash", "Splash")
    object Home : Screen("home", "Calculator", Icons.Default.Calculate, isTopLevel = true)
    object FloorPlan : Screen("floor_plan", "Floor Plan", Icons.Default.ViewInAr, isTopLevel = true)
    object ClearanceAnalysis : Screen("clearance_analysis", "Analysis", Icons.Default.Straighten, isTopLevel = true)
    object SavedPlans : Screen("saved_plans", "Plans", Icons.Default.Folder, isTopLevel = true)
    object History : Screen("history", "History", Icons.Default.History, isTopLevel = true)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings, isTopLevel = true)
    object About : Screen("about", "About", Icons.Default.Info, isTopLevel = true)

    companion object {
        val navItems = listOf(Home, FloorPlan, ClearanceAnalysis, SavedPlans, History, Settings, About)
    }
}
