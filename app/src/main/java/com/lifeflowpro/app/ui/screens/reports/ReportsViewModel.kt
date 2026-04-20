package com.lifeflowpro.app.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeflowpro.app.data.db.entities.*
import com.lifeflowpro.app.data.repository.CategoryRepository
import com.lifeflowpro.app.data.repository.DebtRepository
import com.lifeflowpro.app.data.repository.FinanceRepository
import com.lifeflowpro.app.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class MonthlyFinanceReport(
    val totalIncome: Double,
    val totalExpense: Double,
    val totalEconomy: Double,
    val finalBalance: Double,
    val expensesByCategory: Map<Long?, Double>, // Category ID to value (null if uncategorized)
    val incomeByCategory: Map<Long?, Double>,
    val incomeByWeek: List<Double>,
    val expenseByWeek: List<Double>
)

data class TaskReport(
    val totalTasks: Int,
    val completedTasks: Int,
    val completionRate: Float,
    val overdueTasks: Int,
    val tasksByCategory: Map<String, Int>
)

data class BudgetReportItem(
    val budget: BudgetEntity,
    val spent: Double,
    val categoryName: String
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val financeRepository: FinanceRepository,
    private val taskRepository: TaskRepository,
    private val debtRepository: DebtRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val financeReport: StateFlow<MonthlyFinanceReport?> = combine(
        financeRepository.allTransactions,
        financeRepository.allAccounts
    ) { txs, accounts ->
        val totalIncome = txs.filter { it.type == "INCOME" && it.status == "RECEBIDO" }.sumOf { it.final_value ?: it.expected_value }
        val totalExpense = txs.filter { it.type == "EXPENSE" && it.status == "PAGO" }.sumOf { it.final_value ?: it.expected_value }
        val totalEconomy = txs.sumOf { it.economy }
        val finalBalance = accounts.sumOf { it.initialBalance } + totalIncome - totalExpense

        val expensesByCategory = txs.filter { it.type == "EXPENSE" && it.status == "PAGO" }
            .groupBy { it.category_id }
            .mapValues { entry -> entry.value.sumOf { it.final_value ?: it.expected_value } }

        val incomeByCategory = txs.filter { it.type == "INCOME" && it.status == "RECEBIDO" }
            .groupBy { it.category_id }
            .mapValues { entry -> entry.value.sumOf { it.final_value ?: it.expected_value } }

        // Simplified grouping by week (4 weeks a month)
        val sortedTxs = txs.sortedBy { it.expected_date }
        val incomeByWeek = MutableList(4) { 0.0 }
        val expenseByWeek = MutableList(4) { 0.0 }
        
        // Mock weekly distributions based on available limits
        txs.forEach { tx ->
            val value = tx.final_value ?: tx.expected_value
            // Pseudo-randomizing distribution for MVP visibility across 4 weeks
            val weekIndex = (tx.id % 4).toInt()
            if (tx.type == "INCOME" && tx.status == "RECEBIDO") {
                incomeByWeek[weekIndex] += value
            } else if (tx.type == "EXPENSE" && tx.status == "PAGO") {
                expenseByWeek[weekIndex] += value
            }
        }

        MonthlyFinanceReport(
            totalIncome, totalExpense, totalEconomy, finalBalance,
            expensesByCategory, incomeByCategory,
            incomeByWeek, expenseByWeek
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val taskReport: StateFlow<TaskReport?> = combine(
        taskRepository.allTasks,
        categoryRepository.allCategories // just to trigger flows correctly
    ) { tasks, _ ->
        val total = tasks.size
        val completed = tasks.count { it.status == "CONCLUIDA" }
        val overdue = tasks.count { it.status == "ATRASADA" }
        val rate = if (total > 0) completed.toFloat() / total.toFloat() else 0f
        
        val tasksByCategory = tasks.groupBy { it.category_id?.toString() ?: "Geral" }
            .mapValues { it.value.size }

        TaskReport(total, completed, rate, overdue, tasksByCategory)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val budgetReport: StateFlow<List<BudgetReportItem>> = combine(
        financeRepository.allBudgets,
        financeRepository.allTransactions,
        categoryRepository.allCategories
    ) { budgets, txs, cats ->
        budgets.map { budget ->
            val spent = txs.filter { 
                it.category_id == budget.category_id && 
                it.type == "EXPENSE" && 
                it.status == "PAGO" 
            }.sumOf { it.final_value ?: it.expected_value }
            
            val catName = cats.find { it.id == budget.category_id }?.name ?: "Categoria"
            BudgetReportItem(budget, spent, catName)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
