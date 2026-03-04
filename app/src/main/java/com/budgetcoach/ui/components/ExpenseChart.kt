package com.budgetcoach.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.budgetcoach.domain.model.ExpenseCategory
import com.budgetcoach.domain.usecase.CategoryPercentage
import com.budgetcoach.ui.theme.*

private val categoryColors = mapOf(
    "FOOD" to CategoryFood,
    "TRANSPORT" to CategoryTransport,
    "SHOPPING" to CategoryShopping,
    "CULTURE" to CategoryCulture,
    "MEDICAL" to CategoryMedical,
    "EDUCATION" to CategoryEducation,
    "HOUSING" to CategoryHousing,
    "COMMUNICATION" to CategoryCommunication,
    "OTHER" to CategoryOther
)

fun getCategoryColor(category: String): Color =
    categoryColors[category] ?: CategoryOther

@Composable
fun DonutChart(
    data: List<CategoryPercentage>,
    modifier: Modifier = Modifier
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "donut"
    )
    LaunchedEffect(Unit) { animationPlayed = true }

    if (data.isEmpty()) {
        Box(modifier = modifier.size(200.dp), contentAlignment = Alignment.Center) {
            Text("데이터 없음", color = TextSecondary)
        }
        return
    }

    Canvas(modifier = modifier.size(200.dp).padding(16.dp)) {
        val strokeWidth = 32.dp.toPx()
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
        var startAngle = -90f

        data.forEach { item ->
            val sweepAngle = item.percentage * 360f * animatedProgress
            drawArc(
                color = getCategoryColor(item.category),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeWidth,
                    cap = androidx.compose.ui.graphics.StrokeCap.Butt
                )
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
fun BarChart(
    data: List<Pair<String, Long>>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(modifier = modifier.height(150.dp), contentAlignment = Alignment.Center) {
            Text("데이터 없음", color = TextSecondary)
        }
        return
    }

    val maxValue = data.maxOf { it.second }.coerceAtLeast(1L)

    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "bar"
    )
    LaunchedEffect(Unit) { animationPlayed = true }

    Canvas(modifier = modifier.fillMaxWidth().height(150.dp)) {
        val barWidth = (size.width - 32.dp.toPx()) / data.size.coerceAtLeast(1) * 0.6f
        val spacing = (size.width - 32.dp.toPx()) / data.size.coerceAtLeast(1) * 0.4f
        val bottomPadding = 24.dp.toPx()

        data.forEachIndexed { index, (_, value) ->
            val barHeight = ((value.toFloat() / maxValue) * (size.height - bottomPadding)) * animatedProgress
            val x = 16.dp.toPx() + index * (barWidth + spacing)

            drawRoundRect(
                color = Secondary.copy(alpha = 0.8f),
                topLeft = Offset(x, size.height - bottomPadding - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        data.forEach { (label, _) ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
        }
    }
}
