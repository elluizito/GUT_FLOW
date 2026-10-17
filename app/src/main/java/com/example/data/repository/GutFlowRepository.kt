package com.example.data.repository

import com.example.data.local.dao.AchievementDao
import com.example.data.local.dao.TaskDao
import com.example.data.local.dao.UserProfileDao
import com.example.data.local.entity.AchievementEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class GutFlowRepository(
    private val taskDao: TaskDao,
    private val userProfileDao: UserProfileDao,
    private val achievementDao: AchievementDao
) {
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val pendingTasks: Flow<List<TaskEntity>> = taskDao.getPendingTasksOrdered()
    val completedTasks: Flow<List<TaskEntity>> = taskDao.getCompletedTasks()
    val topPendingTask: Flow<TaskEntity?> = taskDao.getTopPendingTask()
    val userProfile: Flow<UserProfileEntity?> = userProfileDao.getProfile()
    val achievements: Flow<List<AchievementEntity>> = achievementDao.getAllAchievements()

    suspend fun insertTask(
        title: String,
        description: String,
        category: String,
        gravity: Int,
        urgency: Int,
        trend: Int
    ): Long {
        val score = gravity * urgency * trend
        val xp = score + 20
        val task = TaskEntity(
            title = title,
            description = description,
            category = category,
            gravity = gravity,
            urgency = urgency,
            trend = trend,
            gutScore = score,
            xpReward = xp
        )
        val id = taskDao.insertTask(task)

        // Update profile evaluated task count
        val currentProfile = userProfileDao.getProfileDirect() ?: UserProfileEntity()
        val newEvaluated = currentProfile.evaluatedTasksCount + 1
        userProfileDao.insertOrUpdate(
            currentProfile.copy(evaluatedTasksCount = newEvaluated)
        )

        // Unlock first_task achievement
        achievementDao.unlock("first_task", System.currentTimeMillis())

        if (newEvaluated >= 5) {
            achievementDao.unlock("matrix_strategist", System.currentTimeMillis())
        }

        return id
    }

    suspend fun updateTaskGUT(
        task: TaskEntity,
        newGravity: Int,
        newUrgency: Int,
        newTrend: Int
    ) {
        val newScore = newGravity * newUrgency * newTrend
        val newXp = newScore + 20
        val updated = task.copy(
            gravity = newGravity,
            urgency = newUrgency,
            trend = newTrend,
            gutScore = newScore,
            xpReward = newXp
        )
        taskDao.updateTask(updated)
    }

    suspend fun completeTask(task: TaskEntity): Int {
        if (task.isCompleted) return 0
        val now = System.currentTimeMillis()
        val updated = task.copy(
            isCompleted = true,
            completedAt = now
        )
        taskDao.updateTask(updated)

        val profile = userProfileDao.getProfileDirect() ?: UserProfileEntity()
        val earnedXp = task.xpReward
        val newTotalXp = profile.xp + earnedXp
        val newCompletedCount = profile.completedTasksCount + 1
        val newCriticalSolved = if (task.gutScore >= 100) profile.criticalTasksSolved + 1 else profile.criticalTasksSolved

        // Calculate Level: each 200 XP is a level
        val newLevel = (newTotalXp / 200) + 1

        // Check Streak
        val todayStr = LocalDate.now().toString()
        val updatedStreak = if (profile.lastActiveDate == todayStr) {
            profile.currentStreak
        } else {
            profile.currentStreak + 1
        }

        userProfileDao.insertOrUpdate(
            profile.copy(
                xp = newTotalXp,
                level = newLevel,
                completedTasksCount = newCompletedCount,
                criticalTasksSolved = newCriticalSolved,
                currentStreak = updatedStreak,
                lastActiveDate = todayStr
            )
        )

        // Achievements check
        if (task.gutScore >= 100) {
            achievementDao.unlock("crisis_tamer", now)
        }
        if (newCompletedCount >= 10) {
            achievementDao.unlock("productivity_titan", now)
        }
        if (updatedStreak >= 3) {
            achievementDao.unlock("streak_champion", now)
        }

        return earnedXp
    }

    suspend fun deleteTask(taskId: Long) {
        taskDao.deleteTaskById(taskId)
    }

    suspend fun completeFocusSession(minutes: Int): Int {
        val earnedXp = minutes * 2
        val profile = userProfileDao.getProfileDirect() ?: UserProfileEntity()
        val newFocusMins = profile.totalFocusMinutes + minutes
        val newTotalXp = profile.xp + earnedXp
        val newLevel = (newTotalXp / 200) + 1

        val todayStr = LocalDate.now().toString()
        val updatedStreak = if (profile.lastActiveDate == todayStr) {
            profile.currentStreak
        } else {
            profile.currentStreak + 1
        }

        userProfileDao.insertOrUpdate(
            profile.copy(
                xp = newTotalXp,
                level = newLevel,
                totalFocusMinutes = newFocusMins,
                currentStreak = updatedStreak,
                lastActiveDate = todayStr
            )
        )

        achievementDao.unlock("focus_master", System.currentTimeMillis())
        return earnedXp
    }

    suspend fun resetDemoData() {
        val initialTasks = listOf(
            TaskEntity(
                title = "Corrigir falha crítica no pagamento do cliente",
                description = "Gargalo bloqueando novas transações da plataforma.",
                category = "Trabalho",
                gravity = 5,
                urgency = 5,
                trend = 4,
                gutScore = 100,
                isCompleted = false,
                xpReward = 120
            ),
            TaskEntity(
                title = "Entregar relatório acadêmico de métricas (KPIs)",
                description = "Apresentação da taxa de adesão GUT (≥70%) para o comitê.",
                category = "Estudo",
                gravity = 4,
                urgency = 4,
                trend = 4,
                gutScore = 64,
                isCompleted = false,
                xpReward = 84
            ),
            TaskEntity(
                title = "Revisar backup do banco de dados e rotinas de log",
                description = "Verificar integridade semanal dos dados.",
                category = "Trabalho",
                gravity = 4,
                urgency = 3,
                trend = 3,
                gutScore = 36,
                isCompleted = false,
                xpReward = 56
            ),
            TaskEntity(
                title = "Comprar novo adaptador de monitor para estação de estudo",
                description = "Cabo HDMI reserva para apresentação.",
                category = "Pessoal",
                gravity = 2,
                urgency = 2,
                trend = 2,
                gutScore = 8,
                isCompleted = false,
                xpReward = 28
            )
        )
        taskDao.insertAll(initialTasks)
    }
}
