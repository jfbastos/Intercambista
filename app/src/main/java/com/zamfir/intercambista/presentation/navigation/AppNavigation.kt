package com.zamfir.intercambista.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zamfir.intercambista.presentation.screen.coinselection.CoinSelectionNavigation
import com.zamfir.intercambista.presentation.screen.customsplash.CustomSplashScreenNavigation
import com.zamfir.intercambista.presentation.screen.main.MainScreenNavigation

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "CustomSplashScreen"
    ) {
        composable("CustomSplashScreen") {
            CustomSplashScreenNavigation(navController)
        }
        composable("CoinSelectionScreen") {
            CoinSelectionNavigation(navController)
        }
        composable("MainScreen") {
            MainScreenNavigation(navController)
        }
    }
}