package com.lifeflowpro.app.ui.screens.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeflowpro.app.data.db.entities.TransactionEntity
import com.lifeflowpro.app.data.db.entities.BudgetEntity
import com.lifeflowpro.app.data.db.entities.CategoryEntity
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(viewModel: FinanceViewModel = hiltViewModel()) {
    val summary by viewModel.financialSummary.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val budgets by viewModel.budgets.collectAsState()
    val categories by viewModel.categories.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddTxSheet by remember { mutableStateOf(false) }
    var selectedTxForConfirmation by remember { mutableStateOf<TransactionEntity?>(null) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(16.dp)) {
                Text("Finanças", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                BalanceHeader(
                    realBalance = summary.realBalance,
                    predictedBalance = summary.predictedBalance
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddTxSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nova Transação")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Transações") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Orçamentos") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Metas") })
            }

            when (selectedTab) {
                0 -> TransactionList(
                    transactions = transactions,
                    onTransactionClick = { tx -> 
                        if (tx.status == "A_PAGAR" || tx.status == "A_RECEBER") {
                            selectedTxForConfirmation = tx 
                        }
                    }
                )
                1 -> BudgetList(budgets, transactions, categories)
                2 -> GoalList()
            }
        }
        
        if (selectedTxForConfirmation != null) {
            PaymentConfirmationDialog(
                transaction = selectedTxForConfirmation!!,
                onDismiss = { selectedTxForConfirmation = null },
                onConfirm = { tx, finalValue ->
                    viewModel.confirmPayment(tx, finalValue, System.currentTimeMillis())
                    selectedTxForConfirmation = null
                }
            )
        }

        if (showAddTxSheet) {
            AddTransactionBottomSheet(
                onDismiss = { showAddTxSheet = false },
                onSave = { tx -> 
                    viewModel.addTransaction(tx)
                    showAddTxSheet = false
                }
            )
        }
    }
}

@Composable
fun BalanceHeader(realBalance: Double, predictedBalance: Double) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text("Saldo Real", fontSize = 12.sp, color = Color.Gray)
            Text(
                currencyFormatter.format(realBalance),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (realBalance >= 0) Color(0xFF10B981) else Color.Red
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("Saldo Previsto", fontSize = 12.sp, color = Color.Gray)
            Text(
                currencyFormatter.format(predictedBalance),
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun TransactionList(transactions: List<TransactionEntity>, onTransactionClick: (TransactionEntity) -> Unit) {
    if (transactions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nenhuma transação este mês", color = Color.Gray)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(transactions) { tx ->
                TransactionItem(tx, onClick = { onTransactionClick(tx) })
            }
        }
    }
}

@Composable
fun TransactionItem(tx: TransactionEntity, onClick: () -> Unit) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (tx.type == "INCOME") Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                        shape = MaterialTheme.shapes.small
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(if (tx.type == "INCOME") "↓" else "↑", color = if (tx.type == "INCOME") Color(0xFF166534) else Color(0xFF991B1B))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(tx.description ?: "Sem descrição", fontWeight = FontWeight.Medium)
                Text(tx.status, fontSize = 11.sp, color = Color.Gray)
            }
            Text(
                currencyFormatter.format(tx.expected_value),
                fontWeight = FontWeight.Bold,
                color = if (tx.type == "INCOME") Color(0xFF10B981) else Color.Black
            )
        }
    }
}

@Composable
fun BudgetList(budgets: List<BudgetEntity>, transactions: List<TransactionEntity>, categories: List<CategoryEntity>) {
    if (budgets.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nenhum orçamento configurado", color = Color.Gray)
        }
    } else {
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(budgets) { budget ->
                val category = categories.find { it.id == budget.category_id }
                // Calculate spent this month for this category
                // For MVP, we sum all expenses matching the category
                val spent = transactions
                    .filter { it.type == "EXPENSE" && it.category_id == budget.category_id && it.status == "PAGO" }
                    .sumOf { it.final_value ?: it.expected_value }
                
                val progress = if (budget.planned_value > 0) (spent / budget.planned_value).toFloat().coerceIn(0f, 1f) else 1f
                val progressColor = when {
                    progress >= 1f -> Color.Red
                    progress >= 0.7f -> Color(0xFFEAB308) // Yellow
                    else -> Color(0xFF10B981) // Green
                }

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(category?.name ?: "Categoria Geral", fontWeight = FontWeight.Medium)
                            Text(
                                text = "${currencyFormatter.format(spent)} / ${currencyFormatter.format(budget.planned_value)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = progressColor,
                            strokeCap = StrokeCap.Round
                        )
                        if (progress >= 1f) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Orçamento excedido!", color = Color.Red, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GoalList() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Metas de Economia em breve")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionBottomSheet(onDismiss: () -> Unit, onSave: (TransactionEntity) -> Unit) {
    var description by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("EXPENSE") }
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
            Text("Nova Transação", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = type == "EXPENSE",
                    onClick = { type = "EXPENSE" },
                    label = { Text("Despesa") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = type == "INCOME",
                    onClick = { type = "INCOME" },
                    label = { Text("Receita") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text("Valor (R$)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descrição") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    val v = value.toDoubleOrNull() ?: 0.0
                    onSave(TransactionEntity(
                        type = type,
                        account_id = 1, // Default for now
                        category_id = null,
                        description = description,
                        expected_value = v,
                        final_value = null,
                        expected_date = System.currentTimeMillis(),
                        payment_date = null,
                        status = if (type == "INCOME") "A_RECEBER" else "A_PAGAR",
                        recurrence_type = "NENHUMA",
                        recurrence_group_id = null
                    ))
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = value.isNotBlank()
            ) {
                Text("Salvar Transação")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PaymentConfirmationDialog(
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    onConfirm: (TransactionEntity, Double) -> Unit
) {
    var finalValueStr by remember { mutableStateOf(transaction.expected_value.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (transaction.type == "INCOME") "Confirmar Recebimento" else "Confirmar Pagamento") },
        text = {
            Column {
                Text("Qual o valor final pago/recebido para: ${transaction.description ?: ""}? (Se houve juros/multa, ajuste o valor abaixo)")
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
                    val finalValue = finalValueStr.toDoubleOrNull() ?: transaction.expected_value
                    onConfirm(transaction, finalValue)
                }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
