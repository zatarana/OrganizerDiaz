package com.lifeflowpro.app.ui.screens.debts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeflowpro.app.data.db.entities.DebtEntity
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtsScreen(viewModel: DebtViewModel = hiltViewModel()) {
    val debts by viewModel.debts.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddDebtSheet by remember { mutableStateOf(false) }
    var selectedDebtForAction by remember { mutableStateOf<DebtEntity?>(null) }
    var showSettleDialog by remember { mutableStateOf(false) }
    var showNegotiateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(16.dp)) {
                Text("Dívidas", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
                
                when (selectedTab) {
                    0, 1 -> {
                        val openDebtSum = debts.filter { it.status == "EM_ABERTO" || it.status == "EM_PAGAMENTO" }
                            .sumOf { it.negotiated_value ?: it.original_value }
                        Text(
                            "Você tem ${currencyFormatter.format(openDebtSum)} em dívidas em aberto",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    2 -> {
                        val settled = debts.filter { it.status == "QUITADA" }
                        val settledSum = settled.sumOf { it.original_value }
                        val economySum = settled.sumOf { it.total_economy }
                        Text(
                            "Você zerou ${currencyFormatter.format(settledSum)} em dívidas e economizou ${currencyFormatter.format(economySum)}",
                            fontSize = 14.sp,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDebtSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nova Dívida")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Em Aberto") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Pagando") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Quitadas") })
            }

            when (selectedTab) {
                0 -> DebtList(debts.filter { it.status == "EM_ABERTO" }, viewModel.installments.collectAsState().value, viewModel, onDebtClick = { selectedDebtForAction = it })
                1 -> DebtList(debts.filter { it.status == "EM_PAGAMENTO" }, viewModel.installments.collectAsState().value, viewModel, onDebtClick = { selectedDebtForAction = it })
                2 -> DebtList(debts.filter { it.status == "QUITADA" }, viewModel.installments.collectAsState().value, viewModel, onDebtClick = { selectedDebtForAction = it })
            }
        }

        if (selectedDebtForAction != null && !showSettleDialog && !showNegotiateDialog) {
            DebtActionBottomSheet(
                debt = selectedDebtForAction!!,
                onDismiss = { selectedDebtForAction = null },
                onSettle = { showSettleDialog = true },
                onNegotiate = { showNegotiateDialog = true },
                onDelete = { 
                    viewModel.deleteDebt(selectedDebtForAction!!) 
                    selectedDebtForAction = null
                }
            )
        }

        if (showSettleDialog && selectedDebtForAction != null) {
            SettleDebtDialog(
                debt = selectedDebtForAction!!,
                onDismiss = { showSettleDialog = false },
                onConfirm = { finalVal ->
                    viewModel.settleIntegral(selectedDebtForAction!!, finalVal, 1L)
                    showSettleDialog = false
                    selectedDebtForAction = null
                }
            )
        }

        if (showNegotiateDialog && selectedDebtForAction != null) {
            NegotiateDebtBottomSheet(
                debt = selectedDebtForAction!!,
                onDismiss = { showNegotiateDialog = false },
                onConfirm = { total, installments ->
                    viewModel.negotiateDebt(selectedDebtForAction!!, total, installments, System.currentTimeMillis(), 1L)
                    showNegotiateDialog = false
                    selectedDebtForAction = null
                }
            )
        }

        if (showAddDebtSheet) {
            AddDebtBottomSheet(
                onDismiss = { showAddDebtSheet = false },
                onSave = { creditor, value ->
                    viewModel.addDebt(
                        DebtEntity(
                            creditor = creditor,
                            description = null,
                            original_value = value,
                            negotiated_value = null,
                            origin_date = System.currentTimeMillis(),
                            status = "EM_ABERTO"
                        )
                    )
                    showAddDebtSheet = false
                }
            )
        }
    }
}

@Composable
fun DebtList(
    debts: List<DebtEntity>,
    installments: List<DebtInstallmentEntity>,
    viewModel: DebtViewModel,
    onDebtClick: (DebtEntity) -> Unit
) {
    if (debts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nenhum registro encontrado", color = Color.Gray)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(debts) { debt ->
                val debtInstallments = installments.filter { it.debt_id == debt.id }
                DebtItem(debt, debtInstallments, onClick = { onDebtClick(debt) })
            }
        }
    }
}

@Composable
fun DebtItem(debt: DebtEntity, installments: List<DebtInstallmentEntity>, onClick: () -> Unit) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(debt.creditor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    val daysOpen = ((System.currentTimeMillis() - debt.origin_date) / (1000 * 60 * 60 * 24))
                    Text("Em aberto há $daysOpen dias (Desde ${formatDate(debt.origin_date)})", fontSize = 12.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        currencyFormatter.format(debt.negotiated_value ?: debt.original_value),
                        fontWeight = FontWeight.Bold,
                        color = if (debt.status == "QUITADA") Color(0xFF10B981) else Color.Black
                    )
                    if (debt.total_economy > 0) {
                        Text(
                            "Economia de ${currencyFormatter.format(debt.total_economy)}",
                            fontSize = 11.sp,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }

            if (debt.status == "EM_PAGAMENTO" && installments.isNotEmpty()) {
                val totalInstallments = installments.size
                val paidInstallments = installments.count { it.status == "PAGO" }
                val progress = if (totalInstallments > 0) paidInstallments.toFloat() / totalInstallments else 0f
                
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Progresso: $paidInstallments/$totalInstallments", fontSize = 12.sp, color = Color.Gray)
                    
                    val nextInstallment = installments.firstOrNull { it.status != "PAGO" }
                    if (nextInstallment != null) {
                        Text("Próxima parc: ${formatDate(nextInstallment.due_date)}", fontSize = 12.sp, color = Color(0xFFEAB308), fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = Color(0xFF3B82F6),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    return sdf.format(Date(timestamp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDebtBottomSheet(onDismiss: () -> Unit, onSave: (String, Double) -> Unit) {
    var creditor by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
            Text("Nova Dívida", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = creditor,
                onValueChange = { creditor = it },
                label = { Text("Credor") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text("Valor Original (R$)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    val v = value.toDoubleOrNull() ?: 0.0
                    if (creditor.isNotBlank() && v > 0) onSave(creditor, v)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = creditor.isNotBlank() && value.isNotBlank()
            ) {
                Text("Registrar Dívida")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtActionBottomSheet(
    debt: DebtEntity,
    onDismiss: () -> Unit,
    onSettle: () -> Unit,
    onNegotiate: () -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
            Text("Opções para: ${debt.creditor}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(modifier = Modifier.fillMaxWidth(), onClick = onSettle) {
                Text("Quitar Integral")
            }
            TextButton(modifier = Modifier.fillMaxWidth(), onClick = onNegotiate) {
                Text("Negociar / Parcelar")
            }
            TextButton(modifier = Modifier.fillMaxWidth(), onClick = onDelete) {
                Text("Excluir Registro", color = Color.Red)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettleDebtDialog(
    debt: DebtEntity,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var finalValueStr by remember { mutableStateOf(debt.original_value.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quitação Integral") },
        text = {
            Column {
                Text("Qual o valor final acordado para quitar a dívida com ${debt.creditor}?")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = finalValueStr,
                    onValueChange = { finalValueStr = it },
                    label = { Text("Valor Final (R$)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalValue = finalValueStr.toDoubleOrNull() ?: debt.original_value
                    onConfirm(finalValue)
                }
            ) {
                Text("Quitar Dívida")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NegotiateDebtBottomSheet(
    debt: DebtEntity,
    onDismiss: () -> Unit,
    onConfirm: (Double, Int) -> Unit
) {
    var totalValueStr by remember { mutableStateOf(debt.original_value.toString()) }
    var installmentsStr by remember { mutableStateOf("1") }
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
            Text("Negociar Dívida", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = totalValueStr,
                onValueChange = { totalValueStr = it },
                label = { Text("Novo Valor Total (R$)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = installmentsStr,
                onValueChange = { installmentsStr = it },
                label = { Text("Número de Parcelas") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    val total = totalValueStr.toDoubleOrNull() ?: debt.original_value
                    val installments = installmentsStr.toIntOrNull()?.coerceAtLeast(1) ?: 1
                    onConfirm(total, installments)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirmar Negociação")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
