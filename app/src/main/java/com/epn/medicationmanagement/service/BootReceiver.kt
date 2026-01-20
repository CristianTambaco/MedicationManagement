package com.epn.medicationmanagement.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.epn.medicationmanagement.model.database.MedicationDatabase
import com.epn.medicationmanagement.model.repository.MedicationRepository
import com.epn.medicationmanagement.util.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver que se activa cuando el dispositivo se reinicia.
 * Reprograma todas las alarmas activas de medicamentos.
 */
class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            rescheduleAllAlarms(context)
        }
    }
    
    /**
     * Reprograma todas las alarmas activas después del reinicio.
     */
    private fun rescheduleAllAlarms(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = MedicationDatabase.getInstance(context)
                val repository = MedicationRepository(database.medicationDao())
                val alarmScheduler = AlarmScheduler(context)
                
                val schedules = repository.getAllActiveSchedulesForAlarms()
                
                schedules.forEach { schedule ->
                    val medication = repository.getMedicationForSchedule(schedule.id)
                    if (medication != null && medication.activo) {
                        alarmScheduler.scheduleAlarm(schedule, medication)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
