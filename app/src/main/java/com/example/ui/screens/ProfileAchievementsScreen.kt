package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.UserProfileEntity
import com.example.ui.components.LevelProgressBar
import com.example.ui.theme.FlowAmberLight
import com.example.ui.theme.FlowCrimsonCritical
import com.example.ui.theme.FlowCyanLight
import com.example.ui.theme.FlowCyanPrimary
import com.example.ui.theme.FlowEmeraldLow
import com.example.ui.theme.FlowOrangeHigh
import com.example.ui.theme.GoldAccent
import com.example.ui.viewmodel.GutFlowViewModel

enum class TrophyMaterial(
    val label: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val glowColor: Color,
    val textColor: Color
) {
    BRONZE(
        label = "Bronze",
        primaryColor = Color(0xFFCD7F32),
        secondaryColor = Color(0xFF6E3A07),
        glowColor = Color(0xFFB87333),
        textColor = Color(0xFFFFD5B3)
    ),
    PRATA(
        label = "Prata",
        primaryColor = Color(0xFFE2E8F0),
        secondaryColor = Color(0xFF475569),
        glowColor = Color(0xFFCBD5E1),
        textColor = Color(0xFFFFFFFF)
    ),
    OURO(
        label = "Ouro",
        primaryColor = Color(0xFFFBBF24),
        secondaryColor = Color(0xFFB45309),
        glowColor = Color(0xFFF59E0B),
        textColor = Color(0xFFFFFBEB)
    ),
    PLATINA(
        label = "Platina",
        primaryColor = Color(0xFF38BDF8),
        secondaryColor = Color(0xFF0369A1),
        glowColor = Color(0xFF06B6D4),
        textColor = Color(0xFFE0F2FE)
    )
}

data class TrophyItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val currentTier: TrophyMaterial,
    val currentProgress: Int,
    val targetProgress: Int,
    val unitLabel: String,
    val nextXpBonus: Int,
    val isUnlocked: Boolean
) {
    val progressRatio: Float
        get() = if (targetProgress > 0) (currentProgress.toFloat() / targetProgress.toFloat()).coerceIn(0f, 1f) else 1f
}

@Composable
fun ProfileAchievementsScreen(
    viewModel: GutFlowViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    var showDeveloperDialog by remember { mutableStateOf(false) }
    var selectedTrophyForDetail by remember { mutableStateOf<TrophyItem?>(null) }

    // Map user progress to the 4-tier Trophy Collection
    val trophies = remember(userProfile) {
        listOf(
            calculateTrophy(
                id = "first_task",
                title = "Iniciador do Fluxo",
                description = "Avalie e estruture tarefas utilizando as 3 perguntas naturais da Matriz GUT.",
                icon = Icons.Default.Flag,
                progress = userProfile.evaluatedTasksCount,
                thresholds = listOf(1, 5, 15, 30),
                unit = "tarefas avaliadas",
                xpBase = 50
            ),
            calculateTrophy(
                id = "crisis_tamer",
                title = "Domador de Crises",
                description = "Resolva missões com prioridade Crítica (Score GUT superior a 60).",
                icon = Icons.Default.LocalFireDepartment,
                progress = userProfile.criticalTasksSolved,
                thresholds = listOf(1, 3, 7, 15),
                unit = "crises mitigadas",
                xpBase = 100
            ),
            calculateTrophy(
                id = "focus_master",
                title = "Foco Inabalável",
                description = "Acumule blocos deliberados de atenção contínua no Modo Foco.",
                icon = Icons.Default.Timer,
                progress = userProfile.totalFocusMinutes,
                thresholds = listOf(25, 75, 150, 300),
                unit = "minutos focados",
                xpBase = 60
            ),
            calculateTrophy(
                id = "matrix_strategist",
                title = "Mente Estratégica",
                description = "Crie consistência priorizando decisões antes de agir.",
                icon = Icons.Default.Psychology,
                progress = userProfile.evaluatedTasksCount,
                thresholds = listOf(3, 10, 25, 50),
                unit = "decisões registradas",
                xpBase = 80
            ),
            calculateTrophy(
                id = "streak_champion",
                title = "Sequência de Ouro",
                description = "Mantenha uma rotina ininterrupta de foco diário.",
                icon = Icons.Default.Bolt,
                progress = userProfile.currentStreak,
                thresholds = listOf(2, 5, 10, 20),
                unit = "dias consecutivos",
                xpBase = 120
            ),
            calculateTrophy(
                id = "productivity_titan",
                title = "Titã da Execução",
                description = "Conclua tarefas priorizadas com êxito eliminando gargalos.",
                icon = Icons.Default.MilitaryTech,
                progress = userProfile.completedTasksCount,
                thresholds = listOf(3, 10, 25, 50),
                unit = "tarefas concluídas",
                xpBase = 150
            ),
            calculateTrophy(
                id = "rank_commander",
                title = "Comandante do Nível",
                description = "Alcance novos patamares de maestria e experiência pessoal.",
                icon = Icons.Default.WorkspacePremium,
                progress = userProfile.level,
                thresholds = listOf(2, 3, 4, 5),
                unit = "níveis de maestria",
                xpBase = 200
            ),
            calculateTrophy(
                id = "unstoppable",
                title = "Fluxo Imparável",
                description = "Pontuação combinada de tarefas concluídas e sessões de foco.",
                icon = Icons.Default.Star,
                progress = userProfile.completedTasksCount + (userProfile.totalFocusMinutes / 25),
                thresholds = listOf(5, 15, 30, 60),
                unit = "marcos alcançados",
                xpBase = 180
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_achievements_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Cabeçalho Limpo com Acesso Oculto ao Modo Desenvolvedor / Avaliador
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Perfil & Conquistas",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Sua evolução e galeria de maestria",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Ícone discreto para a Banca / Desenvolvedor (oculto para o utilizador comum)
                IconButton(
                    onClick = { showDeveloperDialog = true },
                    modifier = Modifier.testTag("btn_evaluator_settings")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Definições da Avaliação",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // 2. Profile Hero & Level Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(FlowCyanPrimary, FlowAmberLight))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MilitaryTech,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userProfile.levelTitle,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Nível ${userProfile.level} • ${userProfile.xp} XP total acumulados",
                                style = MaterialTheme.typography.bodyMedium,
                                color = FlowCyanPrimary
                            )
                        }
                    }

                    LevelProgressBar(
                        level = userProfile.level,
                        xp = userProfile.xp,
                        levelTitle = userProfile.levelTitle,
                        progress = userProfile.currentLevelProgress
                    )
                }
            }
        }

        // 3. Métricas de Impacto Comportamental (Movidas para o Perfil)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Métricas de Impacto Comportamental",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProfileMetricCard(
                        title = "Tarefas Concluídas",
                        value = "${userProfile.completedTasksCount}",
                        subtitle = "Gargalos eliminados",
                        icon = Icons.Default.Check,
                        accentColor = FlowEmeraldLow,
                        modifier = Modifier.weight(1f)
                    )
                    ProfileMetricCard(
                        title = "Tempo em Foco",
                        value = "${userProfile.totalFocusMinutes}m",
                        subtitle = "Atenção deliberada",
                        icon = Icons.Default.Timer,
                        accentColor = FlowCyanPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    ProfileMetricCard(
                        title = "Crises Mitigadas",
                        value = "${userProfile.criticalTasksSolved}",
                        subtitle = "Score GUT ≥ 60",
                        icon = Icons.Default.LocalFireDepartment,
                        accentColor = FlowCrimsonCritical,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. A. A "Sala de Troféus" (Grelha de Insígnias com Foco Visual)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Sala de Troféus",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Toque numa insígnia para inspecionar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FlowAmberLight.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, FlowAmberLight.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "${trophies.size} Medalhas",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = FlowAmberLight,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Grelha de 4 colunas exibindo apenas a insígnia circular com anel exterior de progresso
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val rows = trophies.chunked(4)
                    rows.forEach { rowTrophies ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            rowTrophies.forEach { trophy ->
                                TrophyGridBadge(
                                    trophy = trophy,
                                    onClick = { selectedTrophyForDetail = trophy }
                                )
                            }
                            // Filler if row has less than 4
                            for (i in 0 until (4 - rowTrophies.size)) {
                                Spacer(modifier = Modifier.size(64.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // D. Modal de Detalhe Flutuante ao Tocar na Medalha
    selectedTrophyForDetail?.let { trophy ->
        TrophyDetailDialog(
            trophy = trophy,
            onDismiss = { selectedTrophyForDetail = null }
        )
    }

    // C. Painel Oculto do Avaliador / Banca Acadêmica
    if (showDeveloperDialog) {
        EvaluatorSettingsDialog(
            viewModel = viewModel,
            onDismiss = { showDeveloperDialog = false }
        )
    }
}

/**
 * Insígnia Circular Minimalista para a Grelha
 * Sem texto ao lado na visão geral, foco 100% no visual e no anel de progresso exterior.
 */
@Composable
private fun TrophyGridBadge(
    trophy: TrophyItem,
    onClick: () -> Unit
) {
    val tier = trophy.currentTier
    val isUnlocked = trophy.isUnlocked
    val progress = trophy.progressRatio

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500),
        label = "trophy_grid_progress"
    )

    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .clickable { onClick() }
            .testTag("trophy_badge_${trophy.id}"),
        contentAlignment = Alignment.Center
    ) {
        // Anel Exterior de Carregamento / Progresso
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
            strokeWidth = 3.dp
        )

        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxSize(),
            color = if (isUnlocked) tier.glowColor else FlowCyanPrimary.copy(alpha = 0.4f),
            strokeWidth = 3.dp
        )

        // Insígnia Circular Metálica Dark Mode
        Surface(
            modifier = Modifier.size(54.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                1.5.dp,
                if (isUnlocked) tier.primaryColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            ),
            shadowElevation = if (isUnlocked) 4.dp else 0.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isUnlocked) {
                            Brush.radialGradient(
                                listOf(
                                    tier.primaryColor.copy(alpha = 0.28f),
                                    tier.secondaryColor.copy(alpha = 0.45f),
                                    Color(0xFF0F172A)
                                )
                            )
                        } else {
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFF1E293B),
                                    Color(0xFF0F172A)
                                )
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = trophy.icon,
                    contentDescription = trophy.title,
                    // Opacidade a 30% em cinzento apagado se bloqueada
                    tint = if (isUnlocked) tier.primaryColor else Color.White.copy(alpha = 0.30f),
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

/**
 * D. Ecrã de Detalhe Flutuante (O Toque na Medalha)
 */
@Composable
private fun TrophyDetailDialog(
    trophy: TrophyItem,
    onDismiss: () -> Unit
) {
    val tier = trophy.currentTier
    val progress = trophy.progressRatio

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("trophy_detail_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.5.dp, tier.glowColor.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Grande Insígnia com Anel de Progresso Exterior
                Box(
                    modifier = Modifier.size(96.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        strokeWidth = 5.dp
                    )
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        color = if (trophy.isUnlocked) tier.glowColor else FlowCyanPrimary.copy(alpha = 0.4f),
                        strokeWidth = 5.dp
                    )

                    Surface(
                        modifier = Modifier.size(76.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(2.dp, if (trophy.isUnlocked) tier.primaryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        shadowElevation = if (trophy.isUnlocked) 6.dp else 1.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    if (trophy.isUnlocked) {
                                        Brush.radialGradient(
                                            listOf(
                                                tier.primaryColor.copy(alpha = 0.35f),
                                                tier.secondaryColor.copy(alpha = 0.6f),
                                                Color(0xFF0F172A)
                                            )
                                        )
                                    } else {
                                        Brush.radialGradient(
                                            listOf(
                                                Color(0xFF1E293B),
                                                Color(0xFF0F172A)
                                            )
                                        )
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = trophy.icon,
                                contentDescription = null,
                                tint = if (trophy.isUnlocked) tier.primaryColor else Color.White.copy(alpha = 0.30f),
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }
                }

                // Badge de Material Atual (Bronze, Prata, Ouro, Platina ou Bloqueado)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (trophy.isUnlocked) tier.primaryColor.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, if (trophy.isUnlocked) tier.primaryColor.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (trophy.isUnlocked) tier.primaryColor else Color.Gray)
                        )
                        Text(
                            text = if (trophy.isUnlocked) "Nível ${tier.label}" else "Nível Zero • Bloqueado",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (trophy.isUnlocked) tier.primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Nome da Conquista
                Text(
                    text = trophy.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                // Descrição Humanizada (sem jargão)
                Text(
                    text = trophy.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // Progresso Numérico Exato
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                                text = "Progresso Atual",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${trophy.currentProgress} / ${trophy.targetProgress} ${trophy.unitLabel}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = tier.primaryColor
                            )
                        }

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = tier.primaryColor,
                            trackColor = MaterialTheme.colorScheme.surface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${(progress * 100).toInt()}% concluído",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "+${trophy.nextXpBonus} XP no próximo nível",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = FlowAmberLight
                            )
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = FlowCyanPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fechar")
                }
            }
        }
    }
}

/**
 * C. Painel Oculto do Avaliador / Banca Acadêmica (Definições)
 */
@Composable
private fun EvaluatorSettingsDialog(
    viewModel: GutFlowViewModel,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.School, contentDescription = null, tint = FlowCyanPrimary)
                Text(
                    text = "Painel do Avaliador (Banca)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Fundamentação Teórica da Matriz GUT",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = FlowCyanPrimary
                )
                Text(
                    text = "O GUTFlow adapta a matriz original de Charles H. Kepner e Benjamin B. Tregoe (1981) para produtividade pessoal, substituindo o jargão por 3 perguntas naturais e ocultando o cálculo G × U × T sob um termômetro reativo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider()

                Text(
                    text = "KPIs de Validação do MVP",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = FlowCyanPrimary
                )
                Text(
                    text = "• Taxa de adesão à recomendação da Matriz: ≥ 70%\n• Redução de tempo e sobrecarga cognitiva de priorização: 30%\n• Gamificação contemporânea não-punitiva para ancoragem de rotina.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider()

                Text(
                    text = "Demonstração para a Banca",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = FlowCyanPrimary
                )
                OutlinedButton(
                    onClick = {
                        viewModel.resetDemoData()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Recarregar Tarefas de Teste")
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Concluir")
            }
        }
    )
}

/**
 * Helper para calcular dinamicamente o material tier e progresso de cada troféu
 */
private fun calculateTrophy(
    id: String,
    title: String,
    description: String,
    icon: ImageVector,
    progress: Int,
    thresholds: List<Int>,
    unit: String,
    xpBase: Int
): TrophyItem {
    val isUnlocked = progress >= thresholds[0]

    val tier = when {
        progress >= thresholds[3] -> TrophyMaterial.PLATINA
        progress >= thresholds[2] -> TrophyMaterial.OURO
        progress >= thresholds[1] -> TrophyMaterial.PRATA
        else -> TrophyMaterial.BRONZE
    }

    val target = when {
        !isUnlocked -> thresholds[0]
        tier == TrophyMaterial.BRONZE -> thresholds[1]
        tier == TrophyMaterial.PRATA -> thresholds[2]
        tier == TrophyMaterial.OURO -> thresholds[3]
        else -> thresholds[3]
    }

    val nextXp = when (tier) {
        TrophyMaterial.BRONZE -> xpBase
        TrophyMaterial.PRATA -> xpBase + 30
        TrophyMaterial.OURO -> xpBase + 60
        TrophyMaterial.PLATINA -> xpBase + 100
    }

    return TrophyItem(
        id = id,
        title = title,
        description = description,
        icon = icon,
        currentTier = tier,
        currentProgress = progress,
        targetProgress = target,
        unitLabel = unit,
        nextXpBonus = nextXp,
        isUnlocked = isUnlocked
    )
}

@Composable
private fun ProfileMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            // Fonte ExtraBold e cor primária azul para destaque máximo no ecrã escuro
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                ),
                color = FlowCyanPrimary
            )
            Text(text = title, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
