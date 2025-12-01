package com.zamfir.intercambista.presentation.screen.main.exchange

sealed class ExchangeIntents {

    data object OnChangeBaseCoin : ExchangeIntents()
    data object OnChangeCoinConfirmed : ExchangeIntents()
    data object OnDismissDialog : ExchangeIntents()
    data object OnRefresh : ExchangeIntents()
}