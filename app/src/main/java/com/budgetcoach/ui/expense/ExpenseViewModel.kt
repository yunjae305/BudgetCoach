package com.budgetcoach.ui.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetcoach.data.local.entity.AssetEntity
import com.budgetcoach.data.local.entity.ExpenseEntity
import com.budgetcoach.data.repository.AssetRepository
import com.budgetcoach.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.ZoneId

class ExpenseViewModel(
    private val expenseRepository: ExpenseRepository,
    private val assetRepository: AssetRepository
) : ViewModel() {

    private val _expenses = MutableStateFlow<List<ExpenseEntity>>(emptyList())
    val expenses: StateFlow<List<ExpenseEntity>> = _expenses.asStateFlow()

    private val _assets = MutableStateFlow<List<AssetEntity>>(emptyList())
    val assets: StateFlow<List<AssetEntity>> = _assets.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    init {
        loadExpenses()
        loadAssets()
    }

    private fun loadExpenses() {
        val now = YearMonth.now()
        val startOfMonth = now.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfMonth = now.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        viewModelScope.launch {
            expenseRepository.getByDateRange(startOfMonth, endOfMonth).collect { list ->
                _expenses.value = list
            }
        }
    }

    private fun loadAssets() {
        viewModelScope.launch {
            assetRepository.getAll().collect { list ->
                _assets.value = list
            }
        }
    }

    fun saveExpense(title: String, amount: Long, category: String, assetId: Long?, memo: String) {
        viewModelScope.launch {
            expenseRepository.save(
                ExpenseEntity(
                    title = title,
                    amount = amount,
                    category = category,
                    assetId = assetId,
                    date = System.currentTimeMillis(),
                    memo = memo
                )
            )
            _saveSuccess.value = true
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            expenseRepository.delete(expense)
        }
    }

    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }
}
