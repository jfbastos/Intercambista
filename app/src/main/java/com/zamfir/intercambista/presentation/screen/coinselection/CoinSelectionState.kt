package com.zamfir.intercambista.presentation.screen.coinselection

import com.zamfir.intercambista.data.database.entity.Currency

sealed class CoinSelectionState {

    data object Loading : CoinSelectionState()

    data class OnSuccess(val currencies : List<Currency>) : CoinSelectionState()

    data class OnFailure(val msg : String, val exception: Exception? = null) : CoinSelectionState()
}

