package com.lifeflowpro.app.ui.screens.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeflowpro.app.data.db.entities.TransactionEntity
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(viewModel: FinanceViewModel = hiltViewModel()) {
    val summary by viewModel.financialSummary.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddTxSheet by remember { mutableStateOf(false) }

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
                0 -> TransactionList(transactions)
                1 -> BudgetList()
                2 -> GoalList()
            }
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
fun TransactionList(transactions: List<TransactionEntity>) {
    if (transactions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nenhuma transação este mês", color = Color.Gray)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(transactions) { tx ->
                TransactionItem(tx)
            }
        }
    }
}

@Composable
fun TransactionItem(tx: TransactionEntity) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    Card(
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
fun BudgetList() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Gerenciamento de Orçamentos em breve")
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
