package com.zamfir.intercambista.presentation.screen.main.exchange

import com.zamfir.intercambista.presentation.screen.main.MainScreenUi

sealed class ExchangeState {
    data object Loading : ExchangeState()
    data class Success(val mainUi : MainScreenUi, val timestamp: Long = System.currentTimeMillis()) : ExchangeState()
    data class Error(val msg : String, val exception: Exception? = null) : ExchangeState()
}