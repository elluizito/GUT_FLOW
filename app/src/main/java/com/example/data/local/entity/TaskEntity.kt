package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "Geral",
    val gravity: Int, // 1 a 5
    val urgency: Int, // 1 a 5
    val trend: Int,   // 1 a 5
    val gutScore: Int = gravity * urgency * trend, // 1 a 125
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val xpReward: Int = (gravity * urgency * trend) + 20
) {
    val priorityLevel: PriorityLevel
        get() = when {
            gutScore >= 100 -> PriorityLevel.CRITICA
            gutScore >= 60 -> PriorityLevel.ALTA
            gutScore >= 25 -> PriorityLevel.MEDIA
            else -> PriorityLevel.BAIXA
        }
}

enum class PriorityLevel(
    val label: String,
    val colorHex: Long,
    val tagText: String,
    val actionAdvice: String
) {
    CRITICA(
        label = "Crítica / Ação Imediata",
        colorHex = 0xFFEF4444, // Red
        tagText = "Crítica",
        actionAdvice = "Parar tudo e resolver agora. O impacto e a velocidade de piora são devastadores se adiada."
    ),
    ALTA(
        label = "Alta Prioridade",
        colorHex = 0xFFF97316, // Orange
        tagText = "Alta",
        actionAdvice = "Planejar para resolver hoje. A tendência indica agravamento significativo em curto prazo."
    ),
    MEDIA(
        label = "Média Prioridade",
        colorHex = 0xFFF59E0B, // Amber
        tagText = "Média",
        actionAdvice = "Agendar para esta semana. O impacto existe mas há margem controlada de manobra."
    ),
    BAIXA(
        label = "Baixa Prioridade",
        colorHex = 0xFF10B981, // Emerald Green
        tagText = "Baixa",
        actionAdvice = "Resolver quando as prioridades críticas estiverem limpas ou delegar se possível."
    )
}
