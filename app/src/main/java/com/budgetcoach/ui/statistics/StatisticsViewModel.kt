package com.budgetcoach.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetcoach.data.local.entity.ExpenseEntity
import com.budgetcoach.data.repository.ExpenseRepository
import com.budgetcoach.domain.usecase.CategoryPercentage
import com.budgetcoach.domain.usecase.StatisticsCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*

class StatisticsViewModel(
    private val expenseRepository: ExpenseRepository,
    private val statisticsCalculator: StatisticsCalculator
) : ViewModel() {

    private val _categoryData = MutableStateFlow<List<CategoryPercentage>>(emptyList())
    val categoryData: StateFlow<List<CategoryPercentage>> = _categoryData.asStateFlow()

    private val _dailyData = MutableStateFlow<List<Pair<String, Long>>>(emptyList())
    val dailyData: StateFlow<List<Pair<String, Long>>> = _dailyData.asStateFlow()

    private val _totalSpent = MutableStateFlow(0L)
    val totalSpent: StateFlow<Long> = _totalSpent.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        val now = YearMonth.now()
        val startOfMonth = now.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfMonth = now.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        viewModelScope.launch {
            expenseRepository.getCategoryTotals(startOfMonth, endOfMonth).collect { totals ->
                _categoryData.value = statisticsCalculator.calculateCategoryPercentages(totals)
                _totalSpent.value = totals.sumOf { it.total }
            }
        }

        viewModelScope.launch {
            expenseRepository.getByDateRange(startOfMonth, endOfMonth).collect { expenses ->
                _dailyData.value = calculateDailyTotals(expenses)
            }
        }
    }

    private fun calculateDailyTotals(expenses: List<ExpenseEntity>): List<Pair<String, Long>> {
        val zone = ZoneId.systemDefault()
        val grouped = expenses.groupBy { expense ->
            java.time.Instant.ofEpochMilli(expense.date).atZone(zone).toLocalDate()
        }

        val now = LocalDate.now()
        val startDay = now.withDayOfMonth(1)
        val days = mutableListOf<Pair<String, Long>>()

        var day = startDay
        while (!day.isAfter(now)) {
            val total = grouped[day]?.sumOf { it.amount } ?: 0L
            days.add(Pair("${day.dayOfMonth}일", total))
            day = day.plusDays(1)
        }

        // 최근 7일만 표시 (너무 많으면)
        return if (days.size > 7) days.takeLast(7) else days
    }
}
