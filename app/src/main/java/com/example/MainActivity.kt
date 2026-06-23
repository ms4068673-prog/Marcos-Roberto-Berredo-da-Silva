package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                EduAptaApp()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EduAptaApp() {
    val transtornos = listOf("Autismo", "TDAH", "Dislexia", "DI", "Outro")
    var transtorno by remember { mutableStateOf(transtornos[0]) }

    val disciplinas = listOf("Matemática", "Português")
    var disciplina by remember { mutableStateOf(disciplinas[0]) }

    val anos = (1..9).map { "${it}º Ano" }
    var anoEscolar by remember { mutableStateOf(anos[0]) }
    var expandedAno by remember { mutableStateOf(false) }

    var dominaLeitura by remember { mutableStateOf(false) }
    var conteudo by remember { mutableStateOf("") }
    var generatedMaterial by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                NavigationBarItem(selected = true, onClick = {}, icon = { Text("🏠") }, label = { Text("Início") })
                NavigationBarItem(selected = false, onClick = {}, icon = { Text("📁") }, label = { Text("Arquivos") })
                NavigationBarItem(selected = false, onClick = {}, icon = { Text("⚙️") }, label = { Text("Ajustes") })
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("AdaptEdu AI", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Text("Especialista IA", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.secondary, CircleShape), contentAlignment = Alignment.Center) {
                    Text("DR", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
            
            // Form Card
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("1. Perfil do Aluno", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    
                    Text("Transtorno/Dificuldade", style = MaterialTheme.typography.bodySmall)
                    FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        transtornos.forEach { item ->
                            FilterChip(
                                selected = transtorno == item,
                                onClick = { transtorno = item },
                                label = { Text(item) }
                            )
                        }
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Domina a leitura?", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.width(8.dp))
                        Switch(checked = dominaLeitura, onCheckedChange = { dominaLeitura = it })
                    }
                    
                    Text("Disciplina", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        disciplinas.forEach { item ->
                            FilterChip(
                                selected = disciplina == item,
                                onClick = { disciplina = item },
                                label = { Text(item) }
                            )
                        }
                    }
                    
                    Text("Ano Escolar", style = MaterialTheme.typography.bodySmall)
                    ExposedDropdownMenuBox(
                        expanded = expandedAno,
                        onExpandedChange = { expandedAno = !expandedAno }
                    ) {
                        OutlinedTextField(
                            value = anoEscolar,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAno) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedAno,
                            onDismissRequest = { expandedAno = false }
                        ) {
                            anos.forEach { ano ->
                                DropdownMenuItem(
                                    text = { Text(ano) },
                                    onClick = { anoEscolar = ano; expandedAno = false }
                                )
                            }
                        }
                    }

                    OutlinedTextField(value = conteudo, onValueChange = { conteudo = it }, label = { Text("Conteúdo BNCC") }, modifier = Modifier.fillMaxWidth())
                }
            }

            // Download/Generate Button
            Button(
                onClick = {
                    isLoading = true
                    scope.launch {
                        val prompt = "Crie um material adaptado para $transtorno, focado em $conteudo para aluno do $anoEscolar em $disciplina. O aluno ${if (dominaLeitura) "domina" else "não domina"} a leitura. Formate como HTML para Word (use tags <h1>, <p>, <ul>, <li>). Inclua o resumo do conteúdo e 5 exercícios, com descrição detalhada de ilustrações entre colchetes."
                        generatedMaterial = GeminiRepository.generateMaterial(prompt)
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text("💾 Baixar em Word (.docx)", fontWeight = FontWeight.Bold)
            }
            
            generatedMaterial?.let {
                Text("Resultado:", style = MaterialTheme.typography.titleMedium)
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Text(text = it, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}
