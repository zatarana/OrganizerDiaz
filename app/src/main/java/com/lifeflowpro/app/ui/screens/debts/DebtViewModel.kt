package com.lifeflowpro.app.ui.screens.debts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeflowpro.app.data.db.entities.DebtEntity
import com.lifeflowpro.app.data.db.entities.DebtInstallmentEntity
import com.lifeflowpro.app.data.db.entities.TransactionEntity
import com.lifeflowpro.app.data.repository.DebtRepository
import com.lifeflowpro.app.data.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebtViewModel @Inject constructor(
    private val repository: DebtRepository,
    private val financeRepository: FinanceRepository
) : ViewModel() {

    val debts: StateFlow<List<DebtEntity>> = repository.allDebts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addDebt(debt: DebtEntity) {
        viewModelScope.launch {
            repository.insertDebt(debt)
        }
    }

    fun settleIntegral(debt: DebtEntity, finalValue: Double, accountId: Long) {
        viewModelScope.launch {
            val economy = debt.original_value - finalValue
            val updatedDebt = debt.copy(
                status = "QUITADA",
                negotiated_value = finalValue,
                total_economy = economy
            )
            repository.updateDebt(updatedDebt)
            
            // Create transaction
            financeRepository.insertTransaction(
                TransactionEntity(
                    type = "EXPENSE",
                    account_id = accountId,
                    category_id = null,
                    description = "Quitação de Dívida: ${debt.creditor}",
                    expected_value = debt.original_value,
                    final_value = finalValue,
                    expected_date = System.currentTimeMillis(),
                    payment_date = System.currentTimeMillis(),
                    status = "PAGO",
                    recurrence_type = "NENHUMA",
                    recurrence_group_id = null,
                    economy = economy
                )
            )
        }
    }

    fun negotiateDebt(debt: DebtEntity, totalValue: Double, numInstallments: Int, firstDueDate: Long, accountId: Long) {
        viewModelScope.launch {
            val economy = debt.original_value - totalValue
            val installmentValue = totalValue / numInstallments
            
            val updatedDebt = debt.copy(
                status = "EM_PAGAMENTO",
                negotiated_value = totalValue,
                total_economy = economy
            )
            repository.updateDebt(updatedDebt)

            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = firstDueDate

            for (i in 1..numInstallments) {
                repository.insertInstallment(
                    DebtInstallmentEntity(
                        debt_id = debt.id,
                        transaction_id = null,
                        installment_number = i,
                        expected_value = installmentValue,
                        final_value = null,
                        due_date = calendar.timeInMillis,
                        payment_date = null,
                        status = "A_PAGAR"
                    )
                )
                
                // Add to financial transactions as well for planning
                financeRepository.insertTransaction(
                    TransactionEntity(
                        type = "EXPENSE",
                        account_id = accountId,
                        category_id = null,
                        description = "Parcela $i/${numInstallments} - ${debt.creditor}",
                        expected_value = installmentValue,
                        final_value = null,
                        expected_date = calendar.timeInMillis,
                        payment_date = null,
                        status = "A_PAGAR",
                        recurrence_type = "NENHUMA",
                        recurrence_group_id = "DEBT_${debt.id}"
                    )
                )
                
                calendar.add(java.util.Calendar.MONTH, 1)
            }
        }
    }

    fun deleteDebt(debt: DebtEntity) {
        viewModelScope.launch {
            repository.deleteDebt(debt)
        }
    }
}
