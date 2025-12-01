package com.zamfir.intercambista.presentation.screen.main.coins

import com.zamfir.intercambista.data.database.entity.Currency

sealed class CoinsIntents {

    data class OnCoinClicked(val coin : Currency) : CoinsIntents()
    data class OnSearch(val query : String) : CoinsIntents()
    data class OnFilter(val filter : String) : CoinsIntents()

}