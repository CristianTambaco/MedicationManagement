package com.epn.medicationmanagement.controller

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.epn.medicationmanagement.model.database.MedicationDatabase
import com.epn.medicationmanagement.model.entity.Medication
import com.epn.medicationmanagement.model.entity.MedicationSchedule
import com.epn.medicationmanagement.model.entity.MedicationWithSchedules
import com.epn.medicationmanagement.model.repository.MedicationRepository
import com.epn.medicationmanagement.util.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Controller principal para gestionar medicamentos (Patrón MVC).
 * Actúa como intermediario entre la Vista (UI) y el Modelo (Repository).
 */
class MedicationController(application: Application) : AndroidViewModel(application) {
    
    private val repository: MedicationRepository
    private val alarmScheduler: AlarmScheduler
    
    // Estados para la UI
    private val _medicationsWithSchedules = MutableStateFlow<List<MedicationWithSchedules>>(emptyList())
    val medicationsWithSchedules: StateFlow<List<MedicationWithSchedules>> = _medicationsWithSchedules.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _selectedMedication = MutableStateFlow<MedicationWithSchedules?>(null)
    val selectedMedication: StateFlow<MedicationWithSchedules?> = _selectedMedication.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        val database = MedicationDatabase.getInstance(application)
        repository = MedicationRepository(database.medicationDao())
        alarmScheduler = AlarmScheduler(application)
        
        // Cargar medicamentos al inicializar
        loadMedications()
    }
    
    /**
     * Carga todos los medicamentos con sus horarios.
     */
    private fun loadMedications() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.allActiveMedicationsWithSchedules.collect { medications ->
                    _medicationsWithSchedules.value = medications
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar medicamentos: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Agrega un nuevo medicamento.
     */
    fun addMedication(
        nombre: String,
        dosis: String? = null,
        unidad: String? = null,
        instrucciones: String? = null,
        notas: String? = null,
        onSuccess: (Long) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (nombre.isBlank()) {
            onError("El nombre del medicamento es obligatorio")
            return
        }
        
        viewModelScope.launch {
            try {
                val medication = Medication(
                    nombre = nombre.trim(),
                    dosis = dosis?.takeIf { it.isNotBlank() },
                    unidad = unidad?.takeIf { it.isNotBlank() },
                    instrucciones = instrucciones?.takeIf { it.isNotBlank() },
                    notas = notas?.takeIf { it.isNotBlank() }
                )
                val id = repository.insertMedication(medication)
                onSuccess(id)
            } catch (e: Exception) {
                onError("Error al agregar medicamento: ${e.message}")
            }
        }
    }
    
    /**
     * Actualiza un medicamento existente.
     */
    fun updateMedication(
        medication: Medication,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (medication.nombre.isBlank()) {
            onError("El nombre del medicamento es obligatorio")
            return
        }
        
        viewModelScope.launch {
            try {
                repository.updateMedication(medication)
                // Reprogramar alarmas para este medicamento
                rescheduleAlarmsForMedication(medication.id)
                onSuccess()
            } catch (e: Exception) {
                onError("Error al actualizar medicamento: ${e.message}")
            }
        }
    }
    
    /**
     * Elimina un medicamento y sus horarios.
     */
    fun deleteMedication(
        medication: Medication,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                // Cancelar todas las alarmas de este medicamento
                val schedules = repository.getMedicationWithSchedules(medication.id)?.schedules ?: emptyList()
                schedules.forEach { schedule ->
                    alarmScheduler.cancelAlarm(schedule.id)
                }
                
                repository.deleteMedication(medication)
                onSuccess()
            } catch (e: Exception) {
                onError("Error al eliminar medicamento: ${e.message}")
            }
        }
    }
    
    /**
     * Obtiene un medicamento por su ID.
     */
    fun getMedicationById(id: Long, onResult: (MedicationWithSchedules?) -> Unit) {
        viewModelScope.launch {
            try {
                val medication = repository.getMedicationWithSchedules(id)
                _selectedMedication.value = medication
                onResult(medication)
            } catch (e: Exception) {
                _errorMessage.value = "Error al obtener medicamento: ${e.message}"
                onResult(null)
            }
        }
    }
    
    /**
     * Agrega un horario a un medicamento.
     */
    fun addSchedule(
        medicationId: Long,
        horaEnMinutos: Int,
        diasSemana: Int = MedicationSchedule.TODOS_LOS_DIAS,
        onSuccess: (Long) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val schedule = MedicationSchedule(
                    medicationId = medicationId,
                    horaEnMinutos = horaEnMinutos,
                    diasSemana = diasSemana
                )
                val scheduleId = repository.insertSchedule(schedule)
                
                // Programar la alarma
                val medication = repository.getMedicationById(medicationId)
                if (medication != null) {
                    alarmScheduler.scheduleAlarm(schedule.copy(id = scheduleId), medication)
                }
                
                onSuccess(scheduleId)
            } catch (e: Exception) {
                onError("Error al agregar horario: ${e.message}")
            }
        }
    }
    
    /**
     * Elimina un horario.
     */
    fun deleteSchedule(
        schedule: MedicationSchedule,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                alarmScheduler.cancelAlarm(schedule.id)
                repository.deleteSchedule(schedule)
                onSuccess()
            } catch (e: Exception) {
                onError("Error al eliminar horario: ${e.message}")
            }
        }
    }
    
    /**
     * Activa/desactiva un horario.
     */
    fun toggleScheduleActive(
        schedule: MedicationSchedule,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val updatedSchedule = schedule.copy(activo = !schedule.activo)
                repository.updateSchedule(updatedSchedule)
                
                if (updatedSchedule.activo) {
                    val medication = repository.getMedicationById(schedule.medicationId)
                    if (medication != null) {
                        alarmScheduler.scheduleAlarm(updatedSchedule, medication)
                    }
                } else {
                    alarmScheduler.cancelAlarm(schedule.id)
                }
                
                onSuccess()
            } catch (e: Exception) {
                onError("Error al actualizar horario: ${e.message}")
            }
        }
    }
    
    /**
     * Reprograma todas las alarmas (útil después de reinicio del dispositivo).
     */
    fun rescheduleAllAlarms() {
        viewModelScope.launch {
            try {
                val schedules = repository.getAllActiveSchedulesForAlarms()
                schedules.forEach { schedule ->
                    val medication = repository.getMedicationById(schedule.medicationId)
                    if (medication != null && medication.activo) {
                        alarmScheduler.scheduleAlarm(schedule, medication)
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al reprogramar alarmas: ${e.message}"
            }
        }
    }
    
    /**
     * Reprograma alarmas para un medicamento específico.
     */
    private suspend fun rescheduleAlarmsForMedication(medicationId: Long) {
        try {
            val medicationWithSchedules = repository.getMedicationWithSchedules(medicationId)
            medicationWithSchedules?.let { mws ->
                mws.schedules.filter { it.activo }.forEach { schedule ->
                    if (mws.medication.activo) {
                        alarmScheduler.scheduleAlarm(schedule, mws.medication)
                    } else {
                        alarmScheduler.cancelAlarm(schedule.id)
                    }
                }
            }
        } catch (e: Exception) {
            _errorMessage.value = "Error al reprogramar alarmas: ${e.message}"
        }
    }
    
    /**
     * Limpia el mensaje de error.
     */
    fun clearError() {
        _errorMessage.value = null
    }
}
