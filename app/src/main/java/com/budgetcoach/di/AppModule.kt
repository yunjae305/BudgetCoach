package com.budgetcoach.di

import android.content.Context
import com.budgetcoach.data.ai.GeminiService
import com.budgetcoach.data.local.AppDatabase
import com.budgetcoach.data.remote.BankApiService
import com.budgetcoach.data.repository.AssetRepository
import com.budgetcoach.data.repository.BudgetRepository
import com.budgetcoach.data.repository.ExpenseRepository
import com.budgetcoach.domain.usecase.BudgetCalculator
import com.budgetcoach.domain.usecase.StatisticsCalculator
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppModule(context: Context) {
    private val database = AppDatabase.getInstance(context)

    // Retrofit for Spring Boot Backend
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://172.31.128.1:8080/") // Updated to your PC's IP address
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val bankApiService = retrofit.create(BankApiService::class.java)

    val budgetRepository = BudgetRepository(database.budgetDao())
    val expenseRepository = ExpenseRepository(database.expenseDao())
    val assetRepository = AssetRepository(database.assetDao(), bankApiService)

    val budgetCalculator = BudgetCalculator()
    val statisticsCalculator = StatisticsCalculator()

    val geminiService = GeminiService()
}
