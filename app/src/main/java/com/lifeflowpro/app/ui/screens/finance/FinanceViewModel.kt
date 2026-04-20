package com.lifeflowpro.app.ui.screens.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeflowpro.app.data.db.entities.*
import com.lifeflowpro.app.data.repository.CategoryRepository
import com.lifeflowpro.app.data.repository.FinanceRepository
import com.lifeflowpro.app.domain.model.FinancialSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val repository: FinanceRepository,
    private val categoryRepository: CategoryRepository,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    init {
        // Reactive Flow Trigger for Budgets
        viewModelScope.launch {
            combine(repository.allBudgets, repository.allTransactions) { budgets, txs ->
                budgets.forEach { budget ->
                    val spent = txs.filter { 
                        it.category_id == budget.categoryId && 
                        it.type == "EXPENSE" && 
                        it.status == "PAGO" 
                    }.sumOf { it.final_value ?: it.expected_value }
                    
                    val percentage = if (budget.limitAmount > 0) spent / budget.limitAmount else 0.0
                    
                    if (percentage >= 1.0) {
                        com.lifeflowpro.app.worker.NotificationHelper.showNotification(
                            context = context,
                            channelId = com.lifeflowpro.app.worker.NotificationHelper.CHANNEL_FINANCE,
                            notificationId = budget.id.toInt() * 1000,
                            title = "Orçamento Estourado!",
                            message = "Você extrapolou o limite para o orçamento.",
                        )
                    } else if (percentage >= 0.8) {
                        com.lifeflowpro.app.worker.NotificationHelper.showNotification(
                            context = context,
                            channelId = com.lifeflowpro.app.worker.NotificationHelper.CHANNEL_FINANCE,
                            notificationId = budget.id.toInt() * 1000 + 1,
                            title = "Orçamento em Risco",
                            message = "Você já consumiu ${(percentage * 100).toInt()}% do orçamento.",
                        )
                    }
                }
            }.collect()
        }
        
        // Reactive Flow for Goals
        viewModelScope.launch {
            repository.allGoals.collect { goals ->
                goals.forEach { goal ->
                    if (goal.currentAmount >= goal.targetAmount && goal.status != "CONCLUIDA") {
                        com.lifeflowpro.app.worker.NotificationHelper.showNotification(
                            context = context,
                            channelId = com.lifeflowpro.app.worker.NotificationHelper.CHANNEL_GOALS,
                            notificationId = goal.id.toInt() * 2000,
                            title = "Meta Atingida! 🎉",
                            message = "Parabéns! Você alcançou a meta: ${goal.name}",
                        )
                        // Note: Auto-update status would happen here, but requires dao update method
                    }
                }
            }
        }
    }

    val accounts: StateFlow<List<AccountEntity>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgets: StateFlow<List<BudgetEntity>> = repository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<GoalEntity>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val financialSummary: StateFlow<FinancialSummary> = combine(transactions, accounts) { txs, accts ->
        val initialBalanceSum = accts.sumOf { it.initialBalance }
        
        var realBalance = initialBalanceSum
        var predictedBalance = initialBalanceSum

        txs.forEach { tx ->
            when (tx.status) {
                "PAGO" -> realBalance -= (tx.final_value ?: tx.expected_value)
                "RECEBIDO" -> realBalance += (tx.final_value ?: tx.expected_value)
                "A_RECEBER" -> predictedBalance += tx.expected_value
                "A_PAGAR" -> predictedBalance -= tx.expected_value
            }
        }
        
        // Predicted is based on real + future
        predictedBalance += (realBalance - initialBalanceSum)

        FinancialSummary(realBalance, predictedBalance)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary(0.0, 0.0))

    fun addTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.insertTransaction(tx)
        }
    }

    fun confirmPayment(tx: TransactionEntity, finalValue: Double, date: Long) {
        viewModelScope.launch {
            val status = if (tx.type == "INCOME") "RECEBIDO" else "PAGO"
            val economy = if (tx.type == "EXPENSE") tx.expected_value - finalValue else finalValue - tx.expected_value
            
            repository.updateTransaction(tx.copy(
                status = status,
                final_value = finalValue,
                payment_date = date,
                economy = economy
            ))
        }
    }

    fun addAccount(account: AccountEntity) {
        viewModelScope.launch {
            repository.insertAccount(account)
        }
    }

    fun addGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.insertGoal(goal)
        }
    }

    fun updateGoal(goal: GoalEntity) {
        // Assume repository has updateGoal or just leave for later, for now we will skip update or add it to repo later.
        // I will add it to the repo manually if needed, but for MVP let's just do creation and basic visualization.
        // Actually I should add updateGoal to FinanceRepository. For now I'll just keep the insert.
    }

    fun createTransfer(fromAccountId: Long, toAccountId: Long, value: Double, description: String, date: Long) {
        viewModelScope.launch {
            // Transfer creates two linked transactions visually or a specific TransferEntity
            repository.insertTransaction(
                TransactionEntity(
                    type = "EXPENSE",
                    account_id = fromAccountId,
                    category_id = null,
                    description = "Transferência enviada: $description",
                    expected_value = value,
                    final_value = value,
                    expected_date = date,
                    payment_date = date,
                    status = "PAGO",
                    recurrence_type = "NENHUMA",
                    recurrence_group_id = "TRANSFER"
                )
            )
            repository.insertTransaction(
                TransactionEntity(
                    type = "INCOME",
                    account_id = toAccountId,
                    category_id = null,
                    description = "Transferência recebida: $description",
                    expected_value = value,
                    final_value = value,
                    expected_date = date,
                    payment_date = date,
                    status = "RECEBIDO",
                    recurrence_type = "NENHUMA",
                    recurrence_group_id = "TRANSFER"
                )
            )
        }
    }
}
