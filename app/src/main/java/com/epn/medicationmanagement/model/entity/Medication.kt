package com.epn.medicationmanagement.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa un medicamento en la base de datos.
 * Solo el nombre es obligatorio, los demás campos son opcionales.
 */
@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Campo obligatorio
    val nombre: String,
    
    // Campos opcionales
    val dosis: String? = null,
    val unidad: String? = null, // mg, ml, tabletas, gotas, etc.
    val instrucciones: String? = null, // ej: "Tomar con comida"
    val notas: String? = null,
    val fechaInicio: Long? = null, // timestamp
    val fechaFin: Long? = null, // timestamp
    val activo: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis()
)
