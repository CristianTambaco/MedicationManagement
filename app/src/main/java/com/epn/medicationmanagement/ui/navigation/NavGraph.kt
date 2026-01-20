package com.epn.medicationmanagement.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.epn.medicationmanagement.controller.MedicationController
import com.epn.medicationmanagement.ui.screens.AddMedicationScreen
import com.epn.medicationmanagement.ui.screens.EditMedicationScreen
import com.epn.medicationmanagement.ui.screens.HomeScreen
import com.epn.medicationmanagement.ui.screens.MedicationDetailScreen

/**
 * Grafo de navegación de la aplicación.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    controller: MedicationController = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                controller = controller,
                onNavigateToAddMedication = {
                    navController.navigate(Screen.AddMedication.route)
                },
                onNavigateToMedicationDetail = { medicationId ->
                    navController.navigate(Screen.MedicationDetail.createRoute(medicationId))
                }
            )
        }
        
        composable(Screen.AddMedication.route) {
            AddMedicationScreen(
                controller = controller,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.MedicationDetail.route,
            arguments = listOf(
                navArgument("medicationId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val medicationId = backStackEntry.arguments?.getLong("medicationId") ?: 0L
            MedicationDetailScreen(
                medicationId = medicationId,
                controller = controller,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = {
                    navController.navigate(Screen.EditMedication.createRoute(medicationId))
                }
            )
        }
        
        composable(
            route = Screen.EditMedication.route,
            arguments = listOf(
                navArgument("medicationId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val medicationId = backStackEntry.arguments?.getLong("medicationId") ?: 0L
            EditMedicationScreen(
                medicationId = medicationId,
                controller = controller,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
