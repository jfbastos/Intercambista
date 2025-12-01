package com.zamfir.intercambista.presentation.screen.main

import com.zamfir.intercambista.presentation.screen.main.navigation.MainScreenNavDestination

sealed class MainScreenIntent {

    data class NavigateTo(val destination: MainScreenNavDestination) : MainScreenIntent()
    data object OnReturnToCoinSelection : MainScreenIntent()
}