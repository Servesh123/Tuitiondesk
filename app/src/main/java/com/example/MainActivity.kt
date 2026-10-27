package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.PitchGuideDialog
import com.example.ui.components.TuitionBottomNavigation
import com.example.ui.components.TuitionTopBar
import com.example.ui.screens.*
import com.example.ui.theme.TuitionDeskTheme
import com.example.viewmodel.TuitionTab
import com.example.viewmodel.TuitionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TuitionDeskTheme {
                TuitionDeskApp()
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TuitionDeskApp(
    viewModel: TuitionViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val centreInfo by viewModel.centreInfo.collectAsState()
    val showPitchGuide by viewModel.showPitchGuideSheet.collectAsState()
    val showExportShare by viewModel.showExportShareDialog.collectAsState()
    val showWhatsAppApiSettings by viewModel.showWhatsAppApiSettings.collectAsState()
    val showParentPortal by viewModel.showParentPortalDialog.collectAsState()
    val showTestsReport by viewModel.showTestsReportDialog.collectAsState()

    // Handle back press: if on non-Dashboard tab, navigate back to Dashboard
    BackHandler(enabled = currentTab != TuitionTab.DASHBOARD) {
        viewModel.setTab(TuitionTab.DASHBOARD)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TuitionTopBar(
                currentTab = currentTab,
                centreInfo = centreInfo,
                onPitchGuideClick = { viewModel.setShowPitchGuideSheet(true) },
                onExportShareClick = { viewModel.setShowExportShareDialog(true) },
                onWhatsAppApiClick = { viewModel.setShowWhatsAppApiSettings(true) },
                onProfileClick = { viewModel.setTab(TuitionTab.SETTINGS) }
            )
        },
        bottomBar = {
            TuitionBottomNavigation(
                currentTab = currentTab,
                onTabSelected = { tab -> viewModel.setTab(tab) }
            )
        }
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        when (currentTab) {
            TuitionTab.DASHBOARD -> OwnerDashboardScreen(viewModel = viewModel, modifier = modifier)
            TuitionTab.STUDENTS -> StudentsScreen(viewModel = viewModel, modifier = modifier)
            TuitionTab.ATTENDANCE -> AttendanceScreen(viewModel = viewModel, modifier = modifier)
            TuitionTab.FEES -> FeesScreen(viewModel = viewModel, modifier = modifier)
            TuitionTab.SETTINGS -> ProfileSettingsScreen(viewModel = viewModel, modifier = modifier)
        }
    }

    if (showPitchGuide) {
        PitchGuideDialog(
            onDismiss = { viewModel.setShowPitchGuideSheet(false) }
        )
    }

    if (showExportShare) {
        com.example.ui.components.ExportShareDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.setShowExportShareDialog(false) }
        )
    }

    if (showWhatsAppApiSettings) {
        com.example.ui.components.WhatsAppApiSettingsDialog(
            onDismiss = { viewModel.setShowWhatsAppApiSettings(false) }
        )
    }

    if (showParentPortal) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { viewModel.setShowParentPortalDialog(false) },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Parent Portal Preview & UPI Link", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { viewModel.setShowParentPortalDialog(false) }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                    )
                }
            ) { padding ->
                ParentViewScreen(viewModel = viewModel, modifier = Modifier.padding(padding))
            }
        }
    }

    if (showTestsReport) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { viewModel.setShowTestsReportDialog(false) },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Weekly Tests & Leaderboard", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { viewModel.setShowTestsReportDialog(false) }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                    )
                }
            ) { padding ->
                TestsScreen(viewModel = viewModel, modifier = Modifier.padding(padding))
            }
        }
    }

}

