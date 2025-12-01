package com.zamfir.intercambista.presentation.screen.main.exchange

sealed class ExchangeDialogState {

    data object ShowConfirmationCoinChangeDialog : ExchangeDialogState()
    data object NoneShown : ExchangeDialogState()

}