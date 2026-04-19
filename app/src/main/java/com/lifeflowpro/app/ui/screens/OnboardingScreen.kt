package com.lifeflowpro.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    var currentPage by remember { mutableIntStateOf(0) }
    
    val pages = listOf(
        OnboardingPage("Bem-vindo!", "Sua produtividade e saúde financeira em um só lugar."),
        OnboardingPage("Finanças Reais", "Saldo real vs previsto. Visualize o amanhã hoje."),
        OnboardingPage("Dívidas sob Controle", "Cadastre, negocie e sinta o prazer de quitar tudo.")
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = pages[currentPage].title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = pages[currentPage].description,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = {
                if (currentPage < pages.size - 1) {
                    currentPage++
                } else {
                    onFinished()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (currentPage < pages.size - 1) "Próximo" else "Começar")
        }
    }
}

data class OnboardingPage(val title: String, val description: String)
