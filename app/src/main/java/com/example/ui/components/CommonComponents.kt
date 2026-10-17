package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.PriorityLevel
import com.example.data.local.entity.TaskEntity
import com.example.ui.theme.FlowAmberLight
import com.example.ui.theme.FlowCrimsonCritical
import com.example.ui.theme.FlowCyanLight
import com.example.ui.theme.FlowCyanPrimary
import com.example.ui.theme.FlowEmeraldLow
import com.example.ui.theme.FlowOrangeHigh
import com.example.ui.theme.FlowYellowMedium
import com.example.ui.theme.GoldAccent
import com.example.ui.viewmodel.XpCelebrationEvent

@Composable
fun GutScoreBadge(
    score: Int,
    priority: PriorityLevel,
    modifier: Modifier = Modifier
) {
    val bgColor = Color(priority.colorHex)
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .testTag("gut_score_badge_${priority.name.lowercase()}"),
        color = bgColor.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, bgColor.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(bgColor)
            )
            Text(
                text = "$score pts",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = bgColor
            )
            Text(
                text = "• ${priority.tagText}",
                style = MaterialTheme.typography.labelSmall,
                color = bgColor
            )
        }
    }
}

@Composable
fun CompactLevelBadge(
    level: Int,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.testTag("compact_level_badge"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, FlowCyanPrimary.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier.size(22.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    strokeWidth = 2.5.dp
                )
                androidx.compose.material3.CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = FlowCyanPrimary,
                    strokeWidth = 2.5.dp
                )
                Text(
                    text = "$level",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "Nv. $level",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = FlowCyanPrimary
            )
        }
    }
}

@Composable
fun GutSignalMeters(
    gravity: Int,
    urgency: Int,
    trend: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.testTag("gut_signal_meters"),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SignalMeterItem(label = "G", value = gravity, activeColor = FlowCrimsonCritical)
        SignalMeterItem(label = "U", value = urgency, activeColor = FlowOrangeHigh)
        SignalMeterItem(label = "T", value = trend, activeColor = FlowCyanPrimary)
    }
}

@Composable
private fun SignalMeterItem(
    label: String,
    value: Int,
    activeColor: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                color = activeColor,
                modifier = Modifier.padding(end = 2.dp)
            )
            // 5 vertical signal bars
            val heights = listOf(5.dp, 8.dp, 11.dp, 14.dp, 17.dp)
            for (i in 1..5) {
                val isFilled = i <= value
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(heights[i - 1])
                        .clip(RoundedCornerShape(1.dp))
                        .background(
                            if (isFilled) activeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                        )
                )
            }
        }
    }
}

@Composable
fun GutDimensionChips(
    gravity: Int,
    urgency: Int,
    trend: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        DimensionPill(label = "G", value = gravity, color = FlowCrimsonCritical)
        DimensionPill(label = "U", value = urgency, color = FlowOrangeHigh)
        DimensionPill(label = "T", value = trend, color = FlowCyanPrimary)
    }
}

@Composable
private fun DimensionPill(label: String, value: Int, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text(
                text = "$value/5",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun LevelProgressBar(
    level: Int,
    xp: Int,
    levelTitle: String,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600),
        label = "level_progress"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(FlowCyanPrimary, FlowCyanLight))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$level",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                    Column {
                        Text(
                            text = "Nível $level • $levelTitle",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$xp XP acumulados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GoldAccent.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = FlowAmberLight,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${(progress * 100).toInt()}% para Nv. ${level + 1}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = FlowAmberLight
                        )
                    }
                }
            }

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = FlowCyanLight,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
            )
        }
    }
}

@Composable
fun StreakBadge(streak: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.testTag("streak_badge"),
        shape = RoundedCornerShape(16.dp),
        color = FlowOrangeHigh.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, FlowOrangeHigh.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = "Sequência de Foco",
                tint = FlowOrangeHigh,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "$streak ${if (streak == 1) "dia" else "dias"}",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = FlowOrangeHigh
            )
        }
    }
}

@Composable
fun WhyFirstExplanationDialog(
    task: TaskEntity,
    onDismiss: () -> Unit,
    onFocusClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.HelpOutline,
                    contentDescription = null,
                    tint = FlowCyanPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Por que isso está em #1?",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "\"${task.title}\"",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                // Calculation breakdown card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "FÓRMULA MATRIZ GUT",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Gravidade (${task.gravity}) × Urgência (${task.urgency}) × Tendência (${task.trend}) = ${task.gutScore} pontos",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Escala máxima possível: 125 pontos. Score obtido: ${(task.gutScore * 100) / 125}% da urgência máxima.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Dimension Detailed Analysis
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DimensionExplanationRow(
                        letter = "G",
                        name = "Gravidade (Impacto)",
                        value = task.gravity,
                        desc = when (task.gravity) {
                            5 -> "Catastrófico: Causa prejuízos irreversíveis e danos gravíssimos."
                            4 -> "Muito prejudicial: Impacto severo na operação ou vida pessoal."
                            3 -> "Médio: Prejuízo controlável, porém evidente."
                            2 -> "Baixo: Incômodo reduzido, facilmente contornável."
                            else -> "Mínimo: Impacto desprezível."
                        },
                        color = FlowCrimsonCritical
                    )

                    DimensionExplanationRow(
                        letter = "U",
                        name = "Urgência (Prazo)",
                        value = task.urgency,
                        desc = when (task.urgency) {
                            5 -> "Emergência imediata: Exige intervenção sem qualquer demora."
                            4 -> "Urgente: Prazo muito curto (hoje ou poucas horas)."
                            3 -> "Médio prazo: Precisa de resolução nos próximos dias."
                            2 -> "Sem urgência: Pode aguardar planejamento."
                            else -> "Folga total: Não há pressão temporal."
                        },
                        color = FlowOrangeHigh
                    )

                    DimensionExplanationRow(
                        letter = "T",
                        name = "Tendência (Evolução sem ação)",
                        value = task.trend,
                        desc = when (task.trend) {
                            5 -> "Explosiva: Vai se agravar de forma desastrosa instantaneamente."
                            4 -> "Rápida piora: O atraso multiplicará o trabalho necessário."
                            3 -> "Piora gradual: Situação deteriora em ritmo constante."
                            2 -> "Piora lenta: Pouca evolução negativa no curto prazo."
                            else -> "Estável: A situação permanece inalterada mesmo sem ação."
                        },
                        color = FlowCyanPrimary
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Academic Action Advice
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "DIRETRIZ DE AÇÃO RECOMENDADA",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = task.priorityLevel.actionAdvice,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onFocusClick()
                },
                colors = ButtonDefaults.buttonColors(containerColor = FlowCyanPrimary),
                modifier = Modifier.testTag("action_focus_from_why")
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Executar no Modo Foco")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Entendido")
            }
        }
    )
}

@Composable
private fun DimensionExplanationRow(
    letter: String,
    name: String,
    value: Int,
    desc: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(24.dp),
            shape = CircleShape,
            color = color.copy(alpha = 0.2f),
            border = androidx.compose.foundation.BorderStroke(1.dp, color)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = letter,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = color
                )
            }
        }
        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "($value/5)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = color
                )
            }
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EditGutDialog(
    task: TaskEntity,
    onSave: (g: Int, u: Int, t: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var gravity by remember { mutableIntStateOf(task.gravity) }
    var urgency by remember { mutableIntStateOf(task.urgency) }
    var trend by remember { mutableIntStateOf(task.trend) }
    val newScore = gravity * urgency * trend

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Repriorizar Tarefa",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Sliders
                GutSliderItem(
                    label = "Gravidade (Impacto)",
                    value = gravity,
                    color = FlowCrimsonCritical,
                    onValueChange = { gravity = it }
                )

                GutSliderItem(
                    label = "Urgência (Prazo)",
                    value = urgency,
                    color = FlowOrangeHigh,
                    onValueChange = { urgency = it }
                )

                GutSliderItem(
                    label = "Tendência (Piora)",
                    value = trend,
                    color = FlowCyanPrimary,
                    onValueChange = { trend = it }
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Novo Score GUT:",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = "$gravity × $urgency × $trend = $newScore pts",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = FlowCyanPrimary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(gravity, urgency, trend) },
                colors = ButtonDefaults.buttonColors(containerColor = FlowCyanPrimary),
                modifier = Modifier.testTag("save_gut_edit_button")
            ) {
                Text("Salvar Repriorização")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun GutSliderItem(
    label: String,
    value: Int,
    color: Color,
    onValueChange: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$value de 5",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt().coerceIn(1, 5)) },
            valueRange = 1f..5f,
            steps = 3,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = color.copy(alpha = 0.2f)
            )
        )
    }
}

@Composable
fun XpCelebrationDialog(
    event: XpCelebrationEvent,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("xp_celebration_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(FlowAmberLight, GoldAccent))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = if (event.isLevelUp) "SUBIU DE NÍVEL!" else "MISSÃO CUMPRIDA!",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (event.isLevelUp) FlowAmberLight else FlowEmeraldLow,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = event.taskTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FlowAmberLight.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FlowAmberLight.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = FlowAmberLight
                        )
                        Text(
                            text = "+${event.xpEarned} XP",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = FlowAmberLight
                        )
                    }
                }

                if (event.isLevelUp) {
                    Text(
                        text = "Parabéns! Você alcançou o Nível ${event.newLevel}. Seu poder de foco e decisão aumentou!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = FlowCyanPrimary),
                    modifier = Modifier.fillMaxWidth().testTag("celebration_continue_button")
                ) {
                    Text("Continuar Evoluindo")
                }
            }
        }
    }
}
