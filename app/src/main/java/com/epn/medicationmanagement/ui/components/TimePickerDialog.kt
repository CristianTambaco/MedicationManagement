package com.epn.medicationmanagement.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.epn.medicationmanagement.model.entity.MedicationSchedule

/**
 * Diálogo para seleccionar hora y días de la semana para un recordatorio.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (horaEnMinutos: Int, diasSemana: Int) -> Unit
) {
    var selectedHour by remember { mutableIntStateOf(8) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var showTimePicker by remember { mutableStateOf(true) }
    
    // Estado de los días seleccionados
    var lunes by remember { mutableStateOf(true) }
    var martes by remember { mutableStateOf(true) }
    var miercoles by remember { mutableStateOf(true) }
    var jueves by remember { mutableStateOf(true) }
    var viernes by remember { mutableStateOf(true) }
    var sabado by remember { mutableStateOf(true) }
    var domingo by remember { mutableStateOf(true) }
    
    val timePickerState = rememberTimePickerState(
        initialHour = selectedHour,
        initialMinute = selectedMinute,
        is24Hour = true
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (showTimePicker) "Seleccionar hora" else "Seleccionar días",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (showTimePicker) {
                    TimePicker(state = timePickerState)
                } else {
                    // Selector de días
                    Column(modifier = Modifier.fillMaxWidth()) {
                        DayCheckbox("Lunes", lunes) { lunes = it }
                        DayCheckbox("Martes", martes) { martes = it }
                        DayCheckbox("Miércoles", miercoles) { miercoles = it }
                        DayCheckbox("Jueves", jueves) { jueves = it }
                        DayCheckbox("Viernes", viernes) { viernes = it }
                        DayCheckbox("Sábado", sabado) { sabado = it }
                        DayCheckbox("Domingo", domingo) { domingo = it }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Botones rápidos
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = lunes && martes && miercoles && jueves && viernes && sabado && domingo,
                                onClick = {
                                    lunes = true; martes = true; miercoles = true
                                    jueves = true; viernes = true; sabado = true; domingo = true
                                },
                                label = { Text("Todos") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = lunes && martes && miercoles && jueves && viernes && !sabado && !domingo,
                                onClick = {
                                    lunes = true; martes = true; miercoles = true
                                    jueves = true; viernes = true; sabado = false; domingo = false
                                },
                                label = { Text("L-V") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = !lunes && !martes && !miercoles && !jueves && !viernes && sabado && domingo,
                                onClick = {
                                    lunes = false; martes = false; miercoles = false
                                    jueves = false; viernes = false; sabado = true; domingo = true
                                },
                                label = { Text("Fin de semana") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (showTimePicker) {
                TextButton(
                    onClick = {
                        selectedHour = timePickerState.hour
                        selectedMinute = timePickerState.minute
                        showTimePicker = false
                    }
                ) {
                    Text("Siguiente")
                }
            } else {
                TextButton(
                    onClick = {
                        val horaEnMinutos = selectedHour * 60 + selectedMinute
                        var diasSemana = 0
                        if (lunes) diasSemana = diasSemana or MedicationSchedule.LUNES
                        if (martes) diasSemana = diasSemana or MedicationSchedule.MARTES
                        if (miercoles) diasSemana = diasSemana or MedicationSchedule.MIERCOLES
                        if (jueves) diasSemana = diasSemana or MedicationSchedule.JUEVES
                        if (viernes) diasSemana = diasSemana or MedicationSchedule.VIERNES
                        if (sabado) diasSemana = diasSemana or MedicationSchedule.SABADO
                        if (domingo) diasSemana = diasSemana or MedicationSchedule.DOMINGO
                        
                        // Si no hay días seleccionados, seleccionar todos
                        if (diasSemana == 0) diasSemana = MedicationSchedule.TODOS_LOS_DIAS
                        
                        onConfirm(horaEnMinutos, diasSemana)
                    },
                    enabled = lunes || martes || miercoles || jueves || viernes || sabado || domingo
                ) {
                    Text("Confirmar")
                }
            }
        },
        dismissButton = {
            if (showTimePicker) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            } else {
                TextButton(onClick = { showTimePicker = true }) {
                    Text("Atrás")
                }
            }
        }
    )
}

@Composable
private fun DayCheckbox(
    day: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = day,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
