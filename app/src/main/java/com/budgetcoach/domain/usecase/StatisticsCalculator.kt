package com.budgetcoach.domain.usecase

import com.budgetcoach.data.local.dao.CategoryTotal

data class CategoryPercentage(
    val category: String,
    val amount: Long,
    val percentage: Float
)

class StatisticsCalculator {

    fun calculateCategoryPercentages(categoryTotals: List<CategoryTotal>): List<CategoryPercentage> {
        val total = categoryTotals.sumOf { it.total }
        if (total == 0L) return emptyList()

        return categoryTotals
            .sortedByDescending { it.total }
            .map { ct ->
                CategoryPercentage(
                    category = ct.category,
                    amount = ct.total,
                    percentage = ct.total.toFloat() / total.toFloat()
                )
            }
    }
}
