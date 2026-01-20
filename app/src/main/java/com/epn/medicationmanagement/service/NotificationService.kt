package com.epn.medicationmanagement.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.epn.medicationmanagement.MainActivity
import com.epn.medicationmanagement.R
import com.epn.medicationmanagement.model.database.MedicationDatabase
import com.epn.medicationmanagement.model.repository.MedicationRepository
import com.epn.medicationmanagement.util.AlarmScheduler
import com.epn.medicationmanagement.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Foreground Service para mostrar notificaciones de medicamentos.
 * Garantiza que las notificaciones lleguen incluso con la app cerrada
 * y el teléfono bloqueado, con sonido y vibración.
 */
class NotificationService : Service() {
    
    private var wakeLock: PowerManager.WakeLock? = null
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    
    companion object {
        const val ACTION_SHOW_NOTIFICATION = "com.epn.medicationmanagement.SHOW_NOTIFICATION"
        const val ACTION_STOP_ALARM = "com.epn.medicationmanagement.STOP_ALARM"
        const val EXTRA_SCHEDULE_ID = "schedule_id"
        const val EXTRA_MEDICATION_NAME = "medication_name"
        const val EXTRA_MEDICATION_DOSIS = "medication_dosis"
        const val EXTRA_MEDICATION_INSTRUCCIONES = "medication_instrucciones"
        
        private const val NOTIFICATION_ID = 999
        
        /**
         * Inicia el servicio para mostrar una notificación de medicamento.
         */
        fun startService(
            context: Context,
            scheduleId: Long,
            medicationName: String,
            dosis: String?,
            instrucciones: String?
        ) {
            val intent = Intent(context, NotificationService::class.java).apply {
                action = ACTION_SHOW_NOTIFICATION
                putExtra(EXTRA_SCHEDULE_ID, scheduleId)
                putExtra(EXTRA_MEDICATION_NAME, medicationName)
                putExtra(EXTRA_MEDICATION_DOSIS, dosis ?: "")
                putExtra(EXTRA_MEDICATION_INSTRUCCIONES, instrucciones ?: "")
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Obtener vibrador
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW_NOTIFICATION -> {
                val scheduleId = intent.getLongExtra(EXTRA_SCHEDULE_ID, -1)
                val medicationName = intent.getStringExtra(EXTRA_MEDICATION_NAME) ?: "Medicamento"
                val dosis = intent.getStringExtra(EXTRA_MEDICATION_DOSIS)
                val instrucciones = intent.getStringExtra(EXTRA_MEDICATION_INSTRUCCIONES)
                
                if (scheduleId != -1L) {
                    acquireWakeLock()
                    showFullNotification(scheduleId, medicationName, dosis, instrucciones)
                    startVibrationAndSound()
                    rescheduleAlarm(scheduleId)
                }
            }
            ACTION_STOP_ALARM -> {
                stopAlarm()
                stopSelf()
            }
        }
        
        return START_NOT_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        stopAlarm()
        releaseWakeLock()
        super.onDestroy()
    }
    
    /**
     * Adquiere un WakeLock para asegurar que el dispositivo se despierte.
     */
    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "MedicationManagement::NotificationWakeLock"
        ).apply {
            acquire(60 * 1000L) // 1 minuto máximo
        }
    }
    
    /**
     * Libera el WakeLock.
     */
    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
    }
    
    /**
     * Muestra la notificación completa con todas las características.
     */
    private fun showFullNotification(
        scheduleId: Long,
        medicationName: String,
        dosis: String?,
        instrucciones: String?
    ) {
        // Intent para abrir la app
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("from_notification", true)
            putExtra("schedule_id", scheduleId)
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            scheduleId.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Intent para detener la alarma
        val stopIntent = Intent(this, NotificationService::class.java).apply {
            action = ACTION_STOP_ALARM
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            (scheduleId + 1000).toInt(),
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Construir texto de contenido
        val contentText = buildContentText(dosis, instrucciones)
        
        // Crear notificación
        val notification = NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_medication_notification)
            .setContentTitle("💊 Hora de tomar: $medicationName")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false)
            .setOngoing(true) // No se puede deslizar para cerrar
            .setContentIntent(openPendingIntent)
            .setFullScreenIntent(openPendingIntent, true) // Muestra sobre pantalla de bloqueo
            .addAction(
                R.drawable.ic_medication_notification,
                "✓ Tomado",
                stopPendingIntent
            )
            .addAction(
                R.drawable.ic_medication_notification,
                "Abrir app",
                openPendingIntent
            )
            .setDefaults(0) // Desactivamos defaults porque manejamos sonido/vibración manualmente
            .build()
        
        // Configurar flags adicionales para pantalla de bloqueo
        notification.flags = notification.flags or 
            Notification.FLAG_INSISTENT or // Repite el sonido
            Notification.FLAG_NO_CLEAR // No se puede limpiar
        
        // Iniciar como foreground service
        startForeground(NOTIFICATION_ID, notification)
    }
    
    /**
     * Inicia la vibración y el sonido de alarma.
     */
    private fun startVibrationAndSound() {
        // Patrón de vibración: espera, vibra, espera, vibra...
        val vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500, 1000)
        
        // Iniciar vibración
        vibrator?.let { v ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(
                    VibrationEffect.createWaveform(vibrationPattern, 0), // 0 = repetir desde índice 0
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(vibrationPattern, 0)
            }
        }
        
        // Iniciar sonido de alarma
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@NotificationService, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Detiene la vibración y el sonido.
     */
    private fun stopAlarm() {
        // Detener vibración
        vibrator?.cancel()
        
        // Detener sonido
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
        
        // Cancelar la notificación foreground
        stopForeground(STOP_FOREGROUND_REMOVE)
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
            "Es hora de tomar tu medicamento. Toca 'Tomado' cuando lo hayas tomado."
        } else {
            parts.joinToString(" • ") + "\n\nToca 'Tomado' cuando lo hayas tomado."
        }
    }
    
    /**
     * Reprograma la alarma para el próximo horario.
     */
    private fun rescheduleAlarm(scheduleId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = MedicationDatabase.getInstance(this@NotificationService)
                val repository = MedicationRepository(database.medicationDao())
                val alarmScheduler = AlarmScheduler(this@NotificationService)
                
                val schedule = repository.getScheduleById(scheduleId)
                if (schedule != null && schedule.activo) {
                    val medication = repository.getMedicationForSchedule(scheduleId)
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
