package com.epn.medicationmanagement.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.epn.medicationmanagement.model.entity.Medication
import com.epn.medicationmanagement.model.entity.MedicationSchedule
import com.epn.medicationmanagement.service.AlarmReceiver
import java.util.Calendar

/**
 * Utilidad para programar y cancelar alarmas de medicamentos.
 * Usa AlarmManager para alarmas exactas que funcionan con la app cerrada.
 */
class AlarmScheduler(private val context: Context) {
    
    private val alarmManager: AlarmManager = 
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    companion object {
        const val EXTRA_SCHEDULE_ID = "schedule_id"
        const val EXTRA_MEDICATION_NAME = "medication_name"
        const val EXTRA_MEDICATION_DOSIS = "medication_dosis"
        const val EXTRA_MEDICATION_INSTRUCCIONES = "medication_instrucciones"
    }
    
    /**
     * Programa una alarma para un horario de medicamento.
     */
    fun scheduleAlarm(schedule: MedicationSchedule, medication: Medication) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_SCHEDULE_ID, schedule.id)
            putExtra(EXTRA_MEDICATION_NAME, medication.nombre)
            putExtra(EXTRA_MEDICATION_DOSIS, medication.dosis ?: "")
            putExtra(EXTRA_MEDICATION_INSTRUCCIONES, medication.instrucciones ?: "")
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val triggerTime = getNextTriggerTime(schedule)
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAlarmClock(
                        AlarmManager.AlarmClockInfo(triggerTime, pendingIntent),
                        pendingIntent
                    )
                } else {
                    // Fallback si no tiene permiso de alarmas exactas
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerTime, pendingIntent),
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fallback a alarma inexacta si hay problemas de permisos
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
    
    /**
     * Cancela una alarma programada.
     */
    fun cancelAlarm(scheduleId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
    
    /**
     * Calcula el próximo momento en que debe dispararse la alarma.
     */
    private fun getNextTriggerTime(schedule: MedicationSchedule): Long {
        val now = Calendar.getInstance()
        val triggerCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, schedule.horaEnMinutos / 60)
            set(Calendar.MINUTE, schedule.horaEnMinutos % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // Si la hora ya pasó hoy, programar para el próximo día válido
        if (triggerCalendar.timeInMillis <= now.timeInMillis) {
            triggerCalendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        // Encontrar el próximo día válido según los días configurados
        val maxDays = 7
        var daysChecked = 0
        while (daysChecked < maxDays) {
            val dayOfWeek = getDayBitmask(triggerCalendar.get(Calendar.DAY_OF_WEEK))
            if (schedule.isActiveForDay(dayOfWeek)) {
                break
            }
            triggerCalendar.add(Calendar.DAY_OF_YEAR, 1)
            daysChecked++
        }
        
        return triggerCalendar.timeInMillis
    }
    
    /**
     * Convierte el día de Calendar a nuestro bitmask.
     */
    private fun getDayBitmask(calendarDay: Int): Int {
        return when (calendarDay) {
            Calendar.MONDAY -> MedicationSchedule.LUNES
            Calendar.TUESDAY -> MedicationSchedule.MARTES
            Calendar.WEDNESDAY -> MedicationSchedule.MIERCOLES
            Calendar.THURSDAY -> MedicationSchedule.JUEVES
            Calendar.FRIDAY -> MedicationSchedule.VIERNES
            Calendar.SATURDAY -> MedicationSchedule.SABADO
            Calendar.SUNDAY -> MedicationSchedule.DOMINGO
            else -> 0
        }
    }
}
