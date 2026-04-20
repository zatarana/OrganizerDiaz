package com.lifeflowpro.app.ui.screens.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
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
import com.lifeflowpro.app.ui.components.FinancialTrendChart
import com.lifeflowpro.app.ui.screens.finance.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(financeViewModel: FinanceViewModel = hiltViewModel()) {
    val summary by financeViewModel.financialSummary.collectAsState()
    val transactions by financeViewModel.transactions.collectAsState()

    // Aggregate daily data for chart
    val dailyData = transactions.groupBy { 
        val date = java.util.Date(it.expected_date)
        java.text.SimpleDateFormat("dd", java.util.Locale.getDefault()).format(date)
    }.mapValues { entry -> 
        entry.value.sumOf { if (it.type == "INCOME") it.expected_value else -it.expected_value }
    }.toSortedMap().values.toList().ifEmpty { listOf(0.0, 10.0, 5.0, 15.0, 12.0) } // Mock data if empty

    Scaffold(
        topBar = { TopAppBar(title = { Text("Relatórios e Insights") }) }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            item {
                Text("Evolução Mensal", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                FinancialTrendChart(dailyData.map { it.toDouble() })
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                SectionHeader("Economia Gerada")
                EconomyCard(transactions.sumOf { it.economy })
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                SectionHeader("Distribuição por Categoria")
                // Simplified text list for demo
                Text("Alimentação: 35%", color = Color.Gray)
                Text("Transporte: 15%", color = Color.Gray)
                Text("Lazer: 20%", color = Color.Gray)
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
fun EconomyCard(amount: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF166534))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Total Economizado", fontSize = 12.sp, color = Color(0xFF166534))
                Text(
                    java.text.NumberFormat.getCurrencyInstance(java.util.Locale("pt", "BR")).format(amount),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF166534)
                )
            }
        }
    }
}
