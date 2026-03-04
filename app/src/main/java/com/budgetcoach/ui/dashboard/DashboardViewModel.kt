package com.budgetcoach.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetcoach.data.ai.GeminiService
import com.budgetcoach.data.local.entity.ExpenseEntity
import com.budgetcoach.data.repository.BudgetRepository
import com.budgetcoach.data.repository.ExpenseRepository
import com.budgetcoach.domain.model.DailyBudgetInfo
import com.budgetcoach.domain.usecase.BudgetCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class DashboardViewModel(
    private val budgetRepository: BudgetRepository,
    private val expenseRepository: ExpenseRepository,
    private val budgetCalculator: BudgetCalculator,
    private val geminiService: GeminiService
) : ViewModel() {

    private val _budgetInfo = MutableStateFlow<DailyBudgetInfo?>(null)
    val budgetInfo: StateFlow<DailyBudgetInfo?> = _budgetInfo.asStateFlow()

    private val _recentExpenses = MutableStateFlow<List<ExpenseEntity>>(emptyList())
    val recentExpenses: StateFlow<List<ExpenseEntity>> = _recentExpenses.asStateFlow()

    private val _hasBudget = MutableStateFlow(false)
    val hasBudget: StateFlow<Boolean> = _hasBudget.asStateFlow()

    private val _aiAdvice = MutableStateFlow<String?>(null)
    val aiAdvice: StateFlow<String?> = _aiAdvice.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        val yearMonth = budgetCalculator.getCurrentYearMonth()
        val now = YearMonth.now()
        val startOfMonth = now.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfMonth = now.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        viewModelScope.launch {
            budgetRepository.getByYearMonth(yearMonth).collect { budget ->
                if (budget != null) {
                    _hasBudget.value = true
                    expenseRepository.getTotalByDateRange(startOfMonth, endOfMonth).collect { total ->
                        val info = budgetCalculator.calculateDailyBudget(
                            totalBudget = budget.totalBudget,
                            totalSpent = total ?: 0L
                        )
                        _budgetInfo.value = info
                        fetchAiAdvice(info)
                    }
                } else {
                    _hasBudget.value = false
                    _budgetInfo.value = null
                    _aiAdvice.value = null
                }
            }
        }

        viewModelScope.launch {
            expenseRepository.getByDateRange(startOfMonth, endOfMonth).collect { expenses ->
                _recentExpenses.value = expenses.take(10)
            }
        }
    }

    private fun fetchAiAdvice(info: DailyBudgetInfo) {
        if (!geminiService.isAvailable || _aiAdvice.value != null) return

        viewModelScope.launch {
            _isAiLoading.value = true
            try {
                val advice = geminiService.getAdvice(
                    remainingBudget = info.remainingBudget,
                    remainingDays = info.remainingDays,
                    dailyRecommended = info.dailyRecommended,
                    totalSpent = info.totalSpent
                )
                _aiAdvice.value = advice
            } catch (e: Exception) {
                _aiAdvice.value = "조언을 가져오는 중 오류가 발생했습니다."
            } finally {
                _isAiLoading.value = false
            }
        }
    }
}
