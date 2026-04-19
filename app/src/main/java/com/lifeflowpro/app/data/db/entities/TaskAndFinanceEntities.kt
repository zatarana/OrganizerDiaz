package com.lifeflowpro.app.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    indices = [Index(value = ["due_date"]), Index(value = ["status"])],
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String?,
    val category_id: Long?,
    val status: String, // "PENDENTE", "CONCLUIDA", "ATRASADA"
    val due_date: Long?,
    val due_time: String?,
    val recurrence_type: String, // "NENHUMA", "DIARIA", "SEMANAL", "MENSAL", "PERSONALIZADA"
    val recurrence_config: String?,
    val priority: String, // "BAIXA", "MEDIA", "ALTA"
    val parent_task_id: Long?,
    val linked_transaction_id: Long?,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["expected_date"]), Index(value = ["status"])],
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["account_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "INCOME", "EXPENSE"
    val account_id: Long,
    val category_id: Long?,
    val description: String?,
    val expected_value: Double,
    val final_value: Double?,
    val expected_date: Long,
    val payment_date: Long?,
    val status: String, // "A_RECEBER", "RECEBIDO", "A_PAGAR", "PAGO"
    val recurrence_type: String,
    val recurrence_group_id: String?,
    val economy: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
