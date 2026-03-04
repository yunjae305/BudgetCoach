package com.budgetcoach.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budgetcoach.data.local.entity.ExpenseEntity
import com.budgetcoach.domain.model.DailyBudgetInfo
import com.budgetcoach.domain.model.ExpenseCategory
import com.budgetcoach.ui.components.BudgetProgressRing
import com.budgetcoach.ui.components.CategoryIcon
import com.budgetcoach.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToExpense: () -> Unit,
    onNavigateToBudget: () -> Unit
) {
    val budgetInfo by viewModel.budgetInfo.collectAsState()
    val recentExpenses by viewModel.recentExpenses.collectAsState()
    val hasBudget by viewModel.hasBudget.collectAsState()
    val aiAdvice by viewModel.aiAdvice.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item { HeaderSection() }

            item {
                AiAdviceCard(aiAdvice, isAiLoading)
            }

            if (!hasBudget) {
                item { NoBudgetCard(onNavigateToBudget) }
            } else if (budgetInfo != null) {
                item { BudgetOverviewCard(budgetInfo!!) }
                item { DailyRecommendCard(budgetInfo!!) }
            }

            if (recentExpenses.isNotEmpty()) {
                item {
                    Text(
                        "최근 지출",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
                items(recentExpenses) { expense ->
                    ExpenseItem(expense)
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = onNavigateToExpense,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Secondary,
            contentColor = PrimaryDark
        ) {
            Icon(Icons.Default.Add, contentDescription = "지출 추가")
        }
    }
}

@Composable
private fun AiAdviceCard(advice: String?, isLoading: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Secondary.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, "AI", tint = Secondary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI 코치의 한마디", style = MaterialTheme.typography.labelLarge, color = Secondary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⏳", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("분석 중...", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            } else {
                Text(
                    advice ?: "이번 달 예산을 먼저 설정하시면 분석을 시작할게요! 📈",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
private fun HeaderSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PrimaryLight, BackgroundDark)
                )
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column {
            Text(
                "BudgetCoach",
                style = MaterialTheme.typography.headlineLarge,
                color = Secondary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "스마트한 재정 관리를 시작하세요 💰",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun NoBudgetCard(onNavigateToBudget: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📊", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "이번 달 예산을 설정해주세요",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "예산을 설정하면 AI 코치가 재정 관리를 도와드려요",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNavigateToBudget,
                colors = ButtonDefaults.buttonColors(containerColor = Secondary)
            ) {
                Text("예산 설정하기", color = PrimaryDark)
            }
        }
    }
}

@Composable
private fun BudgetOverviewCard(info: DailyBudgetInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BudgetProgressRing(
                percentage = info.spentPercentage,
                centerText = "${(info.spentPercentage * 100).toInt()}%",
                centerSubText = "예산 소진율"
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BudgetStatItem(
                    "총 예산",
                    "₩${String.format("%,d", info.totalBudget)}",
                    Info
                )
                BudgetStatItem(
                    "지출",
                    "₩${String.format("%,d", info.totalSpent)}",
                    if (info.isOverBudget) Danger else Warning
                )
                BudgetStatItem(
                    "잔액",
                    "₩${String.format("%,d", info.remainingBudget)}",
                    if (info.isOverBudget) Danger else Success
                )
            }
        }
    }
}

@Composable
private fun BudgetStatItem(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun DailyRecommendCard(info: DailyBudgetInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (info.isOverBudget) Danger.copy(alpha = 0.15f) else Secondary.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (info.isOverBudget) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                contentDescription = null,
                tint = if (info.isOverBudget) Danger else Secondary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    "오늘 권장 지출",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Text(
                    "₩${String.format("%,d", info.dailyRecommended)}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (info.isOverBudget) Danger else Secondary
                )
                Text(
                    "남은 ${info.remainingDays}일 기준",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
        }
    }
}

@Composable
private fun ExpenseItem(expense: ExpenseEntity) {
    val category = ExpenseCategory.fromName(expense.category)
    val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.KOREA)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(getCategoryBackgroundColor(expense.category)),
                contentAlignment = Alignment.Center
            ) {
                CategoryIcon(emoji = category.emoji)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    expense.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    "${category.displayName} · ${dateFormat.format(Date(expense.date))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
            Text(
                "-₩${String.format("%,d", expense.amount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Danger
            )
        }
    }
}

private fun getCategoryBackgroundColor(category: String): androidx.compose.ui.graphics.Color {
    return com.budgetcoach.ui.components.getCategoryColor(category).copy(alpha = 0.15f)
}
