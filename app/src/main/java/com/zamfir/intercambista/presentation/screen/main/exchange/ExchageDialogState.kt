package com.zamfir.intercambista.presentation.screen.main.exchange

sealed class ExchangeDialogState {

    data object ShowConfirmationCoinChangeDialog : ExchangeDialogState()
    data object ShowLoadingDialog : ExchangeDialogState()
    data class ShowErrorDialog(val msg : String, val exception : Exception?) : ExchangeDialogState()
    data object NoneShown : ExchangeDialogState()

}