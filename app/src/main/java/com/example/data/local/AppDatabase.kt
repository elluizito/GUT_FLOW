package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AchievementDao
import com.example.data.local.dao.TaskDao
import com.example.data.local.dao.UserProfileDao
import com.example.data.local.entity.AchievementEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TaskEntity::class,
        UserProfileEntity::class,
        AchievementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gutflow_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(db: AppDatabase) {
            // Seed User Profile
            db.userProfileDao().insertOrUpdate(
                UserProfileEntity(
                    id = 1,
                    xp = 75,
                    level = 1,
                    currentStreak = 2,
                    lastActiveDate = java.time.LocalDate.now().toString(),
                    totalFocusMinutes = 25,
                    completedTasksCount = 1,
                    evaluatedTasksCount = 4,
                    criticalTasksSolved = 0
                )
            )

            // Seed Initial Pre-evaluated Tasks showing real GUT prioritization
            val initialTasks = listOf(
                TaskEntity(
                    id = 1,
                    title = "Corrigir falha crítica no pagamento do cliente",
                    description = "Gargalo bloqueando novas transações da plataforma.",
                    category = "Trabalho",
                    gravity = 5, // Catastrófico
                    urgency = 5, // Ação Imediata
                    trend = 4,   // Piora rápido
                    gutScore = 5 * 5 * 4, // 100 - Crítica!
                    isCompleted = false,
                    xpReward = 120
                ),
                TaskEntity(
                    id = 2,
                    title = "Entregar relatório acadêmico de métricas (KPIs)",
                    description = "Apresentação da taxa de adesão GUT (≥70%) para o comitê.",
                    category = "Estudo",
                    gravity = 4,
                    urgency = 4,
                    trend = 4,
                    gutScore = 4 * 4 * 4, // 64 - Alta
                    isCompleted = false,
                    xpReward = 84
                ),
                TaskEntity(
                    id = 3,
                    title = "Revisar backup do banco de dados e rotinas de log",
                    description = "Verificar integridade semanal dos dados.",
                    category = "Trabalho",
                    gravity = 4,
                    urgency = 3,
                    trend = 3,
                    gutScore = 4 * 3 * 3, // 36 - Média
                    isCompleted = false,
                    xpReward = 56
                ),
                TaskEntity(
                    id = 4,
                    title = "Comprar novo adaptador de monitor para estação de estudo",
                    description = "Cabo HDMI reserva para apresentação.",
                    category = "Pessoal",
                    gravity = 2,
                    urgency = 2,
                    trend = 2,
                    gutScore = 2 * 2 * 2, // 8 - Baixa
                    isCompleted = false,
                    xpReward = 28
                ),
                TaskEntity(
                    id = 5,
                    title = "Configurar ambiente inicial e repositório Git",
                    description = "Primeira entrega do ciclo de desenvolvimento.",
                    category = "Estudo",
                    gravity = 3,
                    urgency = 4,
                    trend = 3,
                    gutScore = 36,
                    isCompleted = true,
                    completedAt = System.currentTimeMillis() - 3600_000 * 4,
                    xpReward = 56
                )
            )
            db.taskDao().insertAll(initialTasks)

            // Seed Initial Badges / Achievements with academic rationale
            val achievements = listOf(
                AchievementEntity(
                    id = "first_task",
                    title = "Primeiro Disparo",
                    description = "Cadastrou e avaliou a primeira tarefa no método GUT.",
                    iconName = "flag",
                    isUnlocked = true,
                    unlockedAt = System.currentTimeMillis() - 3600_000 * 24,
                    xpBonus = 50,
                    academicRationale = "Reduz o atrito cognitivo inicial de priorização em mais de 30%."
                ),
                AchievementEntity(
                    id = "crisis_tamer",
                    title = "Domador de Crises",
                    description = "Concluiu com sucesso uma tarefa com Score GUT ≥ 100.",
                    iconName = "local_fire_department",
                    isUnlocked = false,
                    xpBonus = 100,
                    academicRationale = "Reforço comportamental positivo para resolução de gargalos de alto risco."
                ),
                AchievementEntity(
                    id = "focus_master",
                    title = "Foco Inabalável",
                    description = "Completou um bloco de Foco Pomodoro sem interrupções.",
                    iconName = "timer",
                    isUnlocked = true,
                    unlockedAt = System.currentTimeMillis() - 3600_000 * 2,
                    xpBonus = 60,
                    academicRationale = "Integração do foco deliberado aumenta taxa de conclusão em 45%."
                ),
                AchievementEntity(
                    id = "matrix_strategist",
                    title = "Mente Estratégica",
                    description = "Avaliou pelo menos 5 tarefas usando perguntas humanizadas.",
                    iconName = "analytics",
                    isUnlocked = false,
                    xpBonus = 80,
                    academicRationale = "Supera o viés de urgência artificial em favor do impacto real."
                ),
                AchievementEntity(
                    id = "streak_champion",
                    title = "Sequência de Foco",
                    description = "Manteve 3 dias seguidos com ao menos 1 tarefa concluída.",
                    iconName = "bolt",
                    isUnlocked = false,
                    xpBonus = 120,
                    academicRationale = "Formação de hábitos por repetição continuada e consistência diária."
                ),
                AchievementEntity(
                    id = "productivity_titan",
                    title = "Titã da Produtividade",
                    description = "Concluiu 10 tarefas priorizadas pelo GUTFlow.",
                    iconName = "military_tech",
                    isUnlocked = false,
                    xpBonus = 150,
                    academicRationale = "Alcance do KPI de adesão de longo prazo (≥ 70%)."
                )
            )
            db.achievementDao().insertAll(achievements)
        }
    }
}
