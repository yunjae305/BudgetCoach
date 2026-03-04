package com.budgetcoach.data.repository

import com.budgetcoach.data.local.dao.BudgetDao
import com.budgetcoach.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val budgetDao: BudgetDao) {

    fun getByYearMonth(yearMonth: String): Flow<BudgetEntity?> =
        budgetDao.getByYearMonth(yearMonth)

    suspend fun getByYearMonthSync(yearMonth: String): BudgetEntity? =
        budgetDao.getByYearMonthSync(yearMonth)

    fun getAll(): Flow<List<BudgetEntity>> =
        budgetDao.getAll()

    suspend fun save(budget: BudgetEntity): Long =
        budgetDao.insert(budget)

    suspend fun update(budget: BudgetEntity) =
        budgetDao.update(budget)

    suspend fun delete(budget: BudgetEntity) =
        budgetDao.delete(budget)
}
