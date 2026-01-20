package com.epn.medicationmanagement.model.repository

import com.epn.medicationmanagement.model.dao.MedicationDao
import com.epn.medicationmanagement.model.entity.Medication
import com.epn.medicationmanagement.model.entity.MedicationSchedule
import com.epn.medicationmanagement.model.entity.MedicationWithSchedules
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio que abstrae el acceso a datos de medicamentos.
 * Implementa el patrón Repository para separar la lógica de datos.
 */
class MedicationRepository(private val medicationDao: MedicationDao) {
    
    // ==================== MEDICAMENTOS ====================
    
    val allMedications: Flow<List<Medication>> = medicationDao.getAllMedications()
    
    val allActiveMedications: Flow<List<Medication>> = medicationDao.getAllActiveMedications()
    
    val allMedicationsWithSchedules: Flow<List<MedicationWithSchedules>> = 
        medicationDao.getAllMedicationsWithSchedules()
    
    val allActiveMedicationsWithSchedules: Flow<List<MedicationWithSchedules>> = 
        medicationDao.getAllActiveMedicationsWithSchedules()
    
    suspend fun insertMedication(medication: Medication): Long {
        return medicationDao.insertMedication(medication)
    }
    
    suspend fun updateMedication(medication: Medication) {
        medicationDao.updateMedication(medication)
    }
    
    suspend fun deleteMedication(medication: Medication) {
        medicationDao.deleteMedication(medication)
    }
    
    suspend fun getMedicationById(id: Long): Medication? {
        return medicationDao.getMedicationById(id)
    }
    
    suspend fun getMedicationWithSchedules(medicationId: Long): MedicationWithSchedules? {
        return medicationDao.getMedicationWithSchedules(medicationId)
    }
    
    fun searchMedications(query: String): Flow<List<Medication>> {
        return medicationDao.searchMedications(query)
    }
    
    // ==================== HORARIOS ====================
    
    val allActiveSchedules: Flow<List<MedicationSchedule>> = medicationDao.getAllActiveSchedules()
    
    suspend fun insertSchedule(schedule: MedicationSchedule): Long {
        return medicationDao.insertSchedule(schedule)
    }
    
    suspend fun updateSchedule(schedule: MedicationSchedule) {
        medicationDao.updateSchedule(schedule)
    }
    
    suspend fun deleteSchedule(schedule: MedicationSchedule) {
        medicationDao.deleteSchedule(schedule)
    }
    
    suspend fun getScheduleById(id: Long): MedicationSchedule? {
        return medicationDao.getScheduleById(id)
    }
    
    fun getSchedulesForMedication(medicationId: Long): Flow<List<MedicationSchedule>> {
        return medicationDao.getAllSchedulesForMedication(medicationId)
    }
    
    fun getActiveSchedulesForMedication(medicationId: Long): Flow<List<MedicationSchedule>> {
        return medicationDao.getActiveSchedulesForMedication(medicationId)
    }
    
    suspend fun deleteAllSchedulesForMedication(medicationId: Long) {
        medicationDao.deleteAllSchedulesForMedication(medicationId)
    }
    
    // ==================== PARA ALARMAS ====================
    
    suspend fun getAllActiveSchedulesForAlarms(): List<MedicationSchedule> {
        return medicationDao.getAllActiveSchedulesForAlarms()
    }
    
    suspend fun getMedicationForSchedule(scheduleId: Long): Medication? {
        return medicationDao.getMedicationForSchedule(scheduleId)
    }
}
