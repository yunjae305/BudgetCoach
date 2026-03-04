package com.budgetcoach

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.budgetcoach.ui.navigation.AppNavigation
import com.budgetcoach.ui.theme.BudgetCoachTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as BudgetCoachApp

        setContent {
            BudgetCoachTheme {
                AppNavigation(appModule = app.appModule)
            }
        }
    }
}
