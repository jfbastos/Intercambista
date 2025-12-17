package com.zamfir.intercambista.presentation.screen.main.exchange

sealed class ExchangeIntents {
    data object OnChangeBaseCoin : ExchangeIntents()
    data object OnChangeCoinConfirmed : ExchangeIntents()
    data object OnLoadingDialog : ExchangeIntents()
    data class OnFailureDialog(val msg : String, val exception : Exception? = null) : ExchangeIntents()
    data object OnDismissDialog : ExchangeIntents()
    data object OnRefresh : ExchangeIntents()
    data object OnFilter : ExchangeIntents()
}