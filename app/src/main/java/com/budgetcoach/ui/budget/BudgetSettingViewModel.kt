package com.budgetcoach.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetcoach.data.local.entity.BudgetEntity
import com.budgetcoach.data.repository.BudgetRepository
import com.budgetcoach.domain.usecase.BudgetCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BudgetSettingViewModel(
    private val budgetRepository: BudgetRepository,
    private val budgetCalculator: BudgetCalculator
) : ViewModel() {

    private val _currentBudget = MutableStateFlow<BudgetEntity?>(null)
    val currentBudget: StateFlow<BudgetEntity?> = _currentBudget.asStateFlow()

    private val _budgetHistory = MutableStateFlow<List<BudgetEntity>>(emptyList())
    val budgetHistory: StateFlow<List<BudgetEntity>> = _budgetHistory.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    init {
        loadBudget()
    }

    private fun loadBudget() {
        val yearMonth = budgetCalculator.getCurrentYearMonth()
        viewModelScope.launch {
            budgetRepository.getByYearMonth(yearMonth).collect { budget ->
                _currentBudget.value = budget
            }
        }
        viewModelScope.launch {
            budgetRepository.getAll().collect { all ->
                _budgetHistory.value = all
            }
        }
    }

    fun saveBudget(amount: Long) {
        val yearMonth = budgetCalculator.getCurrentYearMonth()
        viewModelScope.launch {
            val existing = _currentBudget.value
            if (existing != null) {
                budgetRepository.update(existing.copy(totalBudget = amount))
            } else {
                budgetRepository.save(
                    BudgetEntity(yearMonth = yearMonth, totalBudget = amount)
                )
            }
            _saveSuccess.value = true
        }
    }

    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }
}
