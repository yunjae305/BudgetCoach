package com.budgetcoach.data.repository

import com.budgetcoach.data.local.dao.AssetDao
import com.budgetcoach.data.local.entity.AssetEntity
import com.budgetcoach.data.remote.BankApiService
import kotlinx.coroutines.flow.Flow

class AssetRepository(
    private val assetDao: AssetDao,
    private val bankApiService: BankApiService
) {

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

    suspend fun syncAssets() {
        try {
            val remoteAssets = bankApiService.getRemoteAccounts()
            remoteAssets.forEach { remote ->
                // Simple sync logic: insert or update based on name
                // In a real app, you might use a unique ID from the bank
                assetDao.insert(
                    AssetEntity(
                        name = remote.name,
                        type = remote.type,
                        balance = remote.balance
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
