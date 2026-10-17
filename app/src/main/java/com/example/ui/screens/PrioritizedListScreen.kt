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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.PriorityLevel
import com.example.data.local.entity.TaskEntity
import com.example.ui.components.EditGutDialog
import com.example.ui.components.GutDimensionChips
import com.example.ui.components.GutScoreBadge
import com.example.ui.components.WhyFirstExplanationDialog
import com.example.ui.theme.FlowCyanPrimary
import com.example.ui.theme.FlowEmeraldLow
import com.example.ui.viewmodel.GutFlowViewModel
import com.example.ui.viewmodel.PriorityFilter

@Composable
fun PrioritizedListScreen(
    viewModel: GutFlowViewModel,
    onNavigateToNewTask: () -> Unit,
    onNavigateToFocus: (taskId: Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredTasks by viewModel.filteredTasks.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val pendingTasks by viewModel.pendingTasks.collectAsStateWithLifecycle()
    val completedTasks by viewModel.completedTasks.collectAsStateWithLifecycle()

    var taskForWhyDialog by remember { mutableStateOf<TaskEntity?>(null) }
    var taskForEditDialog by remember { mutableStateOf<TaskEntity?>(null) }
    var taskForDeleteConfirm by remember { mutableStateOf<TaskEntity?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("prioritized_list_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Fila Priorizada",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Tarefas ordenadas pelo Score GUT decrescente",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Buscar tarefas ou categorias...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = if (searchQuery.isNotBlank()) {
                    {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpar busca")
                        }
                    }
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_search_tasks"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FlowCyanPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            )
        }

        // Filter Chips Row
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

        // List of tasks
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
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "Nenhuma tarefa encontrada",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Cadastre uma nova tarefa ou altere o filtro selecionado.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onNavigateToNewTask,
                            colors = ButtonDefaults.buttonColors(containerColor = FlowCyanPrimary),
                            modifier = Modifier.testTag("btn_empty_list_add_task")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Nova Tarefa GUT")
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
                    onFocusClick = {
                        viewModel.linkTaskToPomodoro(task.id)
                        onNavigateToFocus(task.id)
                    }
                )
            }
        }
    }

    // Modal: "Por que isso está nesta posição?"
    taskForWhyDialog?.let { task ->
        WhyFirstExplanationDialog(
            task = task,
            onDismiss = { taskForWhyDialog = null },
            onFocusClick = {
                viewModel.linkTaskToPomodoro(task.id)
                onNavigateToFocus(task.id)
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

    // Confirm Delete Dialog
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
                    IconButton(
                        onClick = onComplete,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_check_task_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.TaskAlt,
                            contentDescription = "Concluir Tarefa",
                            tint = FlowEmeraldLow
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

            // Dimension Indicators & Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GutDimensionChips(
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
