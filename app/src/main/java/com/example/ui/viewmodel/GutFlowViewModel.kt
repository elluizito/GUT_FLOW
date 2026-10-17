package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AchievementEntity
import com.example.data.local.entity.PriorityLevel
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.repository.GutFlowRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class PomodoroMode(val label: String, val defaultMinutes: Int) {
    FOCO("Foco Intenso", 25),
    PAUSA_CURTA("Pausa Curta", 5),
    PAUSA_LONGA("Pausa Longa", 15)
}

data class PomodoroState(
    val mode: PomodoroMode = PomodoroMode.FOCO,
    val totalSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val sessionsCompleted: Int = 0,
    val linkedTaskId: Long? = null
) {
    val progress: Float
        get() = if (totalSeconds > 0) 1f - (remainingSeconds.toFloat() / totalSeconds.toFloat()) else 0f

    val formattedTime: String
        get() {
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }
}

data class XpCelebrationEvent(
    val xpEarned: Int,
    val taskTitle: String,
    val isLevelUp: Boolean = false,
    val newLevel: Int = 1
)

class GutFlowViewModel(
    application: Application,
    private val repository: GutFlowRepository
) : AndroidViewModel(application) {

    val userProfile: StateFlow<UserProfileEntity> = repository.userProfile
        .combine(MutableStateFlow(Unit)) { profile, _ ->
            profile ?: UserProfileEntity()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfileEntity()
        )

    val pendingTasks: StateFlow<List<TaskEntity>> = repository.pendingTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val completedTasks: StateFlow<List<TaskEntity>> = repository.completedTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val topTask: StateFlow<TaskEntity?> = repository.topPendingTask
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val achievements: StateFlow<List<AchievementEntity>> = repository.achievements
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filters for Priority List Screen
    private val _selectedFilter = MutableStateFlow<PriorityFilter>(PriorityFilter.CRITICAS)
    val selectedFilter: StateFlow<PriorityFilter> = _selectedFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filtered tasks combining query & priority
    val filteredTasks: StateFlow<List<TaskEntity>> = combine(
        repository.allTasks,
        _selectedFilter,
        _searchQuery
    ) { all, filter, query ->
        all.filter { task ->
            val matchesQuery = query.isBlank() ||
                    task.title.contains(query, ignoreCase = true) ||
                    task.description.contains(query, ignoreCase = true) ||
                    task.category.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                PriorityFilter.TODAS -> !task.isCompleted
                PriorityFilter.CRITICAS -> !task.isCompleted && task.priorityLevel == PriorityLevel.CRITICA
                PriorityFilter.ALTAS -> !task.isCompleted && task.priorityLevel == PriorityLevel.ALTA
                PriorityFilter.MEDIAS -> !task.isCompleted && task.priorityLevel == PriorityLevel.MEDIA
                PriorityFilter.BAIXAS -> !task.isCompleted && task.priorityLevel == PriorityLevel.BAIXA
                PriorityFilter.CONCLUIDAS -> task.isCompleted
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Pomodoro Timer State
    private val _pomodoroState = MutableStateFlow(PomodoroState())
    val pomodoroState: StateFlow<PomodoroState> = _pomodoroState.asStateFlow()
    private var timerJob: Job? = null

    // XP Celebration Event
    private val _celebrationEvent = MutableSharedFlow<XpCelebrationEvent>()
    val celebrationEvent: SharedFlow<XpCelebrationEvent> = _celebrationEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.pendingTasks.collect { pending ->
                val currentFilter = _selectedFilter.value
                val isTierFilter = currentFilter in listOf(
                    PriorityFilter.CRITICAS,
                    PriorityFilter.ALTAS,
                    PriorityFilter.MEDIAS,
                    PriorityFilter.BAIXAS
                )
                if (isTierFilter && pending.isNotEmpty()) {
                    val hasCurrentTier = when (currentFilter) {
                        PriorityFilter.CRITICAS -> pending.any { it.priorityLevel == PriorityLevel.CRITICA }
                        PriorityFilter.ALTAS -> pending.any { it.priorityLevel == PriorityLevel.ALTA }
                        PriorityFilter.MEDIAS -> pending.any { it.priorityLevel == PriorityLevel.MEDIA }
                        PriorityFilter.BAIXAS -> pending.any { it.priorityLevel == PriorityLevel.BAIXA }
                        else -> true
                    }
                    if (!hasCurrentTier) {
                        cascadeToNextTier(currentFilter, pending)
                    }
                }
            }
        }
    }

    private fun cascadeToNextTier(fromFilter: PriorityFilter, pending: List<TaskEntity>) {
        val hasCritical = pending.any { it.priorityLevel == PriorityLevel.CRITICA }
        val hasHigh = pending.any { it.priorityLevel == PriorityLevel.ALTA }
        val hasMedium = pending.any { it.priorityLevel == PriorityLevel.MEDIA }
        val hasLow = pending.any { it.priorityLevel == PriorityLevel.BAIXA }

        val nextFilter = when (fromFilter) {
            PriorityFilter.CRITICAS -> when {
                hasHigh -> PriorityFilter.ALTAS
                hasMedium -> PriorityFilter.MEDIAS
                hasLow -> PriorityFilter.BAIXAS
                else -> PriorityFilter.CRITICAS
            }
            PriorityFilter.ALTAS -> when {
                hasMedium -> PriorityFilter.MEDIAS
                hasLow -> PriorityFilter.BAIXAS
                hasCritical -> PriorityFilter.CRITICAS
                else -> PriorityFilter.ALTAS
            }
            PriorityFilter.MEDIAS -> when {
                hasLow -> PriorityFilter.BAIXAS
                hasCritical -> PriorityFilter.CRITICAS
                hasHigh -> PriorityFilter.ALTAS
                else -> PriorityFilter.MEDIAS
            }
            PriorityFilter.BAIXAS -> when {
                hasCritical -> PriorityFilter.CRITICAS
                hasHigh -> PriorityFilter.ALTAS
                hasMedium -> PriorityFilter.MEDIAS
                else -> PriorityFilter.BAIXAS
            }
            else -> fromFilter
        }
        _selectedFilter.value = nextFilter
    }

    fun autoSelectHighestAvailableTier() {
        val pending = pendingTasks.value
        when {
            pending.any { it.priorityLevel == PriorityLevel.CRITICA } -> _selectedFilter.value = PriorityFilter.CRITICAS
            pending.any { it.priorityLevel == PriorityLevel.ALTA } -> _selectedFilter.value = PriorityFilter.ALTAS
            pending.any { it.priorityLevel == PriorityLevel.MEDIA } -> _selectedFilter.value = PriorityFilter.MEDIAS
            pending.any { it.priorityLevel == PriorityLevel.BAIXA } -> _selectedFilter.value = PriorityFilter.BAIXAS
            else -> _selectedFilter.value = PriorityFilter.TODAS
        }
    }

    fun setFilter(filter: PriorityFilter) {
        _selectedFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addNewTask(
        title: String,
        description: String,
        category: String,
        gravity: Int,
        urgency: Int,
        trend: Int
    ) {
        viewModelScope.launch {
            val score = gravity * urgency * trend
            repository.insertTask(
                title = title.trim(),
                description = description.trim(),
                category = category.ifBlank { "Geral" },
                gravity = gravity.coerceIn(1, 5),
                urgency = urgency.coerceIn(1, 5),
                trend = trend.coerceIn(1, 5)
            )
            // Immediately focus on the tier of the newly added task so user sees it right away
            _selectedFilter.value = when {
                score >= 100 -> PriorityFilter.CRITICAS
                score >= 60 -> PriorityFilter.ALTAS
                score >= 25 -> PriorityFilter.MEDIAS
                else -> PriorityFilter.BAIXAS
            }
        }
    }

    fun updateTaskGUT(task: TaskEntity, gravity: Int, urgency: Int, trend: Int) {
        viewModelScope.launch {
            repository.updateTaskGUT(task, gravity, urgency, trend)
        }
    }

    fun completeTask(task: TaskEntity) {
        viewModelScope.launch {
            val previousLevel = userProfile.value.level
            val xpEarned = repository.completeTask(task)
            if (xpEarned > 0) {
                val newLevel = userProfile.value.level
                _celebrationEvent.emit(
                    XpCelebrationEvent(
                        xpEarned = xpEarned,
                        taskTitle = task.title,
                        isLevelUp = newLevel > previousLevel,
                        newLevel = newLevel
                    )
                )
            }
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
            if (_pomodoroState.value.linkedTaskId == taskId) {
                _pomodoroState.value = _pomodoroState.value.copy(linkedTaskId = null)
            }
        }
    }

    // Pomodoro Actions
    fun linkTaskToPomodoro(taskId: Long) {
        _pomodoroState.value = _pomodoroState.value.copy(linkedTaskId = taskId)
    }

    fun setPomodoroMode(mode: PomodoroMode) {
        pausePomodoro()
        val secs = mode.defaultMinutes * 60
        _pomodoroState.value = _pomodoroState.value.copy(
            mode = mode,
            totalSeconds = secs,
            remainingSeconds = secs,
            isRunning = false
        )
    }

    fun togglePomodoro() {
        if (_pomodoroState.value.isRunning) {
            pausePomodoro()
        } else {
            startPomodoro()
        }
    }

    fun startPomodoro() {
        if (_pomodoroState.value.isRunning) return
        _pomodoroState.value = _pomodoroState.value.copy(isRunning = true)
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_pomodoroState.value.remainingSeconds > 0 && _pomodoroState.value.isRunning) {
                delay(1000L)
                val currentRemaining = _pomodoroState.value.remainingSeconds - 1
                if (currentRemaining <= 0) {
                    onPomodoroCompleted()
                    break
                } else {
                    _pomodoroState.value = _pomodoroState.value.copy(remainingSeconds = currentRemaining)
                }
            }
        }
    }

    fun pausePomodoro() {
        timerJob?.cancel()
        _pomodoroState.value = _pomodoroState.value.copy(isRunning = false)
    }

    fun resetPomodoro() {
        pausePomodoro()
        val secs = _pomodoroState.value.mode.defaultMinutes * 60
        _pomodoroState.value = _pomodoroState.value.copy(
            remainingSeconds = secs,
            isRunning = false
        )
    }

    private suspend fun onPomodoroCompleted() {
        val completedMode = _pomodoroState.value.mode
        val minutesSpent = completedMode.defaultMinutes
        pausePomodoro()

        if (completedMode == PomodoroMode.FOCO) {
            val xp = repository.completeFocusSession(minutesSpent)
            val newSessions = _pomodoroState.value.sessionsCompleted + 1
            _pomodoroState.value = _pomodoroState.value.copy(
                remainingSeconds = 0,
                isRunning = false,
                sessionsCompleted = newSessions
            )
            _celebrationEvent.emit(
                XpCelebrationEvent(
                    xpEarned = xp,
                    taskTitle = "Sessão de Foco Pomodoro ($minutesSpent min)"
                )
            )
            // Suggest short break automatically
            setPomodoroMode(PomodoroMode.PAUSA_CURTA)
        } else {
            setPomodoroMode(PomodoroMode.FOCO)
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.resetDemoData()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

enum class PriorityFilter(val label: String) {
    CRITICAS("Críticas"),
    ALTAS("Altas"),
    MEDIAS("Médias"),
    BAIXAS("Baixas"),
    TODAS("Todas"),
    CONCLUIDAS("Concluídas")
}

class GutFlowViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GutFlowViewModel::class.java)) {
            val database = AppDatabase.getDatabase(application, kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO))
            val repository = GutFlowRepository(
                database.taskDao(),
                database.userProfileDao(),
                database.achievementDao()
            )
            return GutFlowViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
