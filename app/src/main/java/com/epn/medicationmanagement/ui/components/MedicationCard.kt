package com.epn.medicationmanagement.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.epn.medicationmanagement.model.entity.MedicationWithSchedules

/**
 * Tarjeta que muestra información de un medicamento en la lista principal.
 */
@Composable
fun MedicationCard(
    medicationWithSchedules: MedicationWithSchedules,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val medication = medicationWithSchedules.medication
    val schedules = medicationWithSchedules.schedules.filter { it.activo }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de medicamento
//            Surface(
//                modifier = Modifier.size(48.dp),
//                shape = MaterialTheme.shapes.medium,
//                color = MaterialTheme.colorScheme.primaryContainer
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Medication,
//                    contentDescription = null,
//                    modifier = Modifier
//                        .padding(12.dp)
//                        .size(24.dp),
//                    tint = MaterialTheme.colorScheme.onPrimaryContainer
//                )
//            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Información del medicamento
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = medication.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (!medication.dosis.isNullOrBlank()) {
                    Text(
                        text = buildString {
                            append(medication.dosis)
                            if (!medication.unidad.isNullOrBlank()) {
                                append(" ${medication.unidad}")
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Mostrar horarios
                if (schedules.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = schedules.take(3).joinToString(", ") { it.getFormattedTime() } +
                                    if (schedules.size > 3) " +${schedules.size - 3}" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sin horarios configurados",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // Botón de eliminar
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
