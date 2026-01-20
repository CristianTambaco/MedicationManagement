package com.epn.medicationmanagement.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.epn.medicationmanagement.model.entity.MedicationSchedule

/**
 * Item que muestra un horario de medicamento con opción de eliminar.
 */
@Composable
fun ScheduleItem(
    schedule: MedicationSchedule,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (schedule.activo) 
                MaterialTheme.colorScheme.secondaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = if (schedule.activo) 
                    MaterialTheme.colorScheme.onSecondaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.getFormattedTime(),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (schedule.activo) 
                        MaterialTheme.colorScheme.onSecondaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = getDaysText(schedule.diasSemana),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (schedule.activo) 
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f) 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            Switch(
                checked = schedule.activo,
                onCheckedChange = { onToggle() }
            )
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar horario",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Convierte el bitmask de días a texto legible.
 */
private fun getDaysText(diasSemana: Int): String {
    if (diasSemana == MedicationSchedule.TODOS_LOS_DIAS) {
        return "Todos los días"
    }
    if (diasSemana == MedicationSchedule.LUNES_A_VIERNES) {
        return "Lunes a Viernes"
    }
    if (diasSemana == MedicationSchedule.FIN_DE_SEMANA) {
        return "Fines de semana"
    }
    
    val days = mutableListOf<String>()
    if ((diasSemana and MedicationSchedule.LUNES) != 0) days.add("Lun")
    if ((diasSemana and MedicationSchedule.MARTES) != 0) days.add("Mar")
    if ((diasSemana and MedicationSchedule.MIERCOLES) != 0) days.add("Mié")
    if ((diasSemana and MedicationSchedule.JUEVES) != 0) days.add("Jue")
    if ((diasSemana and MedicationSchedule.VIERNES) != 0) days.add("Vie")
    if ((diasSemana and MedicationSchedule.SABADO) != 0) days.add("Sáb")
    if ((diasSemana and MedicationSchedule.DOMINGO) != 0) days.add("Dom")
    
    return days.joinToString(", ")
}
