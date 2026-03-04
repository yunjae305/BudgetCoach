package com.budgetcoach.domain.usecase

import com.budgetcoach.domain.model.DailyBudgetInfo
import java.time.LocalDate
import java.time.YearMonth

class BudgetCalculator {

    fun calculateDailyBudget(
        totalBudget: Long,
        totalSpent: Long,
        currentDate: LocalDate = LocalDate.now()
    ): DailyBudgetInfo {
        val yearMonth = YearMonth.from(currentDate)
        val totalDaysInMonth = yearMonth.lengthOfMonth()
        val remainingDays = totalDaysInMonth - currentDate.dayOfMonth + 1 // 오늘 포함
        val remainingBudget = totalBudget - totalSpent
        val dailyRecommended = if (remainingDays > 0 && remainingBudget > 0) {
            remainingBudget / remainingDays
        } else {
            0L
        }
        val spentPercentage = if (totalBudget > 0) {
            (totalSpent.toFloat() / totalBudget.toFloat()).coerceIn(0f, 1.5f)
        } else {
            0f
        }

        return DailyBudgetInfo(
            totalBudget = totalBudget,
            totalSpent = totalSpent,
            remainingBudget = remainingBudget,
            remainingDays = remainingDays,
            dailyRecommended = dailyRecommended,
            spentPercentage = spentPercentage,
            isOverBudget = remainingBudget < 0
        )
    }

    fun getCurrentYearMonth(): String {
        val now = YearMonth.now()
        return "${now.year}-${now.monthValue.toString().padStart(2, '0')}"
    }
}
