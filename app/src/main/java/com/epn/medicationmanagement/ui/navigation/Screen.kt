package com.epn.medicationmanagement.ui.navigation

/**
 * Rutas de navegación de la aplicación.
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddMedication : Screen("add_medication")
    object EditMedication : Screen("edit_medication/{medicationId}") {
        fun createRoute(medicationId: Long) = "edit_medication/$medicationId"
    }
    object MedicationDetail : Screen("medication_detail/{medicationId}") {
        fun createRoute(medicationId: Long) = "medication_detail/$medicationId"
    }
}
