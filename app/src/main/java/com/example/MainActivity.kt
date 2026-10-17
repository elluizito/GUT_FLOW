package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.XpCelebrationDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FocusModeScreen
import com.example.ui.screens.ProfileAchievementsScreen
import com.example.ui.screens.QuickGutScreen
import com.example.ui.theme.FlowCyanPrimary
import com.example.ui.theme.GUTFlowTheme
import com.example.ui.viewmodel.GutFlowViewModel
import com.example.ui.viewmodel.GutFlowViewModelFactory
import com.example.ui.viewmodel.XpCelebrationEvent
import kotlinx.coroutines.flow.collectLatest

enum class ScreenTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD("Início", Icons.Filled.Home, Icons.Outlined.Home),
    QUICK_GUT("Nova Tarefa", Icons.Filled.AddCircle, Icons.Outlined.AddCircleOutline),
    PROFILE("Perfil", Icons.Filled.Person, Icons.Outlined.Person)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GUTFlowTheme {
                GUTFlowApp()
            }
        }
    }
}

@Composable
fun GUTFlowApp() {
    val context = LocalContext.current
    val application = context.applicationContext as android.app.Application
    val viewModel: GutFlowViewModel = viewModel(
        factory = GutFlowViewModelFactory(application)
    )

    var currentTab by remember { mutableStateOf(ScreenTab.DASHBOARD) }
    var isFocusActive by remember { mutableStateOf(false) }
    var activeCelebration by remember { mutableStateOf<XpCelebrationEvent?>(null) }

    // Listen to XP celebration events (task complete, focus complete, level up)
    LaunchedEffect(Unit) {
        viewModel.celebrationEvent.collectLatest { event ->
            activeCelebration = event
        }
    }

    // Handle back button on sub-screens to return to Dashboard
    if (isFocusActive) {
        BackHandler {
            isFocusActive = false
        }
    } else if (currentTab != ScreenTab.DASHBOARD) {
        BackHandler {
            currentTab = ScreenTab.DASHBOARD
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                ScreenTab.entries.forEach { tab ->
                    val isSelected = !isFocusActive && currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            isFocusActive = false
                            currentTab = tab
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(28.dp)
                            )
                        },
                        alwaysShowLabel = false,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = FlowCyanPrimary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = FlowCyanPrimary.copy(alpha = 0.16f)
                        ),
                        modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isFocusActive) {
                FocusModeScreen(
                    viewModel = viewModel,
                    onClose = { isFocusActive = false }
                )
            } else {
                when (currentTab) {
                    ScreenTab.DASHBOARD -> {
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToNewTask = { currentTab = ScreenTab.QUICK_GUT },
                            onStartFocus = { taskId ->
                                viewModel.linkTaskToPomodoro(taskId)
                                isFocusActive = true
                            }
                        )
                    }
                    ScreenTab.QUICK_GUT -> {
                        QuickGutScreen(
                            viewModel = viewModel,
                            onTaskCreated = { currentTab = ScreenTab.DASHBOARD }
                        )
                    }
                    ScreenTab.PROFILE -> {
                        ProfileAchievementsScreen(
                            viewModel = viewModel
                        )
                    }
                }
            }

            // Celebration Modal overlay
            activeCelebration?.let { event ->
                XpCelebrationDialog(
                    event = event,
                    onDismiss = { activeCelebration = null }
                )
            }
        }
    }
}
