package com.zamfir.intercambista.presentation.screen.coinselection

import com.zamfir.intercambista.data.database.entity.Currency

sealed class CoinSelectionIntents {

    data class  OnSearchCoin(val query : String) : CoinSelectionIntents()
    data class  OnShowConfirmationDialog(val currency : Currency) : CoinSelectionIntents()
    data class  OnShowErrorDialog(val msg : String, val exception: Exception? = null) : CoinSelectionIntents()
    data class  OnCoinSelectionConfirmed(val code : String) : CoinSelectionIntents()
    data object OnDismissDialog : CoinSelectionIntents()

}