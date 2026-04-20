package com.lifeflowpro.app.data.repository

import com.lifeflowpro.app.data.db.dao.*
import com.lifeflowpro.app.data.db.entities.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(private val taskDao: TaskDao) {
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    suspend fun insert(task: TaskEntity) = taskDao.insertTask(task)
    suspend fun update(task: TaskEntity) = taskDao.updateTask(task)
    suspend fun delete(task: TaskEntity) = taskDao.deleteTask(task)
}

@Singleton
class FinanceRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val budgetDao: BudgetDao,
    private val goalDao: GoalDao
) {
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allAccounts: Flow<List<AccountEntity>> = accountDao.getAllAccounts()
    val allBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()
    val allGoals: Flow<List<GoalEntity>> = goalDao.getAllGoals()

    suspend fun insertTransaction(transaction: TransactionEntity) = transactionDao.insertTransaction(transaction)
    suspend fun updateTransaction(transaction: TransactionEntity) = transactionDao.updateTransaction(transaction)
    
    suspend fun insertAccount(account: AccountEntity) = accountDao.insertAccount(account)
    suspend fun updateAccount(account: AccountEntity) = accountDao.updateAccount(account)
    
    suspend fun insertBudget(budget: BudgetEntity) = budgetDao.insertBudget(budget)
    suspend fun insertGoal(goal: GoalEntity) = goalDao.insertGoal(goal)
}

@Singleton
class DebtRepository @Inject constructor(private val debtDao: DebtDao) {
    val allDebts: Flow<List<DebtEntity>> = debtDao.getAllDebts()
    val allInstallments: Flow<List<DebtInstallmentEntity>> = debtDao.getAllInstallments()

    suspend fun insertDebt(debt: DebtEntity) = debtDao.insertDebt(debt)
    suspend fun updateDebt(debt: DebtEntity) = debtDao.updateDebt(debt)
    suspend fun deleteDebt(debt: DebtEntity) = debtDao.deleteDebt(debt)

    fun getInstallments(debtId: Long) = debtDao.getInstallmentsByDebt(debtId)
    suspend fun insertInstallment(installment: DebtInstallmentEntity) = debtDao.insertInstallment(installment)
    suspend fun updateInstallment(installment: DebtInstallmentEntity) = debtDao.updateInstallment(installment)
}

@Singleton
class CategoryRepository @Inject constructor(private val categoryDao: CategoryDao) {
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    suspend fun insert(category: CategoryEntity) = categoryDao.insertCategory(category)
}
