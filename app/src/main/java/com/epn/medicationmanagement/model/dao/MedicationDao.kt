package com.epn.medicationmanagement.model.dao

import androidx.room.*
import com.epn.medicationmanagement.model.entity.Medication
import com.epn.medicationmanagement.model.entity.MedicationSchedule
import com.epn.medicationmanagement.model.entity.MedicationWithSchedules
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de base de datos de medicamentos y horarios.
 */
@Dao
interface MedicationDao {
    
    // ==================== MEDICAMENTOS ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication): Long
    
    @Update
    suspend fun updateMedication(medication: Medication)
    
    @Delete
    suspend fun deleteMedication(medication: Medication)
    
    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedicationById(id: Long): Medication?
    
    @Query("SELECT * FROM medications WHERE activo = 1 ORDER BY nombre ASC")
    fun getAllActiveMedications(): Flow<List<Medication>>
    
    @Query("SELECT * FROM medications ORDER BY nombre ASC")
    fun getAllMedications(): Flow<List<Medication>>
    
    @Query("SELECT * FROM medications WHERE nombre LIKE '%' || :query || '%'")
    fun searchMedications(query: String): Flow<List<Medication>>
    
    // ==================== HORARIOS ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: MedicationSchedule): Long
    
    @Update
    suspend fun updateSchedule(schedule: MedicationSchedule)
    
    @Delete
    suspend fun deleteSchedule(schedule: MedicationSchedule)
    
    @Query("SELECT * FROM medication_schedules WHERE id = :id")
    suspend fun getScheduleById(id: Long): MedicationSchedule?
    
    @Query("SELECT * FROM medication_schedules WHERE medicationId = :medicationId AND activo = 1")
    fun getActiveSchedulesForMedication(medicationId: Long): Flow<List<MedicationSchedule>>
    
    @Query("SELECT * FROM medication_schedules WHERE medicationId = :medicationId")
    fun getAllSchedulesForMedication(medicationId: Long): Flow<List<MedicationSchedule>>
    
    @Query("SELECT * FROM medication_schedules WHERE activo = 1 ORDER BY horaEnMinutos ASC")
    fun getAllActiveSchedules(): Flow<List<MedicationSchedule>>
    
    @Query("DELETE FROM medication_schedules WHERE medicationId = :medicationId")
    suspend fun deleteAllSchedulesForMedication(medicationId: Long)
    
    // ==================== RELACIONES ====================
    
    @Transaction
    @Query("SELECT * FROM medications WHERE id = :medicationId")
    suspend fun getMedicationWithSchedules(medicationId: Long): MedicationWithSchedules?
    
    @Transaction
    @Query("SELECT * FROM medications WHERE activo = 1 ORDER BY nombre ASC")
    fun getAllActiveMedicationsWithSchedules(): Flow<List<MedicationWithSchedules>>
    
    @Transaction
    @Query("SELECT * FROM medications ORDER BY nombre ASC")
    fun getAllMedicationsWithSchedules(): Flow<List<MedicationWithSchedules>>
    
    // ==================== CONSULTAS ESPECIALES PARA ALARMAS ====================
    
    @Query("""
        SELECT ms.* FROM medication_schedules ms
        INNER JOIN medications m ON ms.medicationId = m.id
        WHERE ms.activo = 1 AND m.activo = 1
        ORDER BY ms.horaEnMinutos ASC
    """)
    suspend fun getAllActiveSchedulesForAlarms(): List<MedicationSchedule>
    
    @Query("""
        SELECT m.* FROM medications m
        INNER JOIN medication_schedules ms ON m.id = ms.medicationId
        WHERE ms.id = :scheduleId
    """)
    suspend fun getMedicationForSchedule(scheduleId: Long): Medication?
}
