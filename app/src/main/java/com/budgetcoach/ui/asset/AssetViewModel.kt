package com.budgetcoach.ui.asset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetcoach.data.local.entity.AssetEntity
import com.budgetcoach.data.repository.AssetRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AssetViewModel(
    private val assetRepository: AssetRepository
) : ViewModel() {

    private val _assets = MutableStateFlow<List<AssetEntity>>(emptyList())
    val assets: StateFlow<List<AssetEntity>> = _assets.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadAssets()
    }

    private fun loadAssets() {
        viewModelScope.launch {
            assetRepository.getAll().collect { list ->
                _assets.value = list
            }
        }
    }

    fun refreshAssetsFromApi() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                assetRepository.syncAssets()
            } catch (e: Exception) {
                // Handle error (e.g., show a Snackbar)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun addAsset(name: String, type: String, balance: Long) {
        viewModelScope.launch {
            assetRepository.save(
                AssetEntity(name = name, type = type, balance = balance)
            )
        }
    }

    fun deleteAsset(asset: AssetEntity) {
        viewModelScope.launch {
            assetRepository.delete(asset)
        }
    }

    fun updateBalance(asset: AssetEntity, newBalance: Long) {
        viewModelScope.launch {
            assetRepository.update(asset.copy(balance = newBalance))
        }
    }
}
