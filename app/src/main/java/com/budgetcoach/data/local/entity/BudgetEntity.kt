package com.budgetcoach.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val yearMonth: String, // "2026-03"
    val totalBudget: Long, // 원 단위
    val createdAt: Long = System.currentTimeMillis()
)
