package com.lifeflowpro.app.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeflowpro.app.data.db.dao.DebtDao
import com.lifeflowpro.app.data.db.dao.TaskDao
import com.lifeflowpro.app.data.db.dao.TransactionDao
import com.lifeflowpro.app.data.db.entities.DebtInstallmentEntity
import com.lifeflowpro.app.data.db.entities.TaskEntity
import com.lifeflowpro.app.data.db.entities.TransactionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import androidx.compose.ui.graphics.Color
import javax.inject.Inject

enum class EventType(val color: Color) {
    TASK(Color(0xFF3B82F6)), // Blue 🔵
    EXPENSE(Color(0xFFF97316)), // Orange 🟠
    DEBT_INSTALLMENT(Color(0xFFA855F7)), // Purple 🟣
    INCOME(Color(0xFF10B981)) // Green 🟢
}

data class CalendarEvent(
    val id: String,
    val title: String,
    val date: Long,
    val type: EventType,
    val value: Double? = null,
    val originalTx: TransactionEntity? = null,
    val originalTask: TaskEntity? = null,
    val originalInstallment: DebtInstallmentEntity? = null
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val transactionDao: TransactionDao,
    private val debtDao: DebtDao
) : ViewModel() {

    val events: StateFlow<List<CalendarEvent>> = combine(
        taskDao.getAllTasks(),
        transactionDao.getAllTransactions(),
        debtDao.getAllInstallments()
    ) { tasks, transactions, installments ->
        val eventList = mutableListOf<CalendarEvent>()

        tasks.forEach { task ->
            if (task.due_date != null) {
                eventList.add(
                    CalendarEvent(
                        id = "task_${task.id}",
                        title = task.title,
                        date = task.due_date,
                        type = EventType.TASK,
                        originalTask = task
                    )
                )
            }
        }

        transactions.forEach { tx ->
            eventList.add(
                CalendarEvent(
                    id = "tx_${tx.id}",
                    title = tx.description ?: if (tx.type == "INCOME") "Recebimento" else "Pagamento",
                    date = tx.expected_date,
                    type = if (tx.type == "INCOME") EventType.INCOME else EventType.EXPENSE,
                    value = tx.expected_value,
                    originalTx = tx
                )
            )
        }

        installments.forEach { inst ->
            eventList.add(
                CalendarEvent(
                    id = "inst_${inst.id}",
                    title = "Parcela ${inst.installment_number}",
                    date = inst.due_date,
                    type = EventType.DEBT_INSTALLMENT,
                    value = inst.expected_value,
                    originalInstallment = inst
                )
            )
        }

        eventList
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
