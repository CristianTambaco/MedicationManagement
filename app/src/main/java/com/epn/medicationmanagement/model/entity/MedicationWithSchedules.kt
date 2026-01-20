package com.epn.medicationmanagement.model.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Clase de relación para obtener un medicamento con todos sus horarios.
 */
data class MedicationWithSchedules(
    @Embedded
    val medication: Medication,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "medicationId"
    )
    val schedules: List<MedicationSchedule>
)
