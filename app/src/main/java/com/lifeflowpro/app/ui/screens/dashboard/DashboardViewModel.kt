package com.lifeflowpro.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeflowpro.app.data.db.entities.AccountEntity
import com.lifeflowpro.app.data.db.entities.DebtEntity
import com.lifeflowpro.app.data.db.entities.TaskEntity
import com.lifeflowpro.app.data.db.entities.TransactionEntity
import com.lifeflowpro.app.data.repository.DebtRepository
import com.lifeflowpro.app.data.repository.FinanceRepository
import com.lifeflowpro.app.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class DashboardState(
    val upcomingTasks: List<TaskEntity> = emptyList(),
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val criticalDebts: List<DebtEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val financeRepository: FinanceRepository,
    private val debtRepository: DebtRepository
) : ViewModel() {

    val state: StateFlow<DashboardState> = combine(
        taskRepository.allTasks,
        financeRepository.allTransactions,
        financeRepository.allAccounts,
        debtRepository.allDebts
    ) { tasks, transactions, accounts, debts ->
        DashboardState(
            upcomingTasks = tasks.filter { it.status == "PENDENTE" }.take(3),
            recentTransactions = transactions.take(5),
            accounts = accounts,
            criticalDebts = debts.filter { it.status == "EM_ABERTO" }.take(2),
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardState())
}
