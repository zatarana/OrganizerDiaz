package com.lifeflowpro.app.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeflowpro.app.ui.screens.dashboard.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    
    // Simplification for the prototype: Integrated timeline of tasks and transactions
    val items = (state.upcomingTasks.map { TimelineItem.Task(it.title, it.due_date ?: 0L) } + 
                state.recentTransactions.map { TimelineItem.Transaction(it.description ?: "Transação", it.expected_date, it.type, it.expected_value) })
                .sortedBy { it.timestamp }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Calendário e Cronograma") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Mini header of week
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                // Dummy calendar week view
                (0..6).forEach { i ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(listOf("S", "T", "Q", "Q", "S", "S", "D")[i], fontSize = 10.sp, color = Color.Gray)
                        Text("${20 + i}", fontWeight = FontWeight.Bold, color = if (i == 0) MaterialTheme.colorScheme.primary else Color.Black)
                    }
                }
            }
            
            Divider()

            if (items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum evento agendado", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    items(items) { item ->
                        TimelineRow(item)
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineRow(item: TimelineItem) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Column(modifier = Modifier.width(50.dp), horizontalAlignment = Alignment.End) {
            Text(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(item.timestamp)), fontSize = 12.sp, color = Color.Gray)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Box(modifier = Modifier.width(2.dp).height(40.dp).background(Color.LightGray))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                item.title,
                fontWeight = FontWeight.Bold,
                color = if (item is TimelineItem.Transaction) {
                    if (item.type == "INCOME") Color(0xFF10B981) else Color.Red
                } else Color.Black
            )
            Text(if (item is TimelineItem.Task) "Tarefa" else "Transação", fontSize = 11.sp, color = Color.Gray)
        }
    }
}

sealed class TimelineItem(val title: String, val timestamp: Long) {
    class Task(title: String, timestamp: Long) : TimelineItem(title, timestamp)
    class Transaction(title: String, timestamp: Long, val type: String, val value: Double) : TimelineItem(title, timestamp)
}
