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

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(16.dp)) {
                Text("Dívidas", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                val openDebtSum = debts.filter { it.status != "QUITADA" }.sumOf { it.negotiated_value ?: it.original_value }
                Text(
                    "Você deve um total de ${NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(openDebtSum)}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
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
                0 -> DebtList(debts.filter { it.status == "EM_ABERTO" }, viewModel)
                1 -> DebtList(debts.filter { it.status == "EM_PAGAMENTO" }, viewModel)
                2 -> DebtList(debts.filter { it.status == "QUITADA" }, viewModel)
            }
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
fun DebtList(debts: List<DebtEntity>, viewModel: DebtViewModel) {
    if (debts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nenhum registro encontrado", color = Color.Gray)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(debts) { debt ->
                DebtItem(debt)
            }
        }
    }
}

@Composable
fun DebtItem(debt: DebtEntity) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1fr)) {
                Text(debt.creditor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Desde ${formatDate(debt.origin_date)}", fontSize = 12.sp, color = Color.Gray)
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
            IconButton(onClick = { /* Actions */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Mais")
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
