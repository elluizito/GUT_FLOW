package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.PriorityLevel
import com.example.data.local.entity.TaskEntity
import com.example.ui.components.CompactLevelBadge
import com.example.ui.components.EditGutDialog
import com.example.ui.components.GutScoreBadge
import com.example.ui.components.GutSignalMeters
import com.example.ui.components.StreakBadge
import com.example.ui.components.WhyFirstExplanationDialog
import com.example.ui.theme.FlowAmberLight
import com.example.ui.theme.FlowCrimsonCritical
import com.example.ui.theme.FlowCyanPrimary
import com.example.ui.theme.FlowEmeraldLow
import com.example.ui.theme.FlowOrangeHigh
import com.example.ui.viewmodel.GutFlowViewModel
import com.example.ui.viewmodel.PriorityFilter

@Composable
fun DashboardScreen(
    viewModel: GutFlowViewModel,
    onNavigateToNewTask: () -> Unit,
    onStartFocus: (taskId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val topTask by viewModel.topTask.collectAsStateWithLifecycle()
    val filteredTasks by viewModel.filteredTasks.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val pendingTasks by viewModel.pendingTasks.collectAsStateWithLifecycle()
    val completedTasks by viewModel.completedTasks.collectAsStateWithLifecycle()

    var taskForWhyDialog by remember { mutableStateOf<TaskEntity?>(null) }
    var taskForEditDialog by remember { mutableStateOf<TaskEntity?>(null) }
    var taskForDeleteConfirm by remember { mutableStateOf<TaskEntity?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // A. Header com Gamificação condensada (Nv. 1 + Streak lado a lado no topo absoluto)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "GUTFlow",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Central de Missão",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Condensado: Nível circular compacto + Streak de dias
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompactLevelBadge(
                        level = userProfile.level,
                        progress = userProfile.currentLevelProgress
                    )
                    StreakBadge(streak = userProfile.currentStreak)
                }
            }
        }

        // B. Card "Missão Crítica do Momento" (O Coração da App - Acima da dobra)
        item {
            val missionHeaderTitle = when (topTask?.priorityLevel) {
                PriorityLevel.CRITICA -> "Missão Crítica do Momento"
                PriorityLevel.ALTA -> "Missão de Alta Prioridade"
                PriorityLevel.MEDIA -> "Missão de Média Prioridade"
                PriorityLevel.BAIXA -> "Missão de Baixa Prioridade"
                null -> "Missão Prioritária do Momento"
            }
            Text(
                text = missionHeaderTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            if (topTask != null) {
                val task = topTask!!
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hero_critical_task_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(task.priorityLevel.colorHex).copy(alpha = 0.12f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Top Row: Ranking Badge + Categoria + GUT Score Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(task.priorityLevel.colorHex),
                                        modifier = Modifier.testTag("rank_1_badge")
                                    ) {
                                        Text(
                                            text = "#1 PRIORIDADE",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surface
                                    ) {
                                        Text(
                                            text = task.category,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                GutScoreBadge(score = task.gutScore, priority = task.priorityLevel)
                            }

                            // Título & Descrição
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (task.description.isNotBlank()) {
                                Text(
                                    text = task.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Medidores Visuais tipo sinal de telemóvel (G, U, T) substituindo números secos
                            GutSignalMeters(
                                gravity = task.gravity,
                                urgency = task.urgency,
                                trend = task.trend
                            )

                            // Hierarquia de Botões: "Por que #1?", "Focar Agora" (destacado e brilhante) e "✔" (ícone de contorno discreto)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { taskForWhyDialog = task },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_why_first"),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.HelpOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Por que #1?",
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1
                                    )
                                }

                                // Botão Principal: Preenchido, brilhante e convidativo
                                Button(
                                    onClick = { onStartFocus(task.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = FlowCyanPrimary),
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .testTag("btn_focus_top_task"),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Focar Agora",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1
                                    )
                                }

                                // Botão Concluir: Ícone de contorno (outline) discreto para evitar cliques acidentais
                                OutlinedIconButton(
                                    onClick = { viewModel.completeTask(task) },
                                    border = BorderStroke(1.dp, FlowEmeraldLow.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .size(40.dp)
                                        .testTag("btn_complete_top_task")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = "Concluir Tarefa",
                                        tint = FlowEmeraldLow,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TaskAlt,
                            contentDescription = null,
                            tint = FlowEmeraldLow,
                            modifier = Modifier.size(38.dp)
                        )
                        Text(
                            text = "Nenhuma prioridade pendente!",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Cadastre uma nova missão avaliando seu GUT.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = onNavigateToNewTask,
                            border = BorderStroke(1.dp, FlowCyanPrimary.copy(alpha = 0.6f)),
                            modifier = Modifier.testTag("btn_add_first_task")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = FlowCyanPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cadastrar Nova Tarefa", color = FlowCyanPrimary)
                        }
                    }
                }
            }
        }

        // C. Missões Diárias de Foco (Gamificação contemporânea mantida)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CrisisAlert,
                                contentDescription = null,
                                tint = FlowAmberLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Missões Diárias de Foco",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "+100 XP bônus",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = FlowAmberLight
                        )
                    }

                    DailyQuestRow(
                        title = "Avaliar tarefa na Matriz GUT",
                        isCompleted = userProfile.evaluatedTasksCount > 0,
                        reward = "+20 XP"
                    )

                    DailyQuestRow(
                        title = "Completar a prioridade crítica do dia",
                        isCompleted = userProfile.completedTasksCount > 0,
                        reward = "+50 XP"
                    )

                    DailyQuestRow(
                        title = "Realizar 1 bloco no Modo Foco",
                        isCompleted = userProfile.totalFocusMinutes >= 25,
                        reward = "+30 XP"
                    )
                }
            }
        }

        // D. Fila Priorizada GUT (Acesso direto por scroll na tela inicial)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fila de Prioridades GUT",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "${filteredTasks.size} itens",
                    style = MaterialTheme.typography.labelMedium,
                    color = FlowCyanPrimary
                )
            }
        }

        // Filtros rápidos (Críticas por padrão)
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(PriorityFilter.entries.size) { index ->
                    val filter = PriorityFilter.entries[index]
                    val count = when (filter) {
                        PriorityFilter.CRITICAS -> pendingTasks.count { it.priorityLevel == PriorityLevel.CRITICA }
                        PriorityFilter.ALTAS -> pendingTasks.count { it.priorityLevel == PriorityLevel.ALTA }
                        PriorityFilter.MEDIAS -> pendingTasks.count { it.priorityLevel == PriorityLevel.MEDIA }
                        PriorityFilter.BAIXAS -> pendingTasks.count { it.priorityLevel == PriorityLevel.BAIXA }
                        PriorityFilter.TODAS -> pendingTasks.size
                        PriorityFilter.CONCLUIDAS -> completedTasks.size
                    }
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { viewModel.setFilter(filter) },
                        label = {
                            Text(if (count > 0) "${filter.label} ($count)" else filter.label)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FlowCyanPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = FlowCyanPrimary
                        ),
                        modifier = Modifier.testTag("filter_chip_${filter.name.lowercase()}")
                    )
                }
            }
        }

        // Tarefas na Fila
        if (filteredTasks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (pendingTasks.isEmpty()) {
                            Icon(
                                imageVector = Icons.Default.TaskAlt,
                                contentDescription = null,
                                tint = FlowEmeraldLow,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "Fila de Prioridades Zerada! 🎉",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Excelente trabalho! Todas as pendências da sua matriz GUT foram concluídas com sucesso.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedButton(
                                onClick = onNavigateToNewTask,
                                border = BorderStroke(1.dp, FlowCyanPrimary.copy(alpha = 0.6f)),
                                modifier = Modifier.testTag("btn_empty_queue_add_task")
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = FlowCyanPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cadastrar Nova Tarefa", color = FlowCyanPrimary)
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Nenhuma tarefa em \"${selectedFilter.label}\"",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Você possui ${pendingTasks.size} tarefa(s) pendente(s) em outras faixas.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.autoSelectHighestAvailableTier() },
                                    colors = ButtonDefaults.buttonColors(containerColor = FlowCyanPrimary),
                                    modifier = Modifier.testTag("btn_go_to_next_tier")
                                ) {
                                    Text("Ver Próxima Prioridade")
                                }
                                OutlinedButton(
                                    onClick = onNavigateToNewTask,
                                    border = BorderStroke(1.dp, FlowCyanPrimary.copy(alpha = 0.6f)),
                                    modifier = Modifier.testTag("btn_empty_queue_add_task")
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = FlowCyanPrimary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Nova Tarefa", color = FlowCyanPrimary)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            itemsIndexed(filteredTasks, key = { _, task -> task.id }) { index, task ->
                val rankNumber = index + 1
                TaskItemCard(
                    task = task,
                    rankNumber = rankNumber,
                    onComplete = { viewModel.completeTask(task) },
                    onWhyClick = { taskForWhyDialog = task },
                    onEditClick = { taskForEditDialog = task },
                    onDeleteClick = { taskForDeleteConfirm = task },
                    onFocusClick = { onStartFocus(task.id) }
                )
            }
        }
    }

    // Modal: Por que isso está em #1?
    taskForWhyDialog?.let { task ->
        WhyFirstExplanationDialog(
            task = task,
            onDismiss = { taskForWhyDialog = null },
            onFocusClick = {
                onStartFocus(task.id)
            }
        )
    }

    // Modal: Repriorização dinâmica
    taskForEditDialog?.let { task ->
        EditGutDialog(
            task = task,
            onSave = { g, u, t ->
                viewModel.updateTaskGUT(task, g, u, t)
                taskForEditDialog = null
            },
            onDismiss = { taskForEditDialog = null }
        )
    }

    // Modal: Confirmar exclusão
    taskForDeleteConfirm?.let { task ->
        AlertDialog(
            onDismissRequest = { taskForDeleteConfirm = null },
            title = { Text("Excluir Tarefa") },
            text = { Text("Deseja realmente remover \"${task.title}\" da fila?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTask(task.id)
                        taskForDeleteConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { taskForDeleteConfirm = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun TaskItemCard(
    task: TaskEntity,
    rankNumber: Int,
    onComplete: () -> Unit,
    onWhyClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onFocusClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_item_${task.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = if (!task.isCompleted && rankNumber == 1) {
            BorderStroke(1.5.dp, Color(task.priorityLevel.colorHex))
        } else {
            null
        }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Rank badge, Category, GUT Score Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (rankNumber == 1 && !task.isCompleted) Color(task.priorityLevel.colorHex) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "#$rankNumber",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (rankNumber == 1 && !task.isCompleted) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = task.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                GutScoreBadge(score = task.gutScore, priority = task.priorityLevel)
            }

            // Title & Description
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!task.isCompleted) {
                    OutlinedIconButton(
                        onClick = onComplete,
                        border = BorderStroke(1.dp, FlowEmeraldLow.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("btn_check_task_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = "Concluir Tarefa",
                            tint = FlowEmeraldLow,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )

                    if (task.description.isNotBlank()) {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Visual Signal Meters (G, U, T) & Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GutSignalMeters(
                    gravity = task.gravity,
                    urgency = task.urgency,
                    trend = task.trend
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Why explanation button
                    IconButton(
                        onClick = onWhyClick,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_why_task_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.HelpOutline,
                            contentDescription = "Por que este score?",
                            tint = FlowCyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (!task.isCompleted) {
                        // Focus mode link
                        IconButton(
                            onClick = onFocusClick,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("btn_focus_task_${task.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Modo Foco",
                                tint = FlowCyanPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Edit GUT
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("btn_edit_task_${task.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Repriorizar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Delete
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_delete_task_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Excluir",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyQuestRow(
    title: String,
    isCompleted: Boolean,
    reward: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (isCompleted) FlowEmeraldLow else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isCompleted) FontWeight.Medium else FontWeight.Normal
                ),
                color = if (isCompleted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = reward,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = if (isCompleted) FlowEmeraldLow else FlowAmberLight
        )
    }
}
