package com.budgetcoach.data.local.dao

import androidx.room.*
import com.budgetcoach.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity): Long

    @Update
    suspend fun update(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Query("SELECT * FROM budgets WHERE yearMonth = :yearMonth LIMIT 1")
    fun getByYearMonth(yearMonth: String): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets WHERE yearMonth = :yearMonth LIMIT 1")
    suspend fun getByYearMonthSync(yearMonth: String): BudgetEntity?

    @Query("SELECT * FROM budgets ORDER BY yearMonth DESC")
    fun getAll(): Flow<List<BudgetEntity>>
}
