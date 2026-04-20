package com.lifeflowpro.app.ui.screens.reports

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeflowpro.app.data.backup.BackupManager
import com.lifeflowpro.app.ui.components.DonutChart
import com.lifeflowpro.app.ui.components.HorizontalBarChart
import com.lifeflowpro.app.ui.components.PieChart
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val financeReport by viewModel.financeReport.collectAsState()
    val taskReport by viewModel.taskReport.collectAsState()
    val budgetReport by viewModel.budgetReport.collectAsState()
    val categories by viewModel.categories.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Relatórios", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                // Feature 11.4 - Exportação (SAF/Share)
                // In a real implementation this would generate CSV/PDF and use ACTION_CREATE_DOCUMENT
                // For MVP, we invoke simple sharing of a generated text summary.
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Relatório Mensal")
                    putExtra(Intent.EXTRA_TEXT, "Exportação Básica gerada pelo LifeFlowPro.\nSaldo: R$ ${financeReport?.finalBalance ?: 0.0}")
                }
                context.startActivity(Intent.createChooser(shareIntent, "Exportar Relatório"))
            }) {
                Icon(Icons.Default.Share, "Exportar")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Finanças") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Orçamentos") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Tarefas") })
            }

            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                when (selectedTab) {
                    0 -> {
                        financeReport?.let { report ->
                            item { FinanceReportView(report, categories) }
                        } ?: item { Text("Carregando Dados...", Modifier.padding(16.dp)) }
                    }
                    1 -> {
                        item { BudgetReportView(budgetReport) }
                    }
                    2 -> {
                        taskReport?.let { report ->
                            item { TaskReportView(report) }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) } // Padding for FAB
            }
        }
    }
}

@Composable
fun FinanceReportView(report: MonthlyFinanceReport, categories: List<com.lifeflowpro.app.data.db.entities.CategoryEntity>) {
    val fmt = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    
    // Cards
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        ReportCard("Receitas", fmt.format(report.totalIncome), Color(0xFF10B981), Modifier.weight(1f))
        Spacer(modifier = Modifier.width(8.dp))
        ReportCard("Despesas", fmt.format(report.totalExpense), Color.Red, Modifier.weight(1f))
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        ReportCard("Saldo Atual", fmt.format(report.finalBalance), Color.Black, Modifier.weight(1f))
        Spacer(modifier = Modifier.width(8.dp))
        ReportCard("Economia", fmt.format(report.totalEconomy), Color(0xFF3B82F6), Modifier.weight(1f))
    }

    Spacer(modifier = Modifier.height(32.dp))
    Text("Despesas por Categoria", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Spacer(modifier = Modifier.height(16.dp))

    // Pie Chart
    val pieData = report.expensesByCategory.mapKeys { entry -> 
        categories.find { it.id == entry.key }?.name ?: "Outros" 
    }
    
    if (pieData.isNotEmpty()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PieChart(
                data = pieData,
                colors = listOf(Color(0xFF3B82F6), Color(0xFFEF4444), Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFF8B5CF6)),
                modifier = Modifier.size(150.dp)
            )
            Spacer(modifier = Modifier.width(24.dp))
            Column {
                val colors = listOf(Color(0xFF3B82F6), Color(0xFFEF4444), Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFF8B5CF6))
                pieData.entries.forEachIndexed { index, prop ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        Box(modifier = Modifier.size(12.dp).background(colors[index % colors.size], RoundedCornerShape(2.dp)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(prop.key, fontSize = 14.sp)
                    }
                }
            }
        }
    } else {
        Text("Sem despesas registradas.", color = Color.Gray)
    }
}

@Composable
fun BudgetReportView(budgets: List<BudgetReportItem>) {
    Text("Desempenho dos Orçamentos", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Spacer(modifier = Modifier.height(16.dp))

    if (budgets.isEmpty()) {
        Text("Nenhum orçamento configurado.", color = Color.Gray)
        return
    }

    budgets.forEach { item ->
        val limit = item.budget.planned_value
        val spent = item.spent
        val percentage = if (limit > 0) (spent / limit) * 100 else 0.0
        val isOver = percentage > 100
        val fmt = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(item.categoryName, fontWeight = FontWeight.Bold)
                    Text("${percentage.toInt()}%", color = if (isOver) Color.Red else Color(0xFF10B981), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalBarChart(
                    spent = spent.toFloat(),
                    limit = limit.toFloat(),
                    color = if (isOver) Color.Red else Color(0xFF3B82F6),
                    modifier = Modifier.fillMaxWidth().height(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Gasto: ${fmt.format(spent)}", fontSize = 12.sp, color = Color.Gray)
                    Text("Limite: ${fmt.format(limit)}", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun TaskReportView(report: TaskReport) {
    Text("Produtividade do Mês", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Spacer(modifier = Modifier.height(16.dp))
    
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(150.dp)) {
            DonutChart(
                percentage = report.completionRate,
                color = if (report.completionRate > 0.5f) Color(0xFF10B981) else Color(0xFFF59E0B),
                modifier = Modifier.matchParentSize().padding(16.dp)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${(report.completionRate * 100).toInt()}%", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Concluídas", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }

    Spacer(modifier = Modifier.height(32.dp))
    
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(report.totalTasks.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text("Total", fontSize = 14.sp, color = Color.Gray)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(report.completedTasks.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
            Text("Realizadas", fontSize = 14.sp, color = Color.Gray)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(report.overdueTasks.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Red)
            Text("Atrasadas", fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ReportCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(90.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Text(title, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
