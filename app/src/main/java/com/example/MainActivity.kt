package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

@Composable
fun TuitionDeskApp(
    viewModel: TuitionViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val showPitchGuide by viewModel.showPitchGuideSheet.collectAsState()
    val showExportShare by viewModel.showExportShareDialog.collectAsState()
    val showWhatsAppApiSettings by viewModel.showWhatsAppApiSettings.collectAsState()

    // Handle back press: if on non-Fees tab, navigate back to Fees tab
    BackHandler(enabled = currentTab != TuitionTab.FEES) {
        viewModel.setTab(TuitionTab.FEES)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TuitionTopBar(
                currentTab = currentTab,
                onPitchGuideClick = { viewModel.setShowPitchGuideSheet(true) },
                onExportShareClick = { viewModel.setShowExportShareDialog(true) },
                onWhatsAppApiClick = { viewModel.setShowWhatsAppApiSettings(true) }
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
            TuitionTab.FEES -> FeesScreen(viewModel = viewModel, modifier = modifier)
            TuitionTab.ATTENDANCE -> AttendanceScreen(viewModel = viewModel, modifier = modifier)
            TuitionTab.TESTS -> TestsScreen(viewModel = viewModel, modifier = modifier)
            TuitionTab.PARENT -> ParentViewScreen(viewModel = viewModel, modifier = modifier)
            TuitionTab.OWNER -> OwnerDashboardScreen(viewModel = viewModel, modifier = modifier)
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
}
