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
    var taskToLink by remember { mutableStateOf<TaskEntity?>(null) }
    
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
                            onComplete = { 
                                if (task.linked_transaction_id == -1L) {
                                    taskToLink = task
                                } else {
                                    viewModel.completeTask(task) 
                                }
                            },
                            onDelete = { viewModel.deleteTask(task) }
                        )
                    }
                }
            }
        }

        if (taskToLink != null) {
            TaskTransactionDialog(
                task = taskToLink!!,
                onDismiss = {
                    // Complete without creating transaction and unset link flag
                    viewModel.completeTask(taskToLink!!.copy(linked_transaction_id = null))
                    taskToLink = null
                },
                onConfirm = { value, type ->
                    // Set task to completed, then create transaction
                    viewModel.completeTask(taskToLink!!)
                    viewModel.createLinkedTransaction(taskToLink!!, value, type)
                    taskToLink = null
                }
            )
        }

        if (showAddSheet) {
            AddTaskBottomSheet(
                onDismiss = { showAddSheet = false },
                onSave = { newTask ->
                    viewModel.addTask(newTask)
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
fun AddTaskBottomSheet(onDismiss: () -> Unit, onSave: (TaskEntity) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("MEDIA") }
    var recurrence by remember { mutableStateOf("NENHUMA") }
    var linkTransaction by remember { mutableStateOf(false) }
    
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        LazyColumn(modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth()) {
            item {
                Text("Nova Tarefa", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("O que precisa ser feito?") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
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
                Spacer(modifier = Modifier.height(16.dp))
                Text("Recorrência", fontSize = 14.sp, color = Color.Gray)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("NENHUMA", "DIARIA", "SEMANAL", "MENSAL").forEach { r ->
                        FilterChip(
                            selected = recurrence == r,
                            onClick = { recurrence = r },
                            label = { Text(r.take(3)) } // Shortened label
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = linkTransaction, onCheckedChange = { linkTransaction = it })
                    Text("Gerar transação financeira ao concluir")
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { 
                        if (title.isNotBlank()) {
                            onSave(
                                TaskEntity(
                                    title = title,
                                    description = description.takeIf { it.isNotBlank() },
                                    category_id = null,
                                    status = "PENDENTE",
                                    due_date = System.currentTimeMillis() + 3600000, // Demostration: 1hr later
                                    due_time = null,
                                    recurrence_type = recurrence,
                                    recurrence_config = null,
                                    priority = priority,
                                    parent_task_id = null,
                                    linked_transaction_id = if (linkTransaction) -1L else null // -1 indicates intent to create
                                )
                            )
                        } 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = title.isNotBlank()
                ) {
                    Text("Salvar Tarefa")
                }
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskTransactionDialog(
    task: TaskEntity,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var valueStr by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("EXPENSE") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tarefa Financeira") },
        text = {
            Column {
                Text("Você deseja criar uma transação correspondente para '${task.title}'?")
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
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = valueStr,
                    onValueChange = { valueStr = it },
                    label = { Text("Valor (R$)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalValue = valueStr.toDoubleOrNull() ?: 0.0
                    onConfirm(finalValue, type)
                },
                enabled = valueStr.isNotBlank()
            ) {
                Text("Criar e Concluir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Apenas Concluir") }
        }
    )
}
