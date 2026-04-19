package com.lifeflowpro.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeflowpro.app.data.db.entities.TaskEntity
import com.lifeflowpro.app.data.db.entities.TransactionEntity
import com.lifeflowpro.app.ui.screens.finance.BalanceHeader
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumo do Dia", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { /* Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Pesquisar")
                    }
                    IconButton(onClick = { /* Notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificações")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                FinancialSummaryCard(state)
            }

            item {
                SectionHeader("Próximas Tarefas")
            }

            if (state.upcomingTasks.isEmpty()) {
                item { EmptyStateMessage("Nada pendente para agora") }
            } else {
                items(state.upcomingTasks) { task ->
                    DashboardTaskItem(task)
                }
            }

            item {
                SectionHeader("Dívidas Urgentes")
            }

            if (state.criticalDebts.isEmpty()) {
                item { EmptyStateMessage("Nenhuma dívida em aberto!") }
            } else {
                items(state.criticalDebts) { debt ->
                    DashboardDebtItem(debt.creditor, debt.original_value)
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun FinancialSummaryCard(state: DashboardState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            val totalReal = state.accounts.sumOf { it.initialBalance } 
            // In a real summary we'd calculate transactions too, but we reuse concepts from FinanceScreen logic
            Text("PATRIMÔNIO ESTIMADO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(
                NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(totalReal),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("SALDO REAL", fontSize = 10.sp, color = Color.Gray)
                    Text("R$ 0,00", fontSize = 14.sp, fontWeight = FontWeight.Bold) // Simplified for dashboard
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("PREVISTO", fontSize = 10.sp, color = Color.Gray)
                    Text("R$ 0,00", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

@Composable
fun DashboardTaskItem(task: TaskEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        when (task.priority) {
                            "ALTA" -> Color.Red
                            "BAIXA" -> Color.Blue
                            else -> Color.Gray
                        },
                        shape = RoundedCornerShape(4.dp)
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(task.title, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun DashboardDebtItem(creditor: String, value: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(creditor, fontWeight = FontWeight.Medium)
            Text(
                NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(value),
                fontWeight = FontWeight.Bold,
                color = Color.Red
            )
        }
    }
}

@Composable
fun EmptyStateMessage(message: String) {
    Text(message, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
}
