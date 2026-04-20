package com.lifeflowpro.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeflowpro.app.data.backup.BackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val backupManager: BackupManager
) : ViewModel() {
    
    var lastOutput by mutableStateOf("")

    fun export() {
        viewModelScope.launch {
            lastOutput = backupManager.exportBackup()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = androidx.hilt.navigation.compose.hiltViewModel(), navController: androidx.navigation.NavHostController? = null) {
    var showExportDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Menu Mais") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Text("Módulos", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            
            SettingsItem(
                title = "Calendário Integrado",
                subtitle = "Visão geral de seus vencimentos",
                icon = androidx.compose.material.icons.Icons.Default.DateRange,
                onClick = { navController?.navigate("calendar") }
            )

            Spacer(modifier = Modifier.height(32.dp))
            Text("Dados e Backup", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            
            SettingsItem(
                title = "Exportar Backup JSON",
                subtitle = "Gera um arquivo com todos os seus dados locais",
                icon = Icons.Default.ArrowForward,
                onClick = { 
                    viewModel.export()
                    showExportDialog = true
                }
            )

            SettingsItem(
                title = "Importar Backup",
                subtitle = "Restaurar dados de um arquivo JSON",
                icon = Icons.Default.ArrowBack,
                onClick = { /* File picker logic */ }
            )

            Spacer(modifier = Modifier.height(32.dp))
            Text("Sobre o App", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            
            SettingsItem(
                title = "Versão",
                subtitle = "1.0.0 (Build Final)",
                icon = Icons.Default.Info,
                onClick = { }
            )
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            confirmButton = { TextButton(onClick = { showExportDialog = false }) { Text("Fechar") } },
            title = { Text("Backup Gerado") },
            text = { 
                Column {
                    Text("O JSON de backup foi gerado com sucesso.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        viewModel.lastOutput.take(100) + "...",
                        style = MaterialTheme.typography.bodySmall,
                        color = androidx.compose.ui.graphics.Color.Gray
                    )
                }
            }
        )
    }
}

@Composable
fun SettingsItem(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Medium)
                Text(subtitle, fontSize = 12.sp, color = androidx.compose.ui.graphics.Color.Gray)
            }
        }
    }
}
