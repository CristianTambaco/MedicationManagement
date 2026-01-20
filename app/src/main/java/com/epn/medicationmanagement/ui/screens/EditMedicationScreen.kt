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
import com.epn.medicationmanagement.model.entity.MedicationWithSchedules

/**
 * Pantalla para editar un medicamento existente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMedicationScreen(
    medicationId: Long,
    controller: MedicationController,
    onNavigateBack: () -> Unit
) {
    var medicationWithSchedules by remember { mutableStateOf<MedicationWithSchedules?>(null) }
    
    var nombre by remember { mutableStateOf("") }
    var dosis by remember { mutableStateOf("") }
    var unidad by remember { mutableStateOf("") }
    var instrucciones by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }
    var activo by remember { mutableStateOf(true) }
    
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingData by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scrollState = rememberScrollState()
    
    // Cargar datos del medicamento
    LaunchedEffect(medicationId) {
        controller.getMedicationById(medicationId) { result ->
            medicationWithSchedules = result
            result?.medication?.let { med ->
                nombre = med.nombre
                dosis = med.dosis ?: ""
                unidad = med.unidad ?: ""
                instrucciones = med.instrucciones ?: ""
                notas = med.notas ?: ""
                activo = med.activo
            }
            isLoadingData = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Medicamento") },
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
            if (!isLoadingData) {
                ExtendedFloatingActionButton(
                    onClick = {
                        if (nombre.isBlank()) {
                            errorMessage = "El nombre del medicamento es obligatorio"
                            return@ExtendedFloatingActionButton
                        }
                        
                        medicationWithSchedules?.medication?.let { originalMed ->
                            isLoading = true
                            val updatedMedication = originalMed.copy(
                                nombre = nombre.trim(),
                                dosis = dosis.takeIf { it.isNotBlank() },
                                unidad = unidad.takeIf { it.isNotBlank() },
                                instrucciones = instrucciones.takeIf { it.isNotBlank() },
                                notas = notas.takeIf { it.isNotBlank() },
                                activo = activo
                            )
                            
                            controller.updateMedication(
                                medication = updatedMedication,
                                onSuccess = { 
                                    isLoading = false
                                    onNavigateBack() 
                                },
                                onError = { error ->
                                    isLoading = false
                                    errorMessage = error
                                }
                            )
                        }
                    },
                    icon = { 
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null)
                        }
                    },
                    text = { Text("Guardar") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        if (isLoadingData) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
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
                
                // Estado activo/inactivo
                Card {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Medicamento activo",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = if (activo) "Recibirás recordatorios" else "No recibirás recordatorios",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = activo,
                            onCheckedChange = { activo = it }
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
                    label = { Text("Nombre del medicamento *") },
                    placeholder = { Text("Ej: Paracetamol") },
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
                        label = { Text("Dosis") },
                        placeholder = { Text("Ej: 500") },
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
                    placeholder = { Text("Ej: Tomar con comida") },
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
                    label = { Text("Notas adicionales") },
                    placeholder = { Text("Ej: Recetado por Dr. García") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    )
                )
                
                // Espacio para el FAB
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
