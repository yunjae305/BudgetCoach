package com.budgetcoach.domain.model

data class DailyBudgetInfo(
    val totalBudget: Long,
    val totalSpent: Long,
    val remainingBudget: Long,
    val remainingDays: Int,
    val dailyRecommended: Long,
    val spentPercentage: Float,
    val isOverBudget: Boolean
)
