package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.TaskEntity
import com.example.ui.components.GutScoreBadge
import com.example.ui.theme.FlowAmberLight
import com.example.ui.theme.FlowCyanLight
import com.example.ui.theme.FlowCyanPrimary
import com.example.ui.theme.FlowEmeraldLow
import com.example.ui.viewmodel.GutFlowViewModel
import com.example.ui.viewmodel.PomodoroMode

@Composable
fun FocusModeScreen(
    viewModel: GutFlowViewModel,
    onNavigateToList: () -> Unit = {},
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val pomodoroState by viewModel.pomodoroState.collectAsStateWithLifecycle()
    val pendingTasks by viewModel.pendingTasks.collectAsStateWithLifecycle()
    val topTask by viewModel.topTask.collectAsStateWithLifecycle()

    val linkedTask = pendingTasks.find { it.id == pomodoroState.linkedTaskId } ?: topTask

    var showTaskPicker by remember { mutableStateOf(false) }

    val animatedProgress by animateFloatAsState(
        targetValue = pomodoroState.progress,
        animationSpec = tween(durationMillis = 300),
        label = "timer_progress"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("focus_mode_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with Back button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("btn_close_focus_mode")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Modo Foco Pomodoro",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Atenção deliberada na prioridade",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Mode Selector TabRow
        item {
            TabRow(
                selectedTabIndex = pomodoroState.mode.ordinal,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = FlowCyanPrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("pomodoro_mode_tabs")
            ) {
                PomodoroMode.entries.forEach { mode ->
                    val isSelected = pomodoroState.mode == mode
                    Tab(
                        selected = isSelected,
                        onClick = { viewModel.setPomodoroMode(mode) },
                        text = {
                            Text(
                                text = "${mode.label} (${mode.defaultMinutes}m)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    )
                }
            }
        }

        // Linked Task Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("focus_linked_task_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, FlowCyanPrimary.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "OBJETIVO DA SESSÃO",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = FlowCyanPrimary
                        )

                        TextButton(
                            onClick = { showTaskPicker = true },
                            modifier = Modifier.testTag("btn_change_focus_task")
                        ) {
                            Text("Trocar tarefa")
                        }
                    }

                    if (linkedTask != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = linkedTask.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${linkedTask.category} • Score GUT: ${linkedTask.gutScore}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            GutScoreBadge(score = linkedTask.gutScore, priority = linkedTask.priorityLevel)
                        }
                    } else {
                        Text(
                            text = "Nenhuma tarefa vinculada. Foco livre ou escolha uma na fila.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Dropdown for selecting task
                    DropdownMenu(
                        expanded = showTaskPicker,
                        onDismissRequest = { showTaskPicker = false }
                    ) {
                        pendingTasks.forEach { task ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(task.title, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                        Text("${task.category} • GUT: ${task.gutScore}", style = MaterialTheme.typography.bodySmall)
                                    }
                                },
                                onClick = {
                                    viewModel.linkTaskToPomodoro(task.id)
                                    showTaskPicker = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Circular Timer Visual
        item {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .padding(12.dp)
                    .testTag("circular_timer_container"),
                contentAlignment = Alignment.Center
            ) {
                // Background Track
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 14.dp,
                )

                // Active Progress
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = if (pomodoroState.mode == PomodoroMode.FOCO) FlowCyanPrimary else FlowEmeraldLow,
                    strokeWidth = 14.dp,
                )

                // Timer Digital Text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = pomodoroState.formattedTime,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = (if (pomodoroState.isRunning) FlowCyanPrimary else FlowAmberLight).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (pomodoroState.isRunning) "EM EXECUÇÃO" else "PAUSADO",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (pomodoroState.isRunning) FlowCyanPrimary else FlowAmberLight,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Control Buttons (Start / Pause, Reset)
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = { viewModel.resetPomodoro() },
                    modifier = Modifier.testTag("btn_reset_timer")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reiniciar")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reiniciar")
                }

                Button(
                    onClick = { viewModel.togglePomodoro() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (pomodoroState.isRunning) MaterialTheme.colorScheme.error else FlowCyanPrimary
                    ),
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("btn_toggle_timer")
                ) {
                    Icon(
                        imageVector = if (pomodoroState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (pomodoroState.isRunning) "Pausar" else "Iniciar"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (pomodoroState.isRunning) "Pausar Foco" else "Iniciar Foco",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                if (linkedTask != null) {
                    IconButton(
                        onClick = { viewModel.completeTask(linkedTask) },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_complete_focus_task")
                    ) {
                        Icon(
                            imageVector = Icons.Default.TaskAlt,
                            contentDescription = "Concluir Tarefa",
                            tint = FlowEmeraldLow,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // Scientific / Academic Justification of Pomodoro + GUT
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassBottom,
                            contentDescription = null,
                            tint = FlowCyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Por que o Modo Foco no GUTFlow?",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "A metodologia acadêmica comprova que a priorização sem execução imediata gera 'ansiedade de lista'. Ao vincular a tarefa #1 diretamente a um bloco de 25 minutos, eliminamos o custo de transição cognitiva, garantindo a meta de ≥70% de adesão à recomendação da Matriz GUT.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
