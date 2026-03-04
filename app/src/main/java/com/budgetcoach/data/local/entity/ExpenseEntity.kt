package com.budgetcoach.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Long, // 원 단위
    val category: String, // ExpenseCategory name
    val assetId: Long? = null,
    val date: Long, // epoch millis
    val memo: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
