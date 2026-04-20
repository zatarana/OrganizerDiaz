package com.lifeflowpro.app.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val creditor: String,
    val description: String?,
    val original_value: Double,
    val negotiated_value: Double?,
    val origin_date: Long,
    val status: String, // "EM_ABERTO", "EM_PAGAMENTO", "QUITADA"
    val total_economy: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "debt_installments",
    indices = [Index(value = ["due_date"]), Index(value = ["status"])],
    foreignKeys = [
        ForeignKey(
            entity = DebtEntity::class,
            parentColumns = ["id"],
            childColumns = ["debt_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DebtInstallmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val debt_id: Long,
    val installment_number: Int,
    val expected_value: Double,
    val final_value: Double?,
    val due_date: Long,
    val payment_date: Long?,
    val status: String,
    val economy: Double = 0.0,
    val transaction_id: Long?
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String,
    val target_value: Double,
    val current_value: Double,
    val target_date: Long?,
    val status: String, // "ATIVA", "CONCLUIDA"
    val completed_at: Long?,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category_id: Long,
    val month_year: String, // "MM-YYYY"
    val planned_value: Double,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transfers")
data class TransferEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val from_account_id: Long,
    val to_account_id: Long,
    val value: Double,
    val description: String?,
    val date: Long,
    val createdAt: Long = System.currentTimeMillis()
)
