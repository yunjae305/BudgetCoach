package com.budgetcoach.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,       // "현금", "신용카드" 등
    val type: String,       // "CASH" or "CARD"
    val balance: Long = 0   // 원 단위
)
