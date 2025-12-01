package com.zamfir.intercambista.presentation.screen.commom

sealed class NavigationEvents {

    data object GoToCoinSelectionScreen : NavigationEvents()
    data object GoToMainScreen : NavigationEvents()

}