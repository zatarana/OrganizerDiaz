package com.lifeflowpro.app.ui.screens.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeflowpro.app.data.db.entities.TaskEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(viewModel: TaskViewModel = hiltViewModel()) {
    val tasks by viewModel.tasks.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = { TopAppBar(title = { Text("Minhas Tarefas") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nova Tarefa")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (tasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhuma tarefa cadastrada", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    items(tasks) { task ->
                        TaskItem(
                            task = task,
                            onComplete = { viewModel.completeTask(task) },
                            onDelete = { viewModel.deleteTask(task) }
                        )
                    }
                }
            }
        }

        if (showAddSheet) {
            AddTaskBottomSheet(
                onDismiss = { showAddSheet = false },
                onSave = { title, priority ->
                    viewModel.addTask(
                        TaskEntity(
                            title = title,
                            description = "",
                            category_id = null,
                            status = "PENDENTE",
                            due_date = System.currentTimeMillis() + 3600000, // 1 hour from now for demo
                            due_time = null,
                            recurrence_type = "NENHUMA",
                            recurrence_config = null,
                            priority = priority,
                            parent_task_id = null,
                            linked_transaction_id = null
                        )
                    )
                    showAddSheet = false
                }
            )
        }
    }
}

@Composable
fun TaskItem(task: TaskEntity, onComplete: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.status == "CONCLUIDA") Color(0xFFF1F5F9) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.status == "CONCLUIDA",
                onCheckedChange = { if (it) onComplete() }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (task.status == "CONCLUIDA") Color.Gray else Color.Unspecified
                )
                if (task.priority != "MEDIA") {
                    Text(
                        text = "Prioridade ${task.priority}",
                        fontSize = 12.sp,
                        color = if (task.priority == "ALTA") Color.Red else Color.Blue
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color.Gray)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskBottomSheet(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("MEDIA") }
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
            Text("Nova Tarefa", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("O que precisa ser feito?") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Prioridade", fontSize = 14.sp, color = Color.Gray)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("BAIXA", "MEDIA", "ALTA").forEach { p ->
                    FilterChip(
                        selected = priority == p,
                        onClick = { priority = p },
                        label = { Text(p) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { if (title.isNotBlank()) onSave(title, priority) },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank()
            ) {
                Text("Salvar Tarefa")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
