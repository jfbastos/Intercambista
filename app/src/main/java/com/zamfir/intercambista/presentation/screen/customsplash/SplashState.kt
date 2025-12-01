package com.zamfir.intercambista.presentation.screen.customsplash

sealed class SplashState {

    data class  Loading(val msg : String) : SplashState()

    data class  OnFailure(val error : String) : SplashState()

    data object OnCurrencyAlreadySelected : SplashState()

    data object OnSuccess : SplashState()
}