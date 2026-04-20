package com.lifeflowpro.app.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: CalendarViewModel = hiltViewModel(), navController: androidx.navigation.NavHostController? = null) {
    val events by viewModel.events.collectAsState()
    
    // Manage current month/year selection
    val cal = Calendar.getInstance()
    var currentMonth by remember { mutableStateOf(cal.get(Calendar.MONTH)) }
    var currentYear by remember { mutableStateOf(cal.get(Calendar.YEAR)) }
    
    var selectedDate by remember { mutableStateOf<Date?>(cal.time) }

    val daysInMonth = getDaysForMonth(currentYear, currentMonth)

    Scaffold(
        topBar = { TopAppBar(title = { Text("Calendário", fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            
            // Month Switcher
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { 
                    if (currentMonth == 0) {
                        currentMonth = 11
                        currentYear--
                    } else {
                        currentMonth--
                    }
                }) { Text("< Mês Ant.") }
                
                Text(
                    text = "${getMonthName(currentMonth)} $currentYear",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                
                TextButton(onClick = { 
                    if (currentMonth == 11) {
                        currentMonth = 0
                        currentYear++
                    } else {
                        currentMonth++
                    }
                }) { Text("Próx. >") }
            }
            
            // Days of Week Header
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sab").forEach { dayName ->
                    Text(
                        text = dayName,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Calendar Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                items(daysInMonth) { date ->
                    if (date == null) {
                        Spacer(modifier = Modifier.size(48.dp))
                    } else {
                        val isSelected = selectedDate != null && isSameDay(date, selectedDate!!)
                        val dayEvents = events.filter { isSameDay(Date(it.date), date) }
                        
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .padding(4.dp)
                                .background(
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedDate = date },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val c = Calendar.getInstance()
                                c.time = date
                                Text(
                                    text = c.get(Calendar.DAY_OF_MONTH).toString(),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color.Black
                                )
                                
                                // Dots for events
                                if (dayEvents.isNotEmpty()) {
                                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(top = 2.dp)) {
                                        dayEvents.map { it.type }.distinct().take(4).forEach { eventType ->
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .padding(horizontal = 1.dp)
                                                    .background(eventType.color, CircleShape)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            
            // Selected Day Events List
            if (selectedDate != null) {
                val selectedDayEvents = events.filter { isSameDay(Date(it.date), selectedDate!!) }
                
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
                Text(
                    text = "Eventos em ${sdf.format(selectedDate!!)}",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
                
                if (selectedDayEvents.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("Nenhum evento neste dia", color = Color.Gray)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                        items(selectedDayEvents) { event ->
                            CalendarEventItem(event, onClick = {
                                when(event.type) {
                                    EventType.TASK -> navController?.navigate("tasks")
                                    EventType.EXPENSE, EventType.INCOME -> navController?.navigate("finance")
                                    EventType.DEBT_INSTALLMENT -> navController?.navigate("debts")
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarEventItem(event: CalendarEvent, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(event.type.color, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(event.title, fontWeight = FontWeight.Medium)
                val typeLabel = when (event.type) {
                    EventType.TASK -> "Tarefa"
                    EventType.EXPENSE -> "Despesa"
                    EventType.DEBT_INSTALLMENT -> "Parcela de Dívida"
                    EventType.INCOME -> "Receita"
                }
                Text(typeLabel, fontSize = 12.sp, color = Color.Gray)
            }
            if (event.value != null) {
                Text(
                    text = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(event.value),
                    fontWeight = FontWeight.Bold,
                    color = event.type.color
                )
            }
        }
    }
}

fun isSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance()
    cal1.time = date1
    val cal2 = Calendar.getInstance()
    cal2.time = date2
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun getDaysForMonth(year: Int, month: Int): List<Date?> {
    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, year)
    cal.set(Calendar.MONTH, month)
    cal.set(Calendar.DAY_OF_MONTH, 1)
    
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    val days = mutableListOf<Date?>()
    for (i in 0 until firstDayOfWeek) {
        days.add(null)
    }
    
    for (i in 1..daysInMonth) {
        cal.set(Calendar.DAY_OF_MONTH, i)
        days.add(cal.time)
    }
    return days
}

fun getMonthName(month: Int): String {
    val months = listOf("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro")
    return months[month]
}
