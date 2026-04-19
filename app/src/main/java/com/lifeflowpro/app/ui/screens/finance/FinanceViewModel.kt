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
    private val categoryRepository: CategoryRepository
) : ViewModel() {

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
}
