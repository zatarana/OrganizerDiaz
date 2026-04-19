package com.lifeflowpro.app.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeflowpro.app.alarm.TaskAlarmScheduler
import com.lifeflowpro.app.data.db.entities.TaskEntity
import com.lifeflowpro.app.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val alarmScheduler: TaskAlarmScheduler
) : ViewModel() {

    val tasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTask(task: TaskEntity) {
        viewModelScope.launch {
            val id = repository.insert(task)
            alarmScheduler.schedule(task.copy(id = id))
        }
    }

    fun completeTask(task: TaskEntity) {
        viewModelScope.launch {
            val updatedTask = task.copy(status = "CONCLUIDA")
            repository.update(updatedTask)
            alarmScheduler.cancel(task)
            
            // Handle recurrence logic here if applicable
            if (task.recurrence_type != "NENHUMA") {
                createNextRecurrence(task)
            }
        }
    }

    private suspend fun createNextRecurrence(task: TaskEntity) {
        val nextDate = calculateNextDate(task.due_date, task.recurrence_type)
        if (nextDate != null) {
            val nextTask = task.copy(
                id = 0,
                due_date = nextDate,
                status = "PENDENTE"
            )
            val nextId = repository.insert(nextTask)
            alarmScheduler.schedule(nextTask.copy(id = nextId))
        }
    }

    private fun calculateNextDate(currentDate: Long?, type: String): Long? {
        if (currentDate == null) return null
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = currentDate
        
        when (type) {
            "DIARIA" -> calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
            "SEMANAL" -> calendar.add(java.util.Calendar.WEEK_OF_YEAR, 1)
            "MENSAL" -> calendar.add(java.util.Calendar.MONTH, 1)
            else -> return null
        }
        return calendar.timeInMillis
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.delete(task)
            alarmScheduler.cancel(task)
        }
    }
}
