package com.zamfir.intercambista.presentation.screen.main.coins

import com.zamfir.intercambista.data.database.entity.Currency

sealed class CoinsState {

    data object OnLoading : CoinsState()
    data class OnSuccess(val listOfCoins : List<Currency>) : CoinsState()
    data class OnFailure(val msg : String) : CoinsState()
}