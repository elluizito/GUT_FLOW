package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val xp: Int = 0,
    val level: Int = 1,
    val currentStreak: Int = 1,
    val lastActiveDate: String = "",
    val totalFocusMinutes: Int = 0,
    val completedTasksCount: Int = 0,
    val evaluatedTasksCount: Int = 0,
    val criticalTasksSolved: Int = 0
) {
    val levelTitle: String
        get() = when (level) {
            1 -> "Iniciante no Foco"
            2 -> "Estrategista de Ação"
            3 -> "Mestre da Prioridade"
            4 -> "Comandante do Fluxo"
            5 -> "Oráculo da Decisão"
            else -> "Lenda da Produtividade (Nv. $level)"
        }

    val xpForCurrentLevel: Int
        get() = (level - 1) * 200

    val xpForNextLevel: Int
        get() = level * 200

    val currentLevelProgress: Float
        get() {
            val needed = xpForNextLevel - xpForCurrentLevel
            if (needed <= 0) return 1f
            val currentInLevel = (xp - xpForCurrentLevel).coerceIn(0, needed)
            return currentInLevel.toFloat() / needed.toFloat()
        }
}

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val xpBonus: Int = 50,
    val academicRationale: String = ""
)
