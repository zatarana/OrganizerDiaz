package com.lifeflowpro.app.domain.model

enum class TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER
}

enum class TransactionStatus {
    TO_RECEIVE,
    RECEIVED,
    TO_PAY,
    PAID
}

data class FinancialSummary(
    val realBalance: Double,
    val predictedBalance: Double
)
