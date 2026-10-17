package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PriorityLevel
import com.example.ui.theme.FlowCrimsonCritical
import com.example.ui.theme.FlowCyanPrimary
import com.example.ui.theme.FlowEmeraldLow
import com.example.ui.theme.FlowOrangeHigh
import com.example.ui.theme.FlowYellowMedium
import com.example.ui.viewmodel.GutFlowViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickGutScreen(
    viewModel: GutFlowViewModel,
    onTaskCreated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Trabalho") }

    var gravity by remember { mutableIntStateOf(3) }
    var urgency by remember { mutableIntStateOf(3) }
    var trend by remember { mutableIntStateOf(3) }

    val gutScore = gravity * urgency * trend

    val priorityLevel = when {
        gutScore >= 100 -> PriorityLevel.CRITICA
        gutScore >= 60 -> PriorityLevel.ALTA
        gutScore >= 25 -> PriorityLevel.MEDIA
        else -> PriorityLevel.BAIXA
    }

    val animatedColor by animateColorAsState(
        targetValue = Color(priorityLevel.colorHex),
        animationSpec = tween(durationMillis = 350),
        label = "thermometer_color"
    )

    val categories = listOf("Trabalho", "Estudo", "Saúde", "Finanças", "Pessoal", "Urgente")

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("quick_gut_screen")
    ) {
        // Main scrollable form
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Nova Tarefa",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Descreva a missão e ajuste os critérios",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 1. PRIORIZAR A AÇÃO: Descrever a Tarefa no topo absoluto (sem fórmulas matemáticas)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("O que precisa ser feito?") },
                            placeholder = { Text("Ex: Finalizar proposta comercial") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_task_title"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FlowCyanPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Anotações ou contexto (opcional)") },
                            placeholder = { Text("Ex: Apresentação para a diretoria na sexta") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_task_description"),
                            maxLines = 2,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FlowCyanPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                        )

                        Text(
                            text = "Categoria",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            categories.forEach { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat, fontSize = 12.sp) },
                                    leadingIcon = if (selectedCategory == cat) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = FlowCyanPrimary.copy(alpha = 0.18f),
                                        selectedLabelColor = FlowCyanPrimary
                                    ),
                                    modifier = Modifier.testTag("chip_category_$cat")
                                )
                            }
                        }
                    }
                }
            }

            // B. CRITÉRIOS GUT: Sliders com Perguntas Humanizadas e Feedback de Texto Dinâmico
            // 1. Gravidade
            item {
                GutSliderCard(
                    letter = "G",
                    question = "Qual o impacto ou prejuízo se isso NÃO for feito?",
                    value = gravity,
                    onValueChange = { gravity = it },
                    accentColor = FlowCrimsonCritical,
                    dynamicTitle = when (gravity) {
                        1 -> "Sem impacto relevante"
                        2 -> "Pouco prejudicial"
                        3 -> "Moderadamente prejudicial"
                        4 -> "Muito prejudicial"
                        else -> "Catastrófico / Crítico"
                    },
                    dynamicSubtitle = when (gravity) {
                        1 -> "Quase imperceptível no dia a dia"
                        2 -> "Pequeno incômodo perfeitamente contornável"
                        3 -> "Impacto perceptível nos resultados da rotina"
                        4 -> "Danos severos e bloqueios significativos"
                        else -> "Prejuízo devastador ou irreparável se falhar"
                    },
                    testTagPrefix = "gravity"
                )
            }

            // 2. Urgência
            item {
                GutSliderCard(
                    letter = "U",
                    question = "Quando isso precisa ser resolvido?",
                    value = urgency,
                    onValueChange = { urgency = it },
                    accentColor = FlowOrangeHigh,
                    dynamicTitle = when (urgency) {
                        1 -> "Pode esperar bastante"
                        2 -> "Não é urgente"
                        3 -> "O mais cedo possível"
                        4 -> "Urgente"
                        else -> "Ação imediata!"
                    },
                    dynamicSubtitle = when (urgency) {
                        1 -> "Sem qualquer pressa ou pressão temporal"
                        2 -> "Prazo folgado, dá para planejar com calma"
                        3 -> "Resolver nos próximos dias sem correria"
                        4 -> "Prazo curto (precisa de atenção hoje ou amanhã)"
                        else -> "Emergência crítica. Cada hora de atraso pesa!"
                    },
                    testTagPrefix = "urgency"
                )
            }

            // 3. Tendência
            item {
                GutSliderCard(
                    letter = "T",
                    question = "O que acontece com o problema se deixar para depois?",
                    value = trend,
                    onValueChange = { trend = it },
                    accentColor = FlowCyanPrimary,
                    dynamicTitle = when (trend) {
                        1 -> "Não vai mudar"
                        2 -> "Vai piorar devagar"
                        3 -> "Vai piorar gradualmente"
                        4 -> "Vai piorar rápido"
                        else -> "Vai explodir instantaneamente"
                    },
                    dynamicSubtitle = when (trend) {
                        1 -> "Estável, estático e inalterado mesmo sem ação"
                        2 -> "Evolução negativa muito lenta no longo prazo"
                        3 -> "Deterioração em ritmo constante e moderado"
                        4 -> "O problema escala e multiplica o trabalho logo"
                        else -> "Crise descontrolada e danos imediatos"
                    },
                    testTagPrefix = "trend"
                )
            }
        }

        // C. STICKY FOOTER (Fixo no fundo, com Termômetro de Prioridade e Botão de Salvar)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Termômetro de Prioridade Dinâmico (Verde -> Amarelo -> Laranja -> Vermelho)
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
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(animatedColor)
                        )
                        Text(
                            text = "Prioridade: ${priorityLevel.tagText}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = animatedColor
                        )
                    }

                    // Mini barra indicadora do termômetro
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val stages = listOf(
                            PriorityLevel.BAIXA,
                            PriorityLevel.MEDIA,
                            PriorityLevel.ALTA,
                            PriorityLevel.CRITICA
                        )
                        stages.forEach { stage ->
                            val isActive = priorityLevel.ordinal >= stage.ordinal
                            Box(
                                modifier = Modifier
                                    .width(18.dp)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        if (isActive) Color(stage.colorHex) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                    )
                            )
                        }
                    }
                }

                // Botão de Salvar (Sempre Visível sem precisar fazer scroll)
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.addNewTask(
                                title = title,
                                description = description,
                                category = selectedCategory,
                                gravity = gravity,
                                urgency = urgency,
                                trend = trend
                            )
                            onTaskCreated()
                        }
                    },
                    enabled = title.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = FlowCyanPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_save_gut_task")
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Priorizar & Salvar Tarefa",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GutSliderCard(
    letter: String,
    question: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    accentColor: Color,
    dynamicTitle: String,
    dynamicSubtitle: String,
    testTagPrefix: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Pergunta Humanizada com identificador visual sutil
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, accentColor),
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = letter,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = accentColor
                        )
                    }
                }
                Text(
                    text = question,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            // Slider horizontal ergonômico (Touch target >= 44x44px, track espesso de 10dp e preenchimento de cor à esquerda)
            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt().coerceIn(1, 5)) },
                valueRange = 1f..5f,
                steps = 3,
                colors = SliderDefaults.colors(
                    thumbColor = accentColor,
                    activeTrackColor = accentColor,
                    inactiveTrackColor = Color(0xFF334155).copy(alpha = 0.5f),
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent
                ),
                thumb = {
                    // Área de toque ergonômica de pelo menos 48x48dp para uso com uma mão
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("thumb_$testTagPrefix"),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(24.dp),
                            shape = CircleShape,
                            color = accentColor,
                            border = BorderStroke(2.dp, Color.White),
                            shadowElevation = 4.dp
                        ) {}
                    }
                },
                track = { sliderState ->
                    SliderDefaults.Track(
                        sliderState = sliderState,
                        colors = SliderDefaults.colors(
                            activeTrackColor = accentColor,
                            inactiveTrackColor = Color(0xFF334155).copy(alpha = 0.45f)
                        ),
                        modifier = Modifier.height(10.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("slider_$testTagPrefix")
            )

            // Feedback de Texto Dinâmico: Apenas o texto da posição selecionada
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Ponto indicador de intensidade
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )

                    Column {
                        Text(
                            text = dynamicTitle,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = accentColor
                        )
                        Text(
                            text = dynamicSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
