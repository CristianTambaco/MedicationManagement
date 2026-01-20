package com.epn.medicationmanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.epn.medicationmanagement.controller.MedicationController
import com.epn.medicationmanagement.model.entity.MedicationWithSchedules
import com.epn.medicationmanagement.ui.components.ScheduleItem
import com.epn.medicationmanagement.ui.components.TimePickerDialog

/**
 * Pantalla que muestra los detalles de un medicamento y permite agregar horarios.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationDetailScreen(
    medicationId: Long,
    controller: MedicationController,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit
) {
    var medicationWithSchedules by remember { mutableStateOf<MedicationWithSchedules?>(null) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var showDeleteScheduleDialog by remember { mutableStateOf(false) }
    var scheduleToDelete by remember { mutableStateOf<com.epn.medicationmanagement.model.entity.MedicationSchedule?>(null) }
    
    // Cargar datos del medicamento
    LaunchedEffect(medicationId) {
        controller.getMedicationById(medicationId) { result ->
            medicationWithSchedules = result
        }
    }
    
    // Observar cambios en el medicamento seleccionado
    val selectedMedication by controller.selectedMedication.collectAsState()
    LaunchedEffect(selectedMedication) {
        if (selectedMedication?.medication?.id == medicationId) {
            medicationWithSchedules = selectedMedication
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = medicationWithSchedules?.medication?.nombre ?: "Cargando...",
                        maxLines = 1
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar"
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
                onClick = { showTimePickerDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Agregar horario") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { paddingValues ->
        medicationWithSchedules?.let { mws ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Información del medicamento
                item {
                    MedicationInfoCard(mws)
                }
                
                // Sección de horarios
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Schedule,
//                            contentDescription = null,
//                            tint = MaterialTheme.colorScheme.primary
//                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recordatorio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                if (mws.schedules.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
//                                Icon(
//                                    imageVector = Icons.Default.Schedule,
//                                    contentDescription = null,
//                                    modifier = Modifier.size(48.dp),
//                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
//                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Sin registro",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
//                                Text(
//                                    text = "Agrega un horario para recibir recordatorios",
//                                    style = MaterialTheme.typography.bodySmall,
//                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
//                                )
                            }
                        }
                    }
                } else {
                    items(
                        items = mws.schedules,
                        key = { it.id }
                    ) { schedule ->
                        ScheduleItem(
                            schedule = schedule,
                            onToggle = {
                                controller.toggleScheduleActive(schedule)
                                // Recargar datos
                                controller.getMedicationById(medicationId) { result ->
                                    medicationWithSchedules = result
                                }
                            },
                            onDelete = {
                                scheduleToDelete = schedule
                                showDeleteScheduleDialog = true
                            }
                        )
                    }
                }
                
                // Espacio para el FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        } ?: run {
            // Estado de carga
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
    
    // Diálogo de selección de hora
    if (showTimePickerDialog) {
        TimePickerDialog(
            onDismiss = { showTimePickerDialog = false },
            onConfirm = { horaEnMinutos, diasSemana ->
                controller.addSchedule(
                    medicationId = medicationId,
                    horaEnMinutos = horaEnMinutos,
                    diasSemana = diasSemana,
                    onSuccess = {
                        // Recargar datos
                        controller.getMedicationById(medicationId) { result ->
                            medicationWithSchedules = result
                        }
                    }
                )
                showTimePickerDialog = false
            }
        )
    }
    
    // Diálogo de confirmación de eliminación de horario
    if (showDeleteScheduleDialog && scheduleToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteScheduleDialog = false
                scheduleToDelete = null
            },
            title = { Text("Eliminar horario") },
            text = { 
                Text("¿Estás seguro de que deseas eliminar el horario de las ${scheduleToDelete?.getFormattedTime()}?") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scheduleToDelete?.let { schedule ->
                            controller.deleteSchedule(
                                schedule = schedule,
                                onSuccess = {
                                    // Recargar datos
                                    controller.getMedicationById(medicationId) { result ->
                                        medicationWithSchedules = result
                                    }
                                }
                            )
                        }
                        showDeleteScheduleDialog = false
                        scheduleToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDeleteScheduleDialog = false
                        scheduleToDelete = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun MedicationInfoCard(mws: MedicationWithSchedules) {
    val medication = mws.medication
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
//                Icon(
//                    imageVector = Icons.Default.Info,
//                    contentDescription = null,
//                    tint = MaterialTheme.colorScheme.primary
//                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Detalles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            HorizontalDivider()
            
            if (!medication.dosis.isNullOrBlank() || !medication.unidad.isNullOrBlank()) {
                InfoRow(
                    label = "Dosis medicamento",
                    value = buildString {
                        append(medication.dosis ?: "")
                        if (!medication.unidad.isNullOrBlank()) {
                            if (isNotEmpty()) append(" ")
                            append(medication.unidad)
                        }
                    }.ifEmpty { "No especificada" }
                )
            }
            
            if (!medication.instrucciones.isNullOrBlank()) {
                InfoRow(
                    label = "Instrucciones",
                    value = medication.instrucciones
                )
            }
            
            if (!medication.notas.isNullOrBlank()) {
                InfoRow(
                    label = "Información adicional",
                    value = medication.notas
                )
            }
            
            if (medication.dosis.isNullOrBlank() && 
                medication.instrucciones.isNullOrBlank() && 
                medication.notas.isNullOrBlank()) {
                Text(
                    text = "No hay información adicional registrada",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
