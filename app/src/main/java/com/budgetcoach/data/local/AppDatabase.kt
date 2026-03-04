package com.budgetcoach.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.budgetcoach.data.local.dao.AssetDao
import com.budgetcoach.data.local.dao.BudgetDao
import com.budgetcoach.data.local.dao.ExpenseDao
import com.budgetcoach.data.local.entity.AssetEntity
import com.budgetcoach.data.local.entity.BudgetEntity
import com.budgetcoach.data.local.entity.ExpenseEntity

@Database(
    entities = [BudgetEntity::class, ExpenseEntity::class, AssetEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun budgetDao(): BudgetDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun assetDao(): AssetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_coach.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
