package com.budgetcoach.data.repository

import com.budgetcoach.data.local.dao.CategoryTotal
import com.budgetcoach.data.local.dao.ExpenseDao
import com.budgetcoach.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    fun getAll(): Flow<List<ExpenseEntity>> =
        expenseDao.getAll()

    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>> =
        expenseDao.getByDateRange(startDate, endDate)

    fun getTotalByDateRange(startDate: Long, endDate: Long): Flow<Long?> =
        expenseDao.getTotalByDateRange(startDate, endDate)

    fun getCategoryTotals(startDate: Long, endDate: Long): Flow<List<CategoryTotal>> =
        expenseDao.getCategoryTotals(startDate, endDate)

    suspend fun getCategoryTotalsSync(startDate: Long, endDate: Long): List<CategoryTotal> =
        expenseDao.getCategoryTotalsSync(startDate, endDate)

    suspend fun getTotalByDateRangeSync(startDate: Long, endDate: Long): Long? =
        expenseDao.getTotalByDateRangeSync(startDate, endDate)

    suspend fun save(expense: ExpenseEntity): Long =
        expenseDao.insert(expense)

    suspend fun update(expense: ExpenseEntity) =
        expenseDao.update(expense)

    suspend fun delete(expense: ExpenseEntity) =
        expenseDao.delete(expense)

    suspend fun getById(id: Long): ExpenseEntity? =
        expenseDao.getById(id)
}
