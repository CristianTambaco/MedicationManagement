package com.epn.medicationmanagement.model.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad que representa un horario de recordatorio para un medicamento.
 * Cada medicamento puede tener múltiples horarios.
 */
@Entity(
    tableName = "medication_schedules",
    foreignKeys = [
        ForeignKey(
            entity = Medication::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["medicationId"])]
)
data class MedicationSchedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val medicationId: Long,
    
    // Hora del recordatorio (en minutos desde medianoche, ej: 8:30 AM = 8*60+30 = 510)
    val horaEnMinutos: Int,
    
    // Días de la semana (bitmask: Lun=1, Mar=2, Mie=4, Jue=8, Vie=16, Sab=32, Dom=64)
    // 127 = todos los días, 31 = lunes a viernes, etc.
    val diasSemana: Int = 127,
    
    val activo: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis()
) {
    companion object {
        const val LUNES = 1
        const val MARTES = 2
        const val MIERCOLES = 4
        const val JUEVES = 8
        const val VIERNES = 16
        const val SABADO = 32
        const val DOMINGO = 64
        const val TODOS_LOS_DIAS = 127
        const val LUNES_A_VIERNES = 31
        const val FIN_DE_SEMANA = 96
    }
    
    /**
     * Verifica si el horario está activo para un día específico
     */
    fun isActiveForDay(day: Int): Boolean {
        return (diasSemana and day) != 0
    }
    
    /**
     * Obtiene la hora formateada (HH:mm)
     */
    fun getFormattedTime(): String {
        val hours = horaEnMinutos / 60
        val minutes = horaEnMinutos % 60
        return String.format("%02d:%02d", hours, minutes)
    }
}
