package com.epn.medicationmanagement.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.epn.medicationmanagement.MainActivity
import com.epn.medicationmanagement.R

/**
 * Helper para crear y mostrar notificaciones de medicamentos.
 * Configura canal con alta prioridad, sonido y vibración.
 */
object NotificationHelper {
    
    const val CHANNEL_ID = "medication_reminders_high_priority"
    private const val CHANNEL_NAME = "Recordatorios de Medicamentos"
    private const val CHANNEL_DESCRIPTION = "Notificaciones importantes para recordarte tomar tus medicamentos. No silencies este canal."
    
    /**
     * Crea el canal de notificaciones con máxima prioridad (requerido para Android 8.0+).
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = 
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Eliminar canal anterior si existe (por si cambiamos configuración)
            notificationManager.deleteNotificationChannel("medication_reminders")
            
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                
                // Vibración
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                
                // Luces
                enableLights(true)
                lightColor = android.graphics.Color.GREEN
                
                // Badge en ícono de app
                setShowBadge(true)
                
                // Visible en pantalla de bloqueo
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                
                // No permitir que DND lo silencie
                setBypassDnd(true)
                
                // Configurar sonido de alarma
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                setSound(soundUri, audioAttributes)
            }
            
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Verifica si las notificaciones están habilitadas.
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    
    /**
     * Abre la configuración de notificaciones de la app.
     */
    fun openNotificationSettings(context: Context) {
        val intent = Intent().apply {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                else -> {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = android.net.Uri.parse("package:${context.packageName}")
                }
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
    
    /**
     * Muestra una notificación simple de recordatorio (usada como fallback).
     */
    fun showMedicationReminder(
        context: Context,
        scheduleId: Long,
        medicationName: String,
        dosis: String?,
        instrucciones: String?
    ) {
        // Intent para abrir la app al tocar la notificación
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("from_notification", true)
            putExtra("schedule_id", scheduleId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            scheduleId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Construir el texto de la notificación
        val contentText = buildContentText(dosis, instrucciones)
        
        // Sonido de alarma
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_medication_notification)
            .setContentTitle("💊 Hora de tomar: $medicationName")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
            .setFullScreenIntent(pendingIntent, true)
            .build()
        
        val notificationManager = 
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(scheduleId.toInt(), notification)
    }
    
    /**
     * Construye el texto del contenido de la notificación.
     */
    private fun buildContentText(dosis: String?, instrucciones: String?): String {
        val parts = mutableListOf<String>()
        
        if (!dosis.isNullOrBlank()) {
            parts.add("Dosis: $dosis")
        }
        
        if (!instrucciones.isNullOrBlank()) {
            parts.add(instrucciones)
        }
        
        return if (parts.isEmpty()) {
            "Es hora de tomar tu medicamento"
        } else {
            parts.joinToString(" • ")
        }
    }
    
    /**
     * Cancela una notificación específica.
     */
    fun cancelNotification(context: Context, scheduleId: Long) {
        val notificationManager = 
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(scheduleId.toInt())
    }
}
