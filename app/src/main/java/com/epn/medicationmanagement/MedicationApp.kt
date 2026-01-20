package com.epn.medicationmanagement

import android.app.Application
import com.epn.medicationmanagement.util.NotificationHelper

/**
 * Clase Application personalizada.
 * Inicializa componentes globales como el canal de notificaciones.
 */
class MedicationApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Crear canal de notificaciones al iniciar la aplicación
        NotificationHelper.createNotificationChannel(this)
    }
}
