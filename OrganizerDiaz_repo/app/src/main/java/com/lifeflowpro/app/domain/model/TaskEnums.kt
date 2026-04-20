package com.lifeflowpro.app.domain.model

enum class TaskPriority(val label: String) {
    LOW("Baixa"),
    MEDIUM("Média"),
    HIGH("Alta")
}

enum class TaskStatus {
    PENDING,
    COMPLETED,
    OVERDUE
}

enum class RecurrenceType(val label: String) {
    NONE("Nenhuma"),
    DAILY("Diária"),
    WEEKLY("Semanal"),
    MONTHLY("Mensal"),
    CUSTOM("Personalizada")
}
