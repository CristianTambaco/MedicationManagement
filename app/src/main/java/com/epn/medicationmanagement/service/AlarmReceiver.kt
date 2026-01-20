package com.epn.medicationmanagement.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.epn.medicationmanagement.util.AlarmScheduler

/**
 * BroadcastReceiver que se activa cuando una alarma de medicamento se dispara.
 * Inicia el NotificationService para mostrar la notificación con sonido y vibración.
 */
class AlarmReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getLongExtra(AlarmScheduler.EXTRA_SCHEDULE_ID, -1)
        val medicationName = intent.getStringExtra(AlarmScheduler.EXTRA_MEDICATION_NAME) ?: "Medicamento"
        val dosis = intent.getStringExtra(AlarmScheduler.EXTRA_MEDICATION_DOSIS)
        val instrucciones = intent.getStringExtra(AlarmScheduler.EXTRA_MEDICATION_INSTRUCCIONES)
        
        if (scheduleId == -1L) return
        
        // Adquirir WakeLock temporal para asegurar que el servicio se inicie
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "MedicationManagement::AlarmReceiverWakeLock"
        )
        wakeLock.acquire(10 * 1000L) // 10 segundos máximo
        
        try {
            // Iniciar el servicio de notificación
            NotificationService.startService(
                context = context,
                scheduleId = scheduleId,
                medicationName = medicationName,
                dosis = dosis,
                instrucciones = instrucciones
            )
        } finally {
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }
    }
}
