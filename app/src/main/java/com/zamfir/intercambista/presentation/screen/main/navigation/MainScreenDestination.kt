package com.zamfir.intercambista.presentation.screen.main.navigation

sealed class MainScreenNavDestination {

    data object Exchange : MainScreenNavDestination()
    data object Coins : MainScreenNavDestination()
    data object Calculator : MainScreenNavDestination()

}