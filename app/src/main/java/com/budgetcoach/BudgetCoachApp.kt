package com.budgetcoach

import android.app.Application
import com.budgetcoach.di.AppModule

class BudgetCoachApp : Application() {

    lateinit var appModule: AppModule
        private set

    override fun onCreate() {
        super.onCreate()
        appModule = AppModule(this)
    }
}
