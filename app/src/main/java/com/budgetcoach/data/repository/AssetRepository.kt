package com.budgetcoach.data.repository

import com.budgetcoach.data.local.dao.AssetDao
import com.budgetcoach.data.local.entity.AssetEntity
import kotlinx.coroutines.flow.Flow

class AssetRepository(private val assetDao: AssetDao) {

    fun getAll(): Flow<List<AssetEntity>> =
        assetDao.getAll()

    suspend fun save(asset: AssetEntity): Long =
        assetDao.insert(asset)

    suspend fun update(asset: AssetEntity) =
        assetDao.update(asset)

    suspend fun delete(asset: AssetEntity) =
        assetDao.delete(asset)

    suspend fun getById(id: Long): AssetEntity? =
        assetDao.getById(id)
}
