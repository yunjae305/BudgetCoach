package com.budgetcoach.ui.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.budgetcoach.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetSettingScreen(
    viewModel: BudgetSettingViewModel,
    onBack: () -> Unit
) {
    val currentBudget by viewModel.currentBudget.collectAsState()
    val budgetHistory by viewModel.budgetHistory.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    var budgetInput by remember { mutableStateOf("") }

    LaunchedEffect(currentBudget) {
        currentBudget?.let {
            budgetInput = it.totalBudget.toString()
        }
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            viewModel.resetSaveSuccess()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("예산 설정", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "뒤로", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        },
        containerColor = BackgroundDark
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            "이번 달 예산",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = budgetInput,
                            onValueChange = { value ->
                                if (value.all { it.isDigit() }) budgetInput = value
                            },
                            label = { Text("예산 금액 (원)", color = TextTertiary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Secondary,
                                unfocusedBorderColor = TextTertiary,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = Secondary
                            ),
                            singleLine = true
                        )

                        if (budgetInput.isNotEmpty()) {
                            val amount = budgetInput.toLongOrNull() ?: 0
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "₩${String.format("%,d", amount)}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // 빠른 설정 버튼
                        Text("빠른 설정", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(500_000L, 1_000_000L, 1_500_000L, 2_000_000L).forEach { preset ->
                                FilledTonalButton(
                                    onClick = { budgetInput = preset.toString() },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = SurfaceVariantDark,
                                        contentColor = TextPrimary
                                    ),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(4.dp)
                                ) {
                                    Text(
                                        "${preset / 10000}만",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val amount = budgetInput.toLongOrNull()
                                if (amount != null && amount > 0) {
                                    viewModel.saveBudget(amount)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Secondary),
                            enabled = budgetInput.toLongOrNull()?.let { it > 0 } ?: false
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryDark)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("저장", color = PrimaryDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (budgetHistory.isNotEmpty()) {
                item {
                    Text(
                        "예산 히스토리",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }
                items(budgetHistory) { budget ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(budget.yearMonth, color = TextSecondary)
                            Text(
                                "₩${String.format("%,d", budget.totalBudget)}",
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
