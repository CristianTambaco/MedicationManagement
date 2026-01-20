package com.epn.medicationmanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.epn.medicationmanagement.controller.MedicationController

/**
 * Pantalla para agregar un nuevo medicamento.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    controller: MedicationController,
    onNavigateBack: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var dosis by remember { mutableStateOf("") }
    var unidad by remember { mutableStateOf("") }
    var instrucciones by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medicamento") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (nombre.isBlank()) {
                        errorMessage = "El nombre del medicamento es obligatorio"
                        return@ExtendedFloatingActionButton
                    }
                    
                    isLoading = true
                    controller.addMedication(
                        nombre = nombre,
                        dosis = dosis,
                        unidad = unidad,
                        instrucciones = instrucciones,
                        notas = notas,
                        onSuccess = { 
                            isLoading = false
                            onNavigateBack() 
                        },
                        onError = { error ->
                            isLoading = false
                            errorMessage = error
                        }
                    )
                },
                icon = { 
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
//                        Icon(Icons.Default.Save, contentDescription = null)
                    }
                },
                text = { Text("Guardar") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error message
            errorMessage?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            // Nombre (obligatorio)
            OutlinedTextField(
                value = nombre,
                onValueChange = { 
                    nombre = it
                    errorMessage = null
                },
                label = { Text("Medicamento ") },
                placeholder = { Text("Ej: Naproxeno") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                isError = errorMessage != null && nombre.isBlank()
            )
            
            // Dosis y Unidad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = dosis,
                    onValueChange = { dosis = it },
                    label = { Text("Dosis medicamento") },
                    placeholder = { Text("Ej: 20") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    )
                )
                
                OutlinedTextField(
                    value = unidad,
                    onValueChange = { unidad = it },
                    label = { Text("Unidad") },
                    placeholder = { Text("Ej: mg") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    )
                )
            }
            
            // Instrucciones
            OutlinedTextField(
                value = instrucciones,
                onValueChange = { instrucciones = it },
                label = { Text("Instrucciones") },
                placeholder = { Text("Ej: Después de la merienda") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                )
            )
            
            // Notas
            OutlinedTextField(
                value = notas,
                onValueChange = { notas = it },
                label = { Text("Información adicional") },
                placeholder = { Text("Ej: Ninguno") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                )
            )
            
            // Información adicional
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
//                    Text(
//                        text = "ℹ️ Información",
//                        style = MaterialTheme.typography.titleSmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
                    Spacer(modifier = Modifier.height(8.dp))
//                    Text(
//                        text = "Después de guardar el medicamento, podrás agregar los horarios de recordatorio desde la pantalla de detalles.",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
//                    )
                }
            }
            
            // Espacio para el FAB
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
