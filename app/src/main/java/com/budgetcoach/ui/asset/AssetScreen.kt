package com.budgetcoach.ui.asset

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.budgetcoach.data.local.entity.AssetEntity
import com.budgetcoach.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetScreen(
    viewModel: AssetViewModel,
    onBack: () -> Unit
) {
    val assets by viewModel.assets.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("자산 관리", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "뒤로", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Secondary,
                contentColor = PrimaryDark
            ) {
                Icon(Icons.Default.Add, "자산 추가")
            }
        },
        containerColor = BackgroundDark
    ) { padding ->
        if (assets.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💳", style = MaterialTheme.typography.displayLarge)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("등록된 자산이 없어요", color = TextSecondary)
                    Text("현금, 카드 등을 추가해보세요", color = TextTertiary, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 총 자산
                item {
                    val totalBalance = assets.sumOf { it.balance }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("총 자산", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
                            Text(
                                "₩${String.format("%,d", totalBalance)}",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = Secondary
                            )
                        }
                    }
                }

                items(assets, key = { it.id }) { asset ->
                    AssetItem(
                        asset = asset,
                        onDelete = { viewModel.deleteAsset(asset) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddAssetDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, type, balance ->
                viewModel.addAsset(name, type, balance)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AssetItem(asset: AssetEntity, onDelete: () -> Unit) {
    val icon = if (asset.type == "CASH") "💵" else "💳"
    val typeLabel = if (asset.type == "CASH") "현금" else "카드"
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (asset.type == "CASH") Success.copy(alpha = 0.15f) else Info.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(asset.name, color = TextPrimary, fontWeight = FontWeight.Medium)
                Text(typeLabel, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "₩${String.format("%,d", asset.balance)}",
                    fontWeight = FontWeight.Bold,
                    color = if (asset.balance >= 0) TextPrimary else Danger
                )
                IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, "삭제", tint = TextTertiary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("삭제 확인") },
            text = { Text("'${asset.name}' 자산을 삭제할까요?") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteConfirm = false }) {
                    Text("삭제", color = Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("취소", color = TextSecondary)
                }
            },
            containerColor = CardDark
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAssetDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, type: String, balance: Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("CASH") }
    var balance by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("자산 추가", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("자산명", color = TextTertiary) },
                    placeholder = { Text("예: 현금, 신한카드", color = TextTertiary) },
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

                Text("유형", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedType == "CASH",
                        onClick = { selectedType = "CASH" },
                        label = { Text("💵 현금") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Success.copy(alpha = 0.2f),
                            selectedLabelColor = Success,
                            containerColor = SurfaceVariantDark,
                            labelColor = TextSecondary
                        )
                    )
                    FilterChip(
                        selected = selectedType == "CARD",
                        onClick = { selectedType = "CARD" },
                        label = { Text("💳 카드") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Info.copy(alpha = 0.2f),
                            selectedLabelColor = Info,
                            containerColor = SurfaceVariantDark,
                            labelColor = TextSecondary
                        )
                    )
                }

                OutlinedTextField(
                    value = balance,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '-' }) balance = it },
                    label = { Text("잔액 (원)", color = TextTertiary) },
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bal = balance.toLongOrNull() ?: 0L
                    if (name.isNotBlank()) {
                        onSave(name, selectedType, bal)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Secondary),
                enabled = name.isNotBlank()
            ) {
                Text("추가", color = PrimaryDark)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = TextSecondary)
            }
        },
        containerColor = CardDark
    )
}
