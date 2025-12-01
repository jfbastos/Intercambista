package com.zamfir.intercambista.presentation.screen.customsplash

sealed class SplashDialogsState {

    data object OnNoneShown : SplashDialogsState()
    data class  OnShowErrorDialog(val msg : String) : SplashDialogsState()

}