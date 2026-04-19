package com.lifeflowpro.app.data.db

import com.lifeflowpro.app.data.db.entities.AccountEntity
import com.lifeflowpro.app.data.db.entities.CategoryEntity
import com.lifeflowpro.app.data.repository.CategoryRepository
import com.lifeflowpro.app.data.repository.FinanceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseInitializer @Inject constructor(
    private val financeRepository: FinanceRepository,
    private val categoryRepository: CategoryRepository
) {
    fun initialize() {
        CoroutineScope(Dispatchers.IO).launch {
            val accounts = financeRepository.allAccounts.first()
            if (accounts.isEmpty()) {
                financeRepository.insertAccount(
                    AccountEntity(name = "Carteira", icon = "wallet", color = 0xFF6366F1.toInt(), initialBalance = 0.0)
                )
            }

            val categories = categoryRepository.allCategories.first()
            if (categories.isEmpty()) {
                val defaultCategories = listOf(
                    CategoryEntity(name = "Alimentação", color = 0xFFEF4444.toInt(), type = "EXPENSE"),
                    CategoryEntity(name = "Transporte", color = 0xFF3B82F6.toInt(), type = "EXPENSE"),
                    CategoryEntity(name = "Saúde", color = 0xFF10B981.toInt(), type = "EXPENSE"),
                    CategoryEntity(name = "Lazer", color = 0xFFF59E0B.toInt(), type = "EXPENSE"),
                    CategoryEntity(name = "Salário", color = 0xFF10B981.toInt(), type = "INCOME")
                )
                defaultCategories.forEach { categoryRepository.insert(it) }
            }
        }
    }
}
