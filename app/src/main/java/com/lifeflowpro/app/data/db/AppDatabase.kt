package com.lifeflowpro.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lifeflowpro.app.data.db.dao.*
import com.lifeflowpro.app.data.db.entities.*

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TaskEntity::class,
        TransactionEntity::class,
        TransferEntity::class,
        DebtEntity::class,
        DebtInstallmentEntity::class,
        BudgetEntity::class,
        GoalEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun taskDao(): TaskDao
    abstract fun transactionDao(): TransactionDao
    abstract fun debtDao(): DebtDao
    abstract fun budgetDao(): BudgetDao
    abstract fun goalDao(): GoalDao
}
