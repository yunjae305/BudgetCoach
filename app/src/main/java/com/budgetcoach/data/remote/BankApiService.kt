package com.budgetcoach.data.remote

import retrofit2.http.GET

interface BankApiService {
    @GET("api/assets/my-accounts")
    suspend fun getRemoteAccounts(): List<AssetResponse>
}

data class AssetResponse(
    val name: String,
    val type: String,
    val balance: Long
)
