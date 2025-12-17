package com.zamfir.intercambista.presentation.screen.main.exchange

import com.zamfir.intercambista.data.enums.SortOption
import com.zamfir.intercambista.presentation.screen.main.MainScreenUi

sealed class ExchangeState {
    data object Loading : ExchangeState()
    data object UpdatingCoins : ExchangeState()
    data class Success(val mainUi : MainScreenUi, val sortType : SortOption) : ExchangeState()
    data class Error(val msg : String, val exception: Exception? = null) : ExchangeState()
}