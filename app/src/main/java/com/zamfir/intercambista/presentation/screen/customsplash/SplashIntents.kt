package com.zamfir.intercambista.presentation.screen.customsplash

sealed class SplashIntents {

    data object OnNavigateCoinSelection : SplashIntents()
    data object OnNavigateMainScreen : SplashIntents()
    data class  OnFailure(val msg : String, val exception: Exception? = null) : SplashIntents()
    data object OnDismissDialog : SplashIntents()

}