package com.epn.medicationmanagement.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.epn.medicationmanagement.model.dao.MedicationDao
import com.epn.medicationmanagement.model.entity.Medication
import com.epn.medicationmanagement.model.entity.MedicationSchedule

/**
 * Base de datos Room con patrón Singleton.
 * Garantiza una única instancia de la base de datos en toda la aplicación.
 */
@Database(
    entities = [Medication::class, MedicationSchedule::class],
    version = 1,
    exportSchema = false
)
abstract class MedicationDatabase : RoomDatabase() {
    
    abstract fun medicationDao(): MedicationDao
    
    companion object {
        private const val DATABASE_NAME = "medication_database"
        
        @Volatile
        private var INSTANCE: MedicationDatabase? = null
        
        /**
         * Obtiene la instancia única de la base de datos (Singleton).
         * Thread-safe mediante double-checked locking.
         */
        fun getInstance(context: Context): MedicationDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }
        
        private fun buildDatabase(context: Context): MedicationDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                MedicationDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
