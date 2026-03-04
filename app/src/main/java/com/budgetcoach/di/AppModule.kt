package com.budgetcoach.di

import android.content.Context
import com.budgetcoach.data.ai.GeminiService
import com.budgetcoach.data.local.AppDatabase
import com.budgetcoach.data.repository.AssetRepository
import com.budgetcoach.data.repository.BudgetRepository
import com.budgetcoach.data.repository.ExpenseRepository
import com.budgetcoach.domain.usecase.BudgetCalculator
import com.budgetcoach.domain.usecase.StatisticsCalculator

class AppModule(context: Context) {
    private val database = AppDatabase.getInstance(context)

    val budgetRepository = BudgetRepository(database.budgetDao())
    val expenseRepository = ExpenseRepository(database.expenseDao())
    val assetRepository = AssetRepository(database.assetDao())

    val budgetCalculator = BudgetCalculator()
    val statisticsCalculator = StatisticsCalculator()

    val geminiService = GeminiService()
}
