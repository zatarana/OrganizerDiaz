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

import com.lifeflowpro.app.data.repository.GamificationRepository
import com.lifeflowpro.app.data.repository.FinanceRepository
import com.lifeflowpro.app.data.db.entities.TransactionEntity

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val gamificationRepository: GamificationRepository,
    private val financeRepository: FinanceRepository,
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
            
            // Update Gamification
            gamificationRepository.processTaskCompletion()
            
            // Handle recurrence logic here if applicable
            if (task.recurrence_type != "NENHUMA") {
                createNextRecurrence(task)
            }
        }
    }

    fun createLinkedTransaction(task: TaskEntity, value: Double, type: String) {
        viewModelScope.launch {
            val tx = TransactionEntity(
                type = type, // "EXPENSE" or "INCOME"
                account_id = 1, // Default main account for now
                category_id = task.category_id,
                description = task.title,
                expected_value = value,
                final_value = value, // Since it's done together with task
                expected_date = System.currentTimeMillis(),
                payment_date = System.currentTimeMillis(),
                status = if (type == "INCOME") "RECEBIDO" else "PAGO", // Auto-pays since task is completed
                recurrence_type = "NENHUMA",
                recurrence_group_id = null
            )
            val txId = financeRepository.insertTransaction(tx)
            // Update the task to reflect it's linked
            repository.update(task.copy(linked_transaction_id = txId))
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
